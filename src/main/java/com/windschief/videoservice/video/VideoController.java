package com.windschief.videoservice.video;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.windschief.videoservice.ffmpeg.FfmpegUtility;
import com.windschief.videoservice.storage.FileType;
import com.windschief.videoservice.storage.StorageFileNotFoundException;
import com.windschief.videoservice.storage.StorageService;

import jakarta.persistence.EntityNotFoundException;

@Controller
public class VideoController {
	private final StorageService storageService;
	private final VideoRepository videoRepo;
	private final FfmpegUtility ffmpegUtility;
	private final VideoModelAssembler assembler;
	private final PagedResourcesAssembler<Video> pagedAssembler;
	private final VideoService videoService;

	@Autowired
	public VideoController(StorageService storageService, VideoRepository videoRepo, FfmpegUtility ffmpegUtility, 
			VideoModelAssembler assembler, PagedResourcesAssembler<Video> pagedAssembler, VideoService videoService) {
		this.storageService = storageService;
		this.videoRepo = videoRepo;
		this.ffmpegUtility = ffmpegUtility;
		this.assembler = assembler;
		this.pagedAssembler = pagedAssembler;
		this.videoService = videoService;
	}

	@GetMapping("/")
    public ResponseEntity<PagedModel<VideoDTO>> getVideos(@PageableDefault Pageable pageable) {
        PagedModel<VideoDTO> page = pagedAssembler.toModel(videoRepo.findAll(pageable), assembler);

        return ResponseEntity.ok().body(page);
    }

	@PostMapping("/")
	public ResponseEntity<VideoDTO> saveVideo(@RequestParam("file") MultipartFile file) {
        String videoFilename = storageService.store(file, FileType.VIDEO);

        String thumbnailName = ffmpegUtility.generateThumbnail(videoFilename);

        Video video = videoRepo.save(
            Video.builder()
                .withVideoName(FilenameUtils.getBaseName(file.getOriginalFilename()))
                .withVideoFilename(videoFilename)
				.withThumbnailFilename(thumbnailName)
                .build()
        );

		return ResponseEntity.ok().body(assembler.toModel(video));
	}

	@GetMapping("/{id}")
    public ResponseEntity<VideoDTO> getVideo(@PathVariable Long id) {
        Video video = videoRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Video not found for id:" + id));

        return ResponseEntity.ok().body(assembler.toModel(video));
    }	
	
	@DeleteMapping("/{id}")
	ResponseEntity<?> deleteVideo(@PathVariable Long id) {
		Video video = videoRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Video not found for id:" + id));

		storageService.delete(video.getVideoFilename(), FileType.VIDEO);
		storageService.delete(video.getThumbnailFilename(), FileType.THUMBNAIL);

		videoRepo.deleteById(id);
		
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/video/{id}")
	@ResponseBody
	public ResponseEntity<Resource> getVideoResource(@PathVariable Long id) {
		Video video = videoRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Thumbnail not found for id: " + id));

		Resource file = storageService.loadAsResource(video.getVideoFilename(), FileType.VIDEO);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@GetMapping("/thumbnail/{id}")
	@ResponseBody
	public ResponseEntity<Resource> getThumbnailResource(@PathVariable Long id) {
		Video video = videoRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Thumbnail not found for id: " + id));

		Resource file = storageService.loadAsResource(video.getThumbnailFilename(), FileType.THUMBNAIL);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}	

	@ExceptionHandler({ StorageFileNotFoundException.class, EntityNotFoundException.class })
	public ResponseEntity<?> handleNotFound() {
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/syncdbfromstorage")
    public ResponseEntity<?> syncDbFromStorage() {
		videoService.syncDatabaseFromStorage();
        return ResponseEntity.ok().build();
    }
}
