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

    public String generateThumbnail(String filename) {
        Path videoPath = Paths.get(rootLocation.toString(), FileType.VIDEO.subDirectory, FilenameUtils.getName(filename));

        try {
            new ProcessBuilder("ffmpeg", "-i", videoPath.toString(), "-vf", "thumbnail", "-frames:v", "1", getThumbnailFilename(filename))
                .directory(rootLocation.toFile())
                .start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Thumbnail for " + FilenameUtils.getName(filename), e);
        }

        return generateThumbnail(filename);
    }

    public static String getThumbnailFilename(String videoFilename) {
        return FilenameUtils.getBaseName(videoFilename) + ".png";
    }
}
