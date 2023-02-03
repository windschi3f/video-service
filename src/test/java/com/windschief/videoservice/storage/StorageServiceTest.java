package com.windschief.videoservice.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@SpringJUnitConfig
public class StorageServiceTest {
    private final String rootLocation = "/home/vinci/Videos/test";
    private final StorageProperties storageProperties = new StorageProperties(rootLocation);
    private final StorageService storageService = new FileSystemStorageService(storageProperties);

    @BeforeEach                                  
    void setUp() {
        storageService.init();
    }

    @AfterEach
    void cleanUp() {
        FileSystemUtils.deleteRecursively(new File(rootLocation));
    }

    @Test                                              
    void givenRootLocation_whenInitializeService_shouldCreateDirectories() {
        assertTrue(Files.exists(Paths.get(rootLocation)));
        for (FileType fileType : FileType.values()) {
            assertTrue(Files.exists(Paths.get(rootLocation).resolve(fileType.subDirectory)));
        }
    }

	@Test
	public void giveNonExistentFile_whenLoad_shouldNotExist() {
		assertThat(storageService.load("foo.txt", FileType.VIDEO)).doesNotExist();
	}    

    @Test
    void givenStoredFile_whenLoadFile_shouldReturnFile() throws IOException {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            Paths.get(rootLocation, filename).toString(), 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        
        storageService.store(testFile, FileType.VIDEO);
        Resource resource = assertDoesNotThrow(() -> { return storageService.loadAsResource(filename, FileType.VIDEO); });
        assertDoesNotThrow(() -> resource.getFile() );
    }

    @Test
    void givenFile_whenStoreFile_shouldReturnFilename() throws IOException {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            filename, 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        assertEquals("testfile.txt", storageService.store(testFile, FileType.VIDEO));
    }

    @Test
    void givenSameFilenames_whenStoreFiles_shouldReturnDifferentFilenames() throws IOException {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            filename, 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        String filename1 = storageService.store(testFile, FileType.VIDEO);
        String filename2 = storageService.store(testFile, FileType.VIDEO);

        assertNotEquals(filename1, filename2);
    }    

    @Test
	public void givePathOutsideDirectory_whenStore_shouldStoreInCorrectLocation() {
        final String filename = "/etc/passwd/outside.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            filename, 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        
        storageService.store(testFile, FileType.VIDEO);

        assertEquals(true, Files.exists(Paths.get(rootLocation, FileType.VIDEO.subDirectory, "outside.txt")));
        assertEquals(false, Files.exists(Paths.get("/etc/passwd", "outside.txt")));
        assertEquals(false, Files.exists(Paths.get("/etc/passwd",  FileType.VIDEO.subDirectory, "outside.txt")));
	}

    @Test
    void givenStoredFile_whenLoadAll_shouldReturnOnePath() {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            Paths.get(rootLocation, filename).toString(), 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        
        storageService.store(testFile, FileType.VIDEO);

        assertEquals(1L, storageService.loadAll(FileType.VIDEO).count());
    }

    @Test
    void givenNoFiles_whenLoadAll_shouldNotReturnPaths() {
        assertEquals(0L, storageService.loadAll(FileType.VIDEO).count());
    }

    @Test
    void givenStoredFile_whenDelete_shouldReturnTrue() {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            Paths.get(rootLocation, filename).toString(), 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        
        storageService.store(testFile, FileType.VIDEO);
        boolean deleted = assertDoesNotThrow(() -> { return storageService.delete(filename, FileType.VIDEO); });
        assertEquals(true, deleted);
    }

    @Test
    void givenDeletedFile_whenLoadResource_shouldThrowStorageFileNotFoundException() {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            Paths.get(rootLocation, filename).toString(), 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        
        storageService.store(testFile, FileType.VIDEO);
        storageService.delete(filename, FileType.VIDEO);

        assertThrows(
            StorageFileNotFoundException.class, 
            () -> { storageService.loadAsResource(filename, FileType.VIDEO); }
        );
    }    

    @Test
    void givenStoredFile_whenDeleteAll_shouldReturnNoFiles() {
        final String filename = "testfile.txt";
        final MultipartFile testFile = new MockMultipartFile(
            filename,
            Paths.get(rootLocation, filename).toString(), 
            MediaType.TEXT_PLAIN_VALUE, 
            "content".getBytes()
        );
        
        storageService.store(testFile, FileType.VIDEO);
        storageService.deleteAll();

        assertEquals(0L, storageService.loadAll().count());
    }    
}
