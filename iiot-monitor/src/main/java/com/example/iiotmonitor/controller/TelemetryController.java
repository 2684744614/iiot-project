package com.example.iiotmonitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.iiotmonitor.entity.DeviceTelemetry;
import com.example.iiotmonitor.mapper.DeviceTelemetryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    @Autowired
    private DeviceTelemetryMapper telemetryMapper;

    /** 查询某设备最近的 N 条遥测数据，用于初始化曲线 */
    @GetMapping("/recent")
    public List<DeviceTelemetry> recent(@RequestParam Long deviceId,
                                        @RequestParam(defaultValue = "30") Integer limit) {
        List<DeviceTelemetry> list = telemetryMapper.selectList(
                new QueryWrapper<DeviceTelemetry>()
                        .eq("device_id", deviceId)
                        .orderByDesc("report_time")
                        .last("LIMIT " + limit)
        );
        // 倒序反转成时间正序，方便前端画图
        Collections.reverse(list);
        return list;
    }
}