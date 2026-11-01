package com.example.iiotmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("alarm_record")
public class AlarmRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;        // 哪台设备报警
    private Long ruleId;          // 哪条规则触发的
    private String metric;        // 指标（冗余字段）
    private BigDecimal metricValue; // 实测值
    private BigDecimal threshold;   // 当时规则的阈值（冗余字段）
    private Integer level;        // 报警等级（冗余字段）
    private Integer status;       // 状态：0未确认 1已确认 2已恢复
    private LocalDateTime alarmTime;  // 报警时间
    private LocalDateTime recoverTime; // 恢复时间
    private String aiSuggestion;  // AI 生成的处置建议
}