package com.example.iiotmonitor.controller;

import com.example.iiotmonitor.entity.Device;
import com.example.iiotmonitor.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceMapper deviceMapper;

    // 查询所有设备列表
    @GetMapping("/list")
    public List<Device> list() {
        // 直接使用 MyBatis-Plus 内置的 selectList 方法，传入 null 表示无条件查询所有
        return deviceMapper.selectList(null);
    }
}