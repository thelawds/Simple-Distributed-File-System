package fx.miserable.sdfs.storage.controller;

import fx.miserable.sdfs.storage.dto.FileMetaData;
import fx.miserable.sdfs.storage.service.FileStorageService;
import fx.miserable.sdfs.storage.service.NamingNodeConnectionService;
import fx.miserable.sdfs.storage.service.StorageNodeConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

	private final FileStorageService fileStorageService;
	private final NamingNodeConnectionService namingNodeConnectionService;
	private final StorageNodeConnectionService storageNodeConnectionService;

	@PostMapping("/create")
	public ResponseEntity<FileMetaData> createFile(@RequestBody FileMetaData metadata) {
		try {
			if (metadata.isCreateReplica()) {
				new Thread(() -> {
					storageNodeConnectionService.createOnReplica(metadata);
				}).start();
			}

			var res = fileStorageService.createFile(metadata);
			var newMetadata = FileMetaData.fromFile(res, metadata.getPath());
			namingNodeConnectionService.updateFile(newMetadata);

			return new ResponseEntity<>(newMetadata, OK);
		} catch (Exception e) {
			log.error("Error {}", e.getMessage());
			throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
		}
	}

	@PostMapping("/upload")
	public ResponseEntity<FileMetaData> uploadFile(
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam(name = "path") String path,
			@RequestParam(name = "create_replica") Boolean createReplica,
			@RequestParam(name = "executable", required = false, defaultValue = "true") Boolean executable,
			@RequestParam(name = "readable", required = false, defaultValue = "true") Boolean readable,
			@RequestParam(name = "writable", required = false, defaultValue = "true") Boolean writable,
			@RequestParam(name = "last_update", required = false, defaultValue = "-1") Long lastUpdate
	) {
		if (lastUpdate == -1) {
			lastUpdate = null;
		}

		var metadata = FileMetaData.builder()
								   .path(path)
								   .createReplica(createReplica)
								   .executable(executable)
								   .readable(readable)
								   .writable(writable)
								   .lastUpdate(lastUpdate)
								   .build();

		try {
			var res = fileStorageService.saveFile(metadata, multipartFile);
			var newMetadata = FileMetaData.fromFile(res, metadata.getPath());
			namingNodeConnectionService.updateFile(newMetadata);

			if (metadata.isCreateReplica()) {
				new Thread(() -> {
					storageNodeConnectionService.uploadOnReplica(newMetadata, res);
				}).start();
			}

			return new ResponseEntity<>(newMetadata, OK);
		} catch (Exception e) {
			log.error("Error {}", e.getMessage());
			throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
		}
	}

	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteFile(
			@RequestParam(name = "path") String path
	) {
		try {
			fileStorageService.deleteFileByPath(path);
			return new ResponseEntity<>("Succeeded", OK);
		} catch (FileNotFoundException e) {
			log.error("Error {}", e.getMessage());
			throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping("/download")
	public ResponseEntity<InputStreamResource> downloadFile(
			@RequestParam(name = "path") String path
	) {
		try {
			var file = fileStorageService.getByPath(path);
			return ResponseEntity.ok()
								 .contentLength(file.length())
								 .contentType(MediaType.APPLICATION_OCTET_STREAM)
								 .body(new InputStreamResource(new FileInputStream(file)));

		} catch (FileNotFoundException e) {
			var message = format("File not found by path %s.", path);
			log.error(message);
			throw new ResponseStatusException(NOT_FOUND, message, e);
		}
	}

	@DeleteMapping("/prune")
	public ResponseEntity<Long> prune() {
		return new ResponseEntity<>(fileStorageService.prune(), OK);
	}

	@PostMapping("/replicate")
	public ResponseEntity<Void> replicate(
			@RequestParam(name = "path") String path,
			@RequestParam(name = "executable") Boolean executable,
			@RequestParam(name = "readable") Boolean readable,
			@RequestParam(name = "writable") Boolean writable,
			@RequestParam(name = "last_update") Long lastUpdate,
			@RequestParam(name = "replica_address") String replicaAddress
	) {
		var file = fileStorageService.getByPath(path);
		var metadata = FileMetaData.builder()
								   .path(path)
								   .createReplica(false)
								   .executable(executable)
								   .readable(readable)
								   .writable(writable)
								   .lastUpdate(lastUpdate)
								   .size(file.length())
								   .build();

		new Thread(() -> storageNodeConnectionService.uploadOnReplica(metadata, file)).start();

		return new ResponseEntity<>(OK);
	}

}
