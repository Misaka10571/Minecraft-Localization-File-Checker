package com.example.localizationchecker.config;

public class FileCheckEntry {
    private String path;
    private String type; // "file", "directory", "pattern"
    private String hash; // 可选，用于验证文件完整性
    private long size; // 可选，用于验证文件大小

    public FileCheckEntry() {}

    public FileCheckEntry(String path, String type) {
        this.path = path;
        this.type = type;
    }

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}