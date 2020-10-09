package fx.miserable.sdfs.naming.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class StorageNodeBackgroundService {

	private final FileInformationService fileInformationService;
	private final StorageNodeInformationService storageNodeInformationService;

	@Scheduled(fixedDelay = 60_000L)
	public void healthCheck() {
		var storage = storageNodeInformationService.getAll()
												   .stream()
												   .map(storageNodeInformationService::checkHealth);

	}

}
