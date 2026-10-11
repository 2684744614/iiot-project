package com.example.iiotmonitor.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;

    @Autowired
    private com.example.iiotmonitor.mapper.KnowledgeDocMapper knowledgeDocMapper;

    // 从环境变量读取，绝不硬编码
    private static final String API_KEY = System.getenv("DASHSCOPE_API_KEY");

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void uploadDocument(File file) {
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(file));
        List<Document> documents = reader.get();
        TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 10, 10000, true);
        List<Document> chunks = splitter.apply(documents);
        vectorStore.add(chunks);

        // 记录到 MySQL
        com.example.iiotmonitor.entity.KnowledgeDoc doc = new com.example.iiotmonitor.entity.KnowledgeDoc();
        doc.setFileName(file.getName());
        doc.setFileType("text/plain");
        doc.setUploadTime(java.time.LocalDateTime.now());
        doc.setChunkCount(chunks.size());
        doc.setStatus(1);
        knowledgeDocMapper.insert(doc);

        System.out.println("📚 文档 [" + file.getName() + "] 入库完成，共 " + chunks.size() + " 个切片。");
    }

    public String chat(String question) throws Exception {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException("环境变量 DASHSCOPE_API_KEY 未配置，请在 IDEA 运行配置里设置");
        }

        // 用 Spring AI 原生相似度搜索（简洁、无反射）
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build()
        );

        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        if (context.isEmpty()) {
            return "知识库中暂未找到相关处置方案。";
        }

        String prompt = "你是一个工业设备运维专家。请根据以下知识库内容，简明扼要地回答用户问题。\n" +
                "【知识库内容】\n" + context + "\n\n【用户问题】\n" + question;

        Generation gen = new Generation();
        GenerationParam param = GenerationParam.builder()
                .apiKey(API_KEY)
                .model("qwen-plus")
                .prompt(prompt)
                .build();

        GenerationResult result = gen.call(param);
        return result.getOutput().getText();
    }
}