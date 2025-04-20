package com.example.neura_search.dto;

public class DirectoryRequestDTO {
    private String directoryPath;

    public DirectoryRequestDTO() {
    }

    public DirectoryRequestDTO(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
}
