package com.example.iiotmonitor.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.iiotmonitor.entity.KnowledgeDoc;
import com.example.iiotmonitor.mapper.KnowledgeDocMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;

    @Autowired
    private KnowledgeDocMapper knowledgeDocMapper;

    // 从环境变量读取，不硬编码
    private static final String API_KEY = System.getenv("DASHSCOPE_API_KEY");

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void uploadDocument(File file) {
        // 1. 解析文档
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(file));
        List<Document> documents = reader.get();

        // 2. 切片
        TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 10, 10000, true);
        List<Document> chunks = splitter.apply(documents);
        // 3. 向量化+存到PgVector库
        vectorStore.add(chunks);

        // 4. 记录元数据到 MySQL
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setFileName(file.getName());
        doc.setFileType("text/plain");
        doc.setUploadTime(LocalDateTime.now());
        doc.setChunkCount(chunks.size());
        doc.setStatus(1);
        knowledgeDocMapper.insert(doc);

        System.out.println("📚 文档 [" + file.getName() + "] 入库完成，共 " + chunks.size() + " 个切片。");
    }

    public String chat(String question) throws Exception {
        // 1. 检查 API Key
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException("环境变量 DASHSCOPE_API_KEY 未配置，请在 IDEA 运行配置里设置");
        }

        // 2. 向量检索, 用 Spring AI 原生相似度搜索（简洁、无反射）
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3)
                        .similarityThreshold(0.5)   // 相似度低于 0.5 的不返回
                        .build());

        // 3. 拼接上下文
        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 4. 知识库空判断
        if (context.isEmpty()) {
            return "知识库中暂未找到相关处置方案。";
        }

        // 5. 构造 RAG Prompt
        String prompt = "你是一个工业设备运维专家。请根据以下知识库内容，简明扼要地回答用户问题。\n" +
                "【知识库内容】\n" + context + "\n\n【用户问题】\n" + question;

        // 6. 调用大模型
        Generation gen = new Generation();
        GenerationParam param = GenerationParam.builder()
                .apiKey(API_KEY)
                .model("qwen-plus")
                .prompt(prompt)
                .build();
        GenerationResult result = gen.call(param);

        // 7. 提取回答
        return result.getOutput().getText();
    }
}