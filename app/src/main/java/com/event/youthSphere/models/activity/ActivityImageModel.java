package com.event.youthSphere.models.activity;

public class ActivityImageModel {
    private String imageUrl;
    private String uploadDate;
    private String domain;
    private String name;

    public ActivityImageModel() {}

    // Getters and setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

