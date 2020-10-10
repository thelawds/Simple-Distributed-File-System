package fx.miserable.sdfs.naming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class StorageNodeBackgroundService {

	private final FileInformationService fileInformationService;
	private final StorageNodeInformationService storageNodeInformationService;

	@Scheduled(fixedDelay = 60_000L)
	public void healthCheck() {
		log.info("Started health check");

		storageNodeInformationService.getAll().forEach(storageNodeInformationService::checkHealth);
		fileInformationService.getAllWithOneOnlineStorageNode()
							  .forEach(storageNodeInformationService::replicateFile);
		storageNodeInformationService.dropAllOffline();

		log.info("Health check was successfully completed");
	}

}
