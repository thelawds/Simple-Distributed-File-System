package fx.miserable.sdfs.storage.controller;

import fx.miserable.sdfs.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

	private final FileStorageService fileStorageService;

	@GetMapping("/check")
	public ResponseEntity<Long> checkHealth() {
		return new ResponseEntity<>(fileStorageService.getAvailableSpace(), OK);
	}

}
