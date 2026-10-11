package com.example.iiotmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.iiotmonitor.entity.AlarmRecord;
import com.example.iiotmonitor.entity.Device;
import com.example.iiotmonitor.mapper.AlarmRecordMapper;
import com.example.iiotmonitor.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    /** 查询最近 50 条报警记录，附带设备名称 */
    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        List<AlarmRecord> records = alarmRecordMapper.selectList(
                new QueryWrapper<AlarmRecord>().orderByDesc("alarm_time").last("LIMIT 50")
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (AlarmRecord r : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("deviceId", r.getDeviceId());
            map.put("metric", r.getMetric());
            map.put("metricValue", r.getMetricValue());
            map.put("threshold", r.getThreshold());
            map.put("level", r.getLevel());
            map.put("status", r.getStatus());
            map.put("alarmTime", r.getAlarmTime());
            map.put("aiSuggestion", r.getAiSuggestion());

            // 查设备名称
            Device d = deviceMapper.selectById(r.getDeviceId());
            map.put("deviceName", d != null ? d.getDeviceName() : "未知设备");
            result.add(map);
        }
        return result;
    }
}