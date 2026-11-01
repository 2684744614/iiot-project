package com.example.iiotmonitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_doc")
public class KnowledgeDoc {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;      // 文件名
    private String fileType;      // 文件类型（MIME，如 text/plain）
    private LocalDateTime uploadTime;  // 上传时间
    private Integer chunkCount;   // 切片数
    private Integer status;       // 状态：1=已向量化 0=处理中
}