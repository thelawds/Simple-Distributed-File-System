package fx.miserable.sdfs.storage.service;

import fx.miserable.sdfs.storage.dto.FileMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageNodeConnectionService {

	private final NamingNodeConnectionService namingNodeConnectionService;
	private final RestTemplate storageServerRestTemplate;

	public void createOnReplica(FileMetaData metadata) {
		var replicationServer = namingNodeConnectionService.getAvailableNode(0L);
		metadata.setCreateReplica(false);

		try {
			var res = storageServerRestTemplate.exchange(
					replicationServer.getAddress() + "/file/create",
					HttpMethod.POST,
					new HttpEntity<>(metadata),
					FileMetaData.class
			);
		} catch (Exception e) {
			log.error(
					"Was not able to create file on replica {}, error: {}.",
					replicationServer.toString(), e.getMessage()
			);
			this.createOnReplica(metadata);
		}

	}

	public void uploadOnReplica(FileMetaData metadata, File file) {
		var replicationServer = namingNodeConnectionService.getAvailableNode(metadata.getSize());
		metadata.setCreateReplica(false);

		var headers = new HttpHeaders();
		headers.setContentType(MULTIPART_FORM_DATA);

		var body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(file));

		var lastUpdate = metadata.getLastUpdate() == null ? -1 : metadata.getLastUpdate();
		var uri = UriComponentsBuilder
				.fromUriString(replicationServer.getAddress() + "/file/upload")
				.queryParam("path", metadata.getPath())
				.queryParam("create_replica", metadata.isCreateReplica())
				.queryParam("executable", metadata.isExecutable())
				.queryParam("readable", metadata.isReadable())
				.queryParam("writable", metadata.isWritable())
				.queryParam("last_update", lastUpdate)
				.build().encode().toUri();

		try {
			var res = storageServerRestTemplate.exchange(
					uri,
					HttpMethod.POST,
					new HttpEntity<>(body, headers),
					FileMetaData.class
			);
		} catch (Exception e) {
			log.error(
					"Was not able to upload file to replica {}, exception: {}.",
					replicationServer.toString(), e.getMessage()
			);
			this.uploadOnReplica(metadata, file);
		}

	}
}
