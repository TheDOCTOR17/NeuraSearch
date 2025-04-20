package com.example.neura_search.dto;

public class SearchResultDTO {
    private String filePath;
    private String content;
    private double score;
    private String fileType;

    public SearchResultDTO() {
    }

    public SearchResultDTO(String filePath, String content, double score, String fileType) {
        this.filePath = filePath;
        this.content = content;
        this.score = score;
        this.fileType = fileType;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
