package fx.miserable.sdfs.naming.controller;

import fx.miserable.sdfs.naming.dto.StorageNodeInformation;
import fx.miserable.sdfs.naming.dto.StorageNodeInformationWithFiles;
import fx.miserable.sdfs.naming.exception.NotEnoughStorageServersException;
import fx.miserable.sdfs.naming.service.StorageNodeInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static fx.miserable.sdfs.naming.domain.StorageNodeEntity.fromStorageNodeInformation;
import static fx.miserable.sdfs.naming.dto.StorageNodeInformation.fromEntity;
import static fx.miserable.sdfs.naming.dto.StorageNodeInformationWithFiles.fromEntities;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {

	private final StorageNodeInformationService storageNodeInformationService;

	@PostMapping("/initialize")
	public ResponseEntity<StorageNodeInformation> createOrUpdateStorageNode(
			@RequestBody StorageNodeInformation request
	) {
		log.info("Initializing Storage Node {}.", request);
		var entity = storageNodeInformationService.submit(fromStorageNodeInformation(request));
		return new ResponseEntity<>(fromEntity(entity), OK);
	}

	@GetMapping("/all")
	public ResponseEntity<List<StorageNodeInformationWithFiles>> getAll() {
		return new ResponseEntity<>(
				fromEntities(storageNodeInformationService.getAll()), OK
		);
	}

	@GetMapping("/available")
	public ResponseEntity<StorageNodeInformation> getAvailableStorage(
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "available_space", required = true) Long availableSpace
	) {
		try {
			var response = storageNodeInformationService.getAvailableStorageNode(address, availableSpace);
			return new ResponseEntity<>(fromEntity(response), OK);
		} catch (NotEnoughStorageServersException e) {
			throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
		}
	}

	@DeleteMapping("/prune")
	public ResponseEntity<Long> pruneAllStorageNodes(){
			storageNodeInformationService.pruneAll();
			return new ResponseEntity<>(storageNodeInformationService.getAvailableSize(), OK);
	}

}
