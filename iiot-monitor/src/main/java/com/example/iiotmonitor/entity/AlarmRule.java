package com.example.iiotmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("alarm_rule")
public class AlarmRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;       // 哪台设备
    private String metric;       // 监控指标："temperature" / "vibration" / "current"
    private String operator;     // 比较运算符：">" / "<" / ">=" / "<="
    private BigDecimal threshold; // 阈值，如 80
    private Integer duration;    // 持续多少秒才触发（本项目暂未用）
    private Integer level;       // 报警等级：1=警告 2=严重 3=紧急
    private Integer enabled;     // 是否启用：0=禁用 1=启用
}