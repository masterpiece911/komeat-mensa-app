package com.pem.mensa_app.models.image;

import org.joda.time.LocalDateTime;

import java.util.List;

public class Image {

    List<String> uids;

    String imagePath;

    LocalDateTime localDateTime;

    public Image(List<String> uids, String imagePath, LocalDateTime localDateTime) {
        this.uids = uids;
        this.imagePath = imagePath;
        this.localDateTime = localDateTime;
    }

    public List<String> getUids() {
        return uids;
    }

    public void setUids(List<String> uids) {
        this.uids = uids;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }
}
