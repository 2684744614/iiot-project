package com.example.iiotmonitor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.iiotmonitor.entity.AlarmRecord;
import com.example.iiotmonitor.entity.AlarmRule;
import com.example.iiotmonitor.entity.Device;
import com.example.iiotmonitor.entity.DeviceTelemetry;
import com.example.iiotmonitor.mapper.AlarmRecordMapper;
import com.example.iiotmonitor.mapper.AlarmRuleMapper;
import com.example.iiotmonitor.mapper.DeviceMapper;
import com.example.iiotmonitor.mapper.DeviceTelemetryMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TelemetryService {

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private DeviceTelemetryMapper telemetryMapper;

    @Autowired
    private AlarmRuleMapper alarmRuleMapper;

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate websocketTemplate;

    @Autowired
    private AlarmAiService alarmAiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(String topic, String payload) {
        try {
            // 1. 解析 topic 获取 deviceCode（如 factory/motor-01/telemetry -> motor-01）
            String[] parts = topic.split("/");
            if (parts.length < 3) return;
            String deviceCode = parts[1];

            // 2. 查数据库找设备
            Device device = deviceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Device>()
                            .eq("device_code", deviceCode)
            );
            if (device == null) return;

            // 3. 解析 JSON
            JsonNode json = objectMapper.readTree(payload);

            // 4. 入库
            DeviceTelemetry telemetry = new DeviceTelemetry();
            telemetry.setDeviceId(device.getId());
            telemetry.setTemperature(BigDecimal.valueOf(json.get("temperature").asDouble()));
            telemetry.setVibration(BigDecimal.valueOf(json.get("vibration").asDouble()));
            telemetry.setCurrent(BigDecimal.valueOf(json.get("current").asDouble()));
            telemetry.setSpeed(json.get("speed").asInt());
            telemetry.setReportTime(LocalDateTime.now());
            telemetryMapper.insert(telemetry);

            // 5. 数据入库后，检查报警规则
            checkAlarmRules(device, json);

            // 6. 通过 WebSocket 实时推送给前端大屏
            java.util.Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("deviceId", device.getId());
            pushData.put("deviceName", device.getDeviceName());
            pushData.put("temperature", telemetry.getTemperature());
            pushData.put("vibration", telemetry.getVibration());
            pushData.put("current", telemetry.getCurrent());
            pushData.put("time", java.time.LocalDateTime.now().toString());

            websocketTemplate.convertAndSend("/topic/device/" + device.getId(), pushData);

            System.out.println("数据已入库: 设备=" + device.getDeviceName() + ", 温度=" + telemetry.getTemperature());
        } catch (Exception e) {
            System.err.println("处理 MQTT 消息失败: " + e.getMessage());
        }
    }

    private void checkAlarmRules(Device device, com.fasterxml.jackson.databind.JsonNode json) {
        // 1. 查询该设备所有启用中的报警规则
                List<AlarmRule> rules = alarmRuleMapper.selectList(
                        new QueryWrapper<AlarmRule>()
                        .eq("device_id", device.getId())
                        .eq("enabled", 1)
        );

        for (AlarmRule rule : rules) {
            double value = 0;
            // 2. 根据规则指标，从 JSON 里取出对应的值
            switch (rule.getMetric()) {
                case "temperature": value = json.get("temperature").asDouble(); break;
                case "vibration": value = json.get("vibration").asDouble(); break;
                case "current": value = json.get("current").asDouble(); break;
            }

            // 3. 判断是否超限（目前只实现了 > 运算符）
            boolean triggered = false;
            if (">".equals(rule.getOperator()) && value > rule.getThreshold().doubleValue()) {
                triggered = true;
            }

            /*可扩展
            switch (rule.getOperator()) {
                case ">":  triggered = value >  threshold; break;
                case "<":  triggered = value <  threshold; break;
                case ">=": triggered = value >= threshold; break;
                case "<=": triggered = value <= threshold; break;
            }*/

            if (triggered) {
                // 4. 核心亮点：Redis 滑动窗口去重
                // 构造唯一 Key，比如：alarm:dedup:1:1
                String dedupKey = "alarm:dedup:" + device.getId() + ":" + rule.getId();
                // setIfAbsent 相当于 Redis 的 SETNX，如果 key 不存在才设置，并设置 60 秒过期
                Boolean absent = redisTemplate.opsForValue().setIfAbsent(
                        dedupKey, "1", 60, TimeUnit.SECONDS
                );

                /*改进版滑动窗口（真正的滑动）：
                // 每次触发都刷新过期时间
                Boolean absent = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1");
                if (Boolean.TRUE.equals(absent)) {
                    // 首次，设过期
                    redisTemplate.expire(dedupKey, 60, TimeUnit.SECONDS);
                } else {
                    // 已存在，刷新过期时间（延长窗口）
                    redisTemplate.expire(dedupKey, 60, TimeUnit.SECONDS);
                }
                但这会导致"持续超标时永远不报警第二次"*/

                // 5. 插入报警记录
                // 如果 absent 为 true，说明 60 秒内第一次触发，可以入库
                if (Boolean.TRUE.equals(absent)) {
                    AlarmRecord record = new AlarmRecord();
                    record.setDeviceId(device.getId());
                    record.setRuleId(rule.getId());
                    record.setMetric(rule.getMetric());
                    record.setMetricValue(java.math.BigDecimal.valueOf(value));
                    record.setThreshold(rule.getThreshold());
                    record.setLevel(rule.getLevel());
                    record.setAlarmTime(java.time.LocalDateTime.now());
                    alarmRecordMapper.insert(record);

                    System.out.println("🚨 触发报警: " + device.getDeviceName() + " 的 " + rule.getMetric() + " 值为 " + value + "，已去重入库！");

                    // 6. ★★★ 新增：异步调用 AI 生成处置建议 ★★★
                    alarmAiService.generateAiSuggestion(
                            record.getId(),
                            device.getDeviceName(),
                            rule.getMetric(),
                            value
                    );
                } else {
                    // 如果 absent 为 false，说明 60 秒内已经报过了，直接忽略
                    // System.out.println("⏳ 报警被 Redis 去重拦截: " + dedupKey);
                }
            }
        }
    }
}