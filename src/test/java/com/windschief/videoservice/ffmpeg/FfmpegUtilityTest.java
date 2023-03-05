package com.windschief.videoservice.ffmpeg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.windschief.videoservice.storage.FileType;
import com.windschief.videoservice.storage.StorageProperties;

@SpringJUnitConfig
public class FfmpegUtilityTest {
    private final String rootLocation = "/home/vinci/videoservice/test";
    private final StorageProperties storageProperties = new StorageProperties(rootLocation);
    private final FfmpegUtility ffmpegUtility = new FfmpegUtility(storageProperties);

    @Test
    void giveVideoFile_whenhumbnailGenerated_ThenThumbnailExists() throws InterruptedException, IOException {
        //GIVEN
        File testvideo = Paths.get(rootLocation, FileType.THUMBNAIL.subDirectory, "testvideo.mp4").toFile();
        assertTrue(testvideo.exists());

        File testThumbnail = Paths.get(rootLocation, FileType.THUMBNAIL.subDirectory, "testvideo.png").toFile();
        if (testThumbnail.exists()) {
            testThumbnail.delete();
            assertTrue(!testThumbnail.exists());
        }

        //WHEN
        Process thumbnailGeneration = ffmpegUtility.generateThumbnail(testvideo.getAbsolutePath());

        //THEN
        Path destinationPath = Paths.get(rootLocation, FileType.THUMBNAIL.subDirectory, FfmpegUtility.getThumbnailFilename(testvideo.getName()));
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(thumbnailGeneration.getInputStream()));
        while ((reader.readLine()) != null) {}
        
        assertTrue(destinationPath.toFile().exists());
    }

    @Test
    void givenFilename_whenGetThumbnailFilename_ThenReturnsExpectedThumbnailName() {
        //GIVEN
        String filename = Paths.get("home", "testuser", "videos", "video.mpg").toString();
        
        //WHEN
        String returnedFilename = FfmpegUtility.getThumbnailFilename(filename);

        //THEN
        assertEquals("video.png", returnedFilename);
    }
}
