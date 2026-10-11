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
    private Long deviceId;
    private BigDecimal temperature;
    private BigDecimal vibration;
    private BigDecimal current;
    private Integer speed;
    private LocalDateTime reportTime;
}