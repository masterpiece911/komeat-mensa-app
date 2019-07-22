package com.pem.mensa_app.models.meal;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Comment {

    private String content;
    private String timestamp;

    public Comment(){

    };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
