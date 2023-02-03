package com.windschief.videoservice.video;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.windschief.videoservice.VideoController;

@Component
public class VideoModelAssembler implements RepresentationModelAssembler<Video, VideoDTO> {

    @Override
    public VideoDTO toModel(Video entity) {
        VideoDTO videoDTO = VideoDTO.builder()
            .withId(entity.getId())
            .withVideoName(entity.getVideoName())
            .withCreatedAt(entity.getCreatedAt())
            .build();
        
        videoDTO.add(linkTo(methodOn(VideoController.class).getVideoResource(entity.getId())).withRel("videoUrl"));        
        videoDTO.add(linkTo(methodOn(VideoController.class).getThumbnailResource(entity.getId())).withRel("thumbnailUrl"));
        videoDTO.add(linkTo(methodOn(VideoController.class).getVideo(entity.getId())).withSelfRel());

        return videoDTO;
    }
}