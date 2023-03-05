package com.windschief.videoservice.ffmpeg;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.windschief.videoservice.storage.FileType;
import com.windschief.videoservice.storage.StorageProperties;

@Service
public class FfmpegUtility {
    //private final String SHELL = System.getProperty("os.name").toLowerCase().startsWith("windows") ? "cmd.exe" : "/bin/sh";
	private final Path rootLocation;

	public FfmpegUtility(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

    public Process generateThumbnail(String videoPath) {
        String destination = rootLocation
            .resolve(FileType.THUMBNAIL.subDirectory)
            .resolve(getThumbnailFilename(videoPath))
            .toString(); 

        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", videoPath, "-vf", "thumbnail", "-frames:v", "1", destination)
                .directory(rootLocation.toFile());
            pb.redirectErrorStream(true);
            return pb.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Thumbnail for " + FilenameUtils.getName(videoPath), e);
        }
    }

    public static String getThumbnailFilename(String videoFilename) {
        return FilenameUtils.getBaseName(videoFilename) + ".png";
    }
}
