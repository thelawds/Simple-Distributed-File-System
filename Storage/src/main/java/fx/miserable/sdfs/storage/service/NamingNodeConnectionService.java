package fx.miserable.sdfs.storage.service;

import fx.miserable.sdfs.storage.dto.FileMetaData;
import fx.miserable.sdfs.storage.dto.StorageNodeInformation;
import fx.miserable.sdfs.storage.settings.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class NamingNodeConnectionService {

	private final RestTemplate namingServerRestTemplate;
	private final ApplicationProperties applicationProperties;

	public void initialize() {
		var baseDir = new File(applicationProperties.getBaseDirectory());
		baseDir.mkdirs();

		log.info(
				"Initialization with address {} and free space {}",
				applicationProperties.getAddress(), baseDir.getFreeSpace()
		);

		var freeSpace = (long) Math.floor(baseDir.getFreeSpace() * .95);
		var request = StorageNodeInformation.builder()
											.address(applicationProperties.getAddress())
											.freeSpace(freeSpace)
											.build();

		log.info("Sending Request: {}", request);

		var url = applicationProperties.getNameNodeAddress().concat("/storage/initialize");
		var response = namingServerRestTemplate.exchange(
				url,
				HttpMethod.POST,
				new HttpEntity<>(request),
				StorageNodeInformation.class
		);

		applicationProperties.setNodeName(response.getBody().getName());
		log.info("Initialized with: {}", response.getBody().toString());
	}

	public StorageNodeInformation getAvailableNode(Long availableSpace) {
		var url = applicationProperties.getNameNodeAddress().concat("/storage/available");
		var uri = UriComponentsBuilder.fromUriString(url)
									  .queryParam("address", applicationProperties.getAddress())
									  .queryParam("available_space", availableSpace)
									  .build().toUriString();

		var response = namingServerRestTemplate.exchange(
				uri,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				StorageNodeInformation.class
		);

		return response.getBody();
	}

	public void updateFile(FileMetaData metadata) {
		var url = applicationProperties.getNameNodeAddress().concat("/file/update");
		metadata.setNodeName(applicationProperties.getNodeName());
		var res = namingServerRestTemplate.exchange(
				url,
				HttpMethod.POST,
				new HttpEntity<>(metadata),
				FileMetaData.class
		);
	}
}
