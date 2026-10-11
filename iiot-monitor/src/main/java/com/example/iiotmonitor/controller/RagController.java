package com.example.iiotmonitor.controller;

import com.example.iiotmonitor.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    @Autowired
    private com.example.iiotmonitor.mapper.KnowledgeDocMapper knowledgeDocMapper;


    // 上传知识库文档
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        File tempFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        ragService.uploadDocument(tempFile);
        return "上传并向量化成功！";
    }

    // 提问测试
    @GetMapping("/chat")
    public String chat(@RequestParam String question) throws Exception {
        return ragService.chat(question);
    }

    /** 查询已上传的文档列表 */
    @GetMapping("/documents")
    public java.util.List<com.example.iiotmonitor.entity.KnowledgeDoc> listDocs() {
        return knowledgeDocMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.iiotmonitor.entity.KnowledgeDoc>()
                        .orderByDesc("upload_time")
        );
    }
}