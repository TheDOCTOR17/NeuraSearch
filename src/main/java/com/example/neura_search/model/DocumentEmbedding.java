package com.example.neura_search.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_embeddings")
public class DocumentEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(length = 10000)
    private String content;

    @Column(nullable = false)
    private String fileType;

    // Reference to vector in Milvus
    private Long vectorId;

    public DocumentEmbedding() {
    }

    public DocumentEmbedding(String filePath, String content, String fileType, Long vectorId) {
        this.filePath = filePath;
        this.content = content;
        this.fileType = fileType;
        this.vectorId = vectorId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getVectorId() {
        return vectorId;
    }

    public void setVectorId(Long vectorId) {
        this.vectorId = vectorId;
    }
}
