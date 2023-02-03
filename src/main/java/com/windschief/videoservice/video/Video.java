package com.windschief.videoservice.video;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_seq")
    private Long id;
    private String videoName;
    private Instant createdAt;
    private String videoFilename;
    private String thumbnailFilename;

    public Video() {
    }

    public Video(Long id, String videoName, String filename, String thumbnailFilename) {
        this.id = id;
        this.videoName = videoName;
        this.videoFilename = filename;
        this.thumbnailFilename = thumbnailFilename;
    }

    public Long getId() {
        return this.id;
    }

    public String getVideoName() {
        return this.videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getVideoFilename() {
        return this.videoFilename;
    }

    public void setVideoFilename(String filename) {
        this.videoFilename = filename;
    }

    public String getThumbnailFilename() {
        return this.thumbnailFilename;
    }

    public void setThumbnailFilename(String filename) {
        this.thumbnailFilename = filename;
    }    

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Video)) {
            return false;
        }
        Video video = (Video) o;
        return Objects.equals(id, video.id) 
            && Objects.equals(videoName, video.videoName) 
            && Objects.equals(createdAt, video.createdAt)
            && Objects.equals(videoFilename, video.videoFilename)
            && Objects.equals(thumbnailFilename, video.thumbnailFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, videoName, createdAt, videoFilename, thumbnailFilename);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Video video = new Video();

        private Builder() {
        }

        public Builder withVideoName(String videoName) {
            this.video.videoName = videoName;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.video.createdAt = createdAt;
            return this;
        }

        public Builder withVideoFilename(String filename) {
            this.video.videoFilename = filename;
            return this;
        }

        public Builder withThumbnailFilename(String filename) {
            this.video.thumbnailFilename = filename;
            return this;
        }        

        public Video build() {
            this.video.createdAt = Instant.now();
            return this.video;
        }
    }
}
