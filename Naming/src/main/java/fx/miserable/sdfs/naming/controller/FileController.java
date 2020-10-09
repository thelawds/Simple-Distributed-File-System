package fx.miserable.sdfs.naming.controller;

import fx.miserable.sdfs.naming.dto.FileInformation;
import fx.miserable.sdfs.naming.dto.request.FileUpdateRequest;
import fx.miserable.sdfs.naming.exception.NotEnoughStorageServersException;
import fx.miserable.sdfs.naming.service.FileInformationService;
import fx.miserable.sdfs.naming.service.StorageNodeInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.util.Set;

import static fx.miserable.sdfs.naming.dto.FileInformation.fromEntity;
import static fx.miserable.sdfs.naming.dto.FileInformation.fromEntitySet;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

	private final FileInformationService fileInformationService;
	private final StorageNodeInformationService storageNodeInformationService;

	@PostMapping("/update")
	public ResponseEntity<FileInformation> createOrUpdateFile(@RequestBody FileUpdateRequest fileUpdateRequest) {
		try {
			storageNodeInformationService.updateAvailableSizeByFileUpdateRequest(fileUpdateRequest);
			return new ResponseEntity<>(
					fromEntity(fileInformationService.saveFileByDto(fileUpdateRequest)), OK
			);
		} catch (NotEnoughStorageServersException e) {
			throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping("/information/{path}")
	public ResponseEntity<FileInformation> getFileInformation(
			@PathVariable(name = "path") String path
	) {
		try {
			var file = fileInformationService.getFileByPath(path);
			return new ResponseEntity<>(fromEntity(file), OK);
		} catch (FileNotFoundException e) {
			throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
		}
	}

	@GetMapping("/information-all")
	public ResponseEntity<Set<FileInformation>> getAllFiles() {
		return new ResponseEntity<>(fromEntitySet(fileInformationService.getAll()), OK);
	}

}
