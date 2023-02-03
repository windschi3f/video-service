package com.windschief.videoservice.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.windschief.videoservice.ffmpeg.FfmpegUtility;
import com.windschief.videoservice.storage.FileType;
import com.windschief.videoservice.storage.StorageService;

@Service
public class VideoServiceImpl implements VideoService {

    private final StorageService storageService;
    private final VideoRepository videoRepo;
    private final FfmpegUtility ffmpegUtility;

    public VideoServiceImpl(StorageService storageService, VideoRepository videoRepo, FfmpegUtility ffmpegUtility) {
        this.storageService = storageService;
        this.videoRepo = videoRepo;
        this.ffmpegUtility = ffmpegUtility;
    }

    @Override
    public void syncDatabaseFromStorage() {
        Set<String> videoFilenames = storageService.loadAll(FileType.VIDEO)
            .map(path -> FilenameUtils.getName(path.getFileName().toString()))
            .collect(Collectors.toSet());

        List<Video> videosDatabase = videoRepo.findAll();

        for (String videoFilename : videoFilenames) {
            if (videosDatabase.stream().noneMatch(video -> video.getVideoFilename().equals(videoFilename))) {
			    if (!Files.exists(storageService.load(FfmpegUtility.getThumbnailFilename(videoFilename), FileType.THUMBNAIL))) {
                    ffmpegUtility.generateThumbnail(videoFilename);
                }

                FileTime creationTime;
                try {
                    creationTime = (FileTime) Files.getAttribute(storageService.load(videoFilename, FileType.VIDEO), "creationTime");
                } catch (IOException e) {
                    creationTime = FileTime.from(Instant.now());
                }

                videoRepo.save(
                    Video.builder()
                        .withVideoName(videoFilename)
                        .withVideoFilename(videoFilename)
                        .withThumbnailFilename(FfmpegUtility.getThumbnailFilename(videoFilename))
                        .withCreatedAt(creationTime.toInstant())
                        .build()
                );
            }
        }
    }
}