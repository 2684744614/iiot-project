package com.example.iiotmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;              // 主键
    private String deviceCode;    // 设备编号，如 "motor-01"
    private String deviceName;    // 设备名称，如 "1号电机"
    private String deviceType;    // 设备类型，如 "电机"、"水泵"
    private String location;      // 安装位置，如 "A车间"
    private Integer status;       // 状态：0=离线，1=在线
    private LocalDateTime createTime;  // 创建时间
}