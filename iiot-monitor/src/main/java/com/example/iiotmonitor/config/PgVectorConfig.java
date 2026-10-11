package com.example.iiotmonitor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PgVectorConfig {

    private HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();
        // ★ 关键：末尾加 ?stringtype=unspecified，解决向量检索的 SQL 类型问题
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/iiot_vector?stringtype=unspecified");
        config.setUsername("postgres");
        config.setPassword("postgres123");
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("PgVectorPool");
        return new HikariDataSource(config);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(buildDataSource());
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .build();
    }
}