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
    private Long deviceId;
    private String metric;
    private String operator;
    private BigDecimal threshold;
    private Integer duration;
    private Integer level;
    private Integer enabled;
}