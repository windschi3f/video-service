package com.windschief.videoservice.video;

import java.time.Instant;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "video")
public class VideoDTO extends RepresentationModel<VideoDTO> {
    private Long id;
    private String videoName;
    private Instant createdAt;

    public VideoDTO() {
    }

    public VideoDTO(Long id, String videoName, Instant createdAt) {
        this.id = id;
        this.videoName = videoName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoName() {
        return this.videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VideoDTO videoResource = new VideoDTO();

        private Builder() {
        }

        public Builder withId(Long id) {
            this.videoResource.id = id;
            return this;
        }

        public Builder withVideoName(String videoName) {
            this.videoResource.videoName = videoName;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.videoResource.createdAt = createdAt;
            return this;
        }        

        public VideoDTO build() {
            return this.videoResource;
        }

    }
}