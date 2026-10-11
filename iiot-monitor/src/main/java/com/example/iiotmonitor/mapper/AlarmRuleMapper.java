package com.example.iiotmonitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.iiotmonitor.entity.AlarmRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {
}