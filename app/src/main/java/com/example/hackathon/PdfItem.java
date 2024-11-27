package com.example.hackathon;

public class PdfItem {
    private String fileName;
    private String addedDate;

    public PdfItem(String fileName, String addedDate) {
        this.fileName = fileName;
        this.addedDate = addedDate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAddedDate() {
        return addedDate;
    }
}
