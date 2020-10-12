package fx.miserable.sdfs.client.service;

import fx.miserable.sdfs.client.dto.FileInformation;
import fx.miserable.sdfs.client.dto.FileMetaData;
import fx.miserable.sdfs.client.dto.FileOrDirectoryInformation;
import fx.miserable.sdfs.client.dto.StorageNodeInformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Slf4j
@Service
@RequiredArgsConstructor
public class NamingNodeConnectionService {

	private final String namingNodeAddress = "http://35.181.4.165:8080";
	private final RestTemplate restTemplate = new RestTemplate();

	public List<FileInformation> getAll() {
		var uri = namingNodeAddress + "/file/information-all";

		var res = restTemplate.exchange(
				uri,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				FileInformation[].class
		);

		return Arrays.asList(Objects.requireNonNull(res.getBody()));
	}

	public void createFile(String fullpath) {

		try {
			var url = UriComponentsBuilder.fromUriString(namingNodeAddress + "/storage/available")
										  .queryParam("available_space", 0).build().toUriString();


			var storageNode = restTemplate.exchange(
					url,
					HttpMethod.GET,
					new HttpEntity<>(new HttpHeaders()),
					StorageNodeInformation.class
			).getBody();

			var storageUrl = UriComponentsBuilder
					.fromUriString(storageNode.getAddress() + "/file/create")
					.build().toUriString();

			restTemplate.exchange(
					storageUrl,
					HttpMethod.POST,
					new HttpEntity<>(FileMetaData.builder()
												 .path(fullpath)
												 .createReplica(true)
												 .build()),
					FileMetaData.class
			);
		} catch (Exception e) {
			log.error("Creating file was unsuccessful. Exception {}. Retrying", e.getMessage());
			this.createFile(fullpath);
		}

	}

	public List<FileOrDirectoryInformation> getFilesAndFolders(String path) {
		var uri = UriComponentsBuilder.fromUriString(namingNodeAddress + "/file/information-dir")
									  .queryParam("dir", path)
									  .build().toUriString();

		return Arrays.asList(Objects.requireNonNull(restTemplate.exchange(
				uri,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				FileOrDirectoryInformation[].class
		).getBody()));
	}

	public void uploadFile(File file, String fullpath) {

		try {
			var url = UriComponentsBuilder.fromUriString(namingNodeAddress + "/storage/available")
										  .queryParam("available_space", file.length()).build()
										  .toUriString();

			var storageNode = restTemplate.exchange(
					url,
					HttpMethod.GET,
					new HttpEntity<>(new HttpHeaders()),
					StorageNodeInformation.class
			).getBody();

			var headers = new HttpHeaders();
			headers.setContentType(MULTIPART_FORM_DATA);

			var body = new LinkedMultiValueMap<>();
			body.add("file", new FileSystemResource(file));

			var storageUrl = UriComponentsBuilder
					.fromUriString(storageNode.getAddress() + "/file/upload")
					.queryParam("path", fullpath)
					.queryParam("create_replica", true)
					.build().encode().toUri();

			var res = restTemplate.exchange(
					storageUrl,
					HttpMethod.POST,
					new HttpEntity<>(body, headers),
					FileMetaData.class
			);

			log.info("File sent to {}", res.getBody().getNodeName());

		} catch (Exception e) {
			log.error("Uploading file was unsuccessful. Exception {}. Retrying", e.getMessage());
			this.uploadFile(file, fullpath);
		}

	}

	public void downloadFile(File file, String fullpath) {

		try {

			var url = UriComponentsBuilder.fromUriString(namingNodeAddress + "/storage/for-file")
										  .queryParam("path", fullpath).build().toUriString();

			var storageNode = restTemplate.exchange(
					url,
					HttpMethod.GET,
					new HttpEntity<>(new HttpHeaders()),
					StorageNodeInformation.class
			).getBody();

			var uri = UriComponentsBuilder
					.fromUriString(storageNode.getAddress() + "/file/download")
					.queryParam("path", fullpath)
					.build().toUriString();

			var is = restTemplate.exchange(
					uri,
					HttpMethod.GET,
					new HttpEntity<>(new HttpHeaders()),
					InputStreamResource.class
			).getBody().getInputStream();

			file.createNewFile();
			var os = new FileOutputStream(file);
			is.transferTo(os);

		} catch (Exception e) {
			log.error("Downloading file was unsuccessful. Exception {}. Retrying", e.getMessage());
			this.downloadFile(file, fullpath);
		}

	}

	public void deleteFile(String fullpath) {

		var url = UriComponentsBuilder.fromUriString(namingNodeAddress + "/file/delete")
									  .queryParam("path", fullpath).build().toUriString();

		restTemplate.exchange(
				url,
				HttpMethod.DELETE,
				new HttpEntity<>(new HttpHeaders()),
				FileInformation.class
		);

	}

	public void init() {

		log.info("Initializing SDFS. Pruning all storage nodes...");

		var url = UriComponentsBuilder.fromUriString(namingNodeAddress + "/storage/prune").build()
									  .toUriString();

		var size = restTemplate.exchange(
				url,
				HttpMethod.DELETE,
				new HttpEntity<>(new HttpHeaders()),
				Long.class
		).getBody();

		log.info("SDFS Initialized. Available size: {}", size);

	}

	public void info(String fullpath) {

		var url = UriComponentsBuilder.fromUriString(namingNodeAddress + "/file/information")
									  .queryParam("path", fullpath).build().toUriString();

		var res = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				FileInformation.class
		).getBody();

		log.info("File {} with size {} KB and stored in <{}>.", res.getPath(), res.getSize() / 1024, String.join(", ", res.getNodeNames()));

	}
}
