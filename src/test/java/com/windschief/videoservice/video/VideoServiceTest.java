package com.windschief.videoservice.video;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.windschief.videoservice.ffmpeg.FfmpegUtility;
import com.windschief.videoservice.storage.FileSystemStorageService;
import com.windschief.videoservice.storage.FileType;
import com.windschief.videoservice.storage.StorageProperties;
import com.windschief.videoservice.storage.StorageService;

@DataJpaTest
public class VideoServiceTest {
    private final String rootLocation = "/home/vinci/Videos/test";
    private final StorageProperties storageProperties = new StorageProperties(rootLocation);
    private final StorageService storageService = new FileSystemStorageService(storageProperties);
    private final FfmpegUtility ffmpegUtility = new FfmpegUtility(storageProperties);
    @Autowired
    private VideoRepository videoRepository;
    private VideoService videoService;

    @BeforeEach                              
    void setUp() {
        storageService.init();
        videoService = new VideoServiceImpl(storageService, videoRepository, ffmpegUtility);
    }

    @AfterEach
    void cleanUp() {
        FileSystemUtils.deleteRecursively(new File(rootLocation));
    }

    @Test                                            
    void givenStoredFile_whenSync_shouldCreatDbEntry() {
        //GIVEN
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            filename, 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        assertEquals("testfile.txt", storageService.store(testFile, FileType.VIDEO));

        //WHEN
        videoService.syncDatabaseFromStorage();

        //THEN
        assertTrue(videoRepository.findAll().stream().anyMatch(v -> v.getVideoName().equals("testfile")));
    }
}

