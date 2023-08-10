package com.example.Bill_Breakdown_Application;

public class ResultEntry {
    private String result;
    private String timestamp;

    public ResultEntry(String result, String timestamp) {
        this.result = result;
        this.timestamp = timestamp;
    }

    public String getResult() {
        return result;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

