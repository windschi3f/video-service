package com.windschief.videoservice.storage;

public enum FileType {
    VIDEO("videos"),
    THUMBNAIL("thumbnails"),
    PREVIEW("previews");

    public final String subDirectory;

    private FileType(String subDirectory) {
        this.subDirectory = subDirectory;
    }
}