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
    private Long deviceId;
    private Long ruleId;
    private String metric;
    private BigDecimal metricValue;
    private BigDecimal threshold;
    private Integer level;
    private Integer status;
    private LocalDateTime alarmTime;
    private LocalDateTime recoverTime;
    private String aiSuggestion;
}