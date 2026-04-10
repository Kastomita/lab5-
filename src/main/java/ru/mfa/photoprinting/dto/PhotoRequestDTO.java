package ru.mfa.photoprinting.dto;

public class PhotoRequestDTO {
    private String fileName;
    private String fileUrl;
    private Integer quantity;
    private String description;
    private Long formatId;

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getFormatId() { return formatId; }
    public void setFormatId(Long formatId) { this.formatId = formatId; }
}