package com.example.cspf.cases;

public class CaseLawResponse {
    private String title;
    private String link;
    private String description;
    private String pubDate;
    private String types;


    public CaseLawResponse(String title, String link, String description, String pubDate, String types) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.types = types;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }
}
