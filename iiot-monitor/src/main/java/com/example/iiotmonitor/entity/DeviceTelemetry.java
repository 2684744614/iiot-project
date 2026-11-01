package com.example.iiotmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_telemetry")
public class DeviceTelemetry {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;        // 外键，关联 device 表
    private BigDecimal temperature;  // 温度
    private BigDecimal vibration;    // 振动
    private BigDecimal current;      // 电流
    private Integer speed;           // 转速
    private LocalDateTime reportTime;  // 上报时间
}