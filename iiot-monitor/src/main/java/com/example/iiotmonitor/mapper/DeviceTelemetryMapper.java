package com.example.iiotmonitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.iiotmonitor.entity.DeviceTelemetry;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceTelemetryMapper extends BaseMapper<DeviceTelemetry> {
}