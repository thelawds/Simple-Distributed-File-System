package fx.miserable.sdfs.naming.service;

import fx.miserable.sdfs.naming.domain.FileEntity;
import fx.miserable.sdfs.naming.domain.NodeState;
import fx.miserable.sdfs.naming.dto.FileOrDirectoryInformation;
import fx.miserable.sdfs.naming.dto.request.FileUpdateRequest;
import fx.miserable.sdfs.naming.exception.StorageServerNotFoundException;
import fx.miserable.sdfs.naming.repository.FileInformationRepository;
import fx.miserable.sdfs.naming.repository.StorageNodeInformationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileInformationService {

	private final FileInformationRepository fileInformationRepository;
	private final StorageNodeInformationRepository storageNodeInformationRepository;

	public FileEntity getFileByPath(String path) throws FileNotFoundException {
		return fileInformationRepository.findByPath(path).orElseThrow(
				FileNotFoundException::new
		);
	}

	public Set<FileEntity> getAll() {
		return new HashSet<>(fileInformationRepository.findAll());
	}

	public List<FileEntity> getAllWithOneOnlineStorageNode() {
		return storageNodeInformationRepository.findAllOffline().stream()
											   .filter(this::hasNotEnoughOnlineReplicas)
											   .collect(Collectors.toList());
	}

	private boolean hasNotEnoughOnlineReplicas(FileEntity el) {
		return el.getStorageNodes().stream()
				 .filter(sn -> sn.getState() == NodeState.OFFLINE)
				 .count() == 1;
	}

	public FileEntity saveFileByDto(FileUpdateRequest fileUpdateRequest) {
		var entity = fileInformationRepository.findByPath(fileUpdateRequest.getPath())
											  .orElseGet(FileEntity::new);

		var node = storageNodeInformationRepository
				.findByName(fileUpdateRequest.getStorageNodeName())
				.orElseThrow(() -> new StorageServerNotFoundException(format(
						"Storage Server was not found by name %s.",
						fileUpdateRequest.getStorageNodeName()
				)));

		this.updateEntityByDto(entity, fileUpdateRequest);


		entity.getStorageNodes().add(node);

		return fileInformationRepository.save(entity);
	}

	private void updateEntityByDto(FileEntity entity, FileUpdateRequest fileUpdateRequest) {
		entity.setPath(fileUpdateRequest.getPath());
		entity.setSize(fileUpdateRequest.getSize());
		entity.setReadable(fileUpdateRequest.isReadable());
		entity.setWritable(fileUpdateRequest.isWritable());
		entity.setExecutable(fileUpdateRequest.isExecutable());
		entity.setLastUpdate(fileUpdateRequest.getLastUpdate());
	}

	// TODO: refactor
	// TODO: bug with recursive counting of files
	public List<FileOrDirectoryInformation> getFilesByDirectory(String dir) {
		log.info("Searching Files by Directory: {}", dir);

		var directories = new HashMap<String, FileOrDirectoryInformation>();

		log.info("Searching Files by Directory: {}. Files fetched: {}", dir, fileInformationRepository
				.findByDirectory(dir));


		var files = fileInformationRepository.findByDirectory(dir).stream()
											 .map((el) -> getFileOrDirectoryInformation(directories, el, dir))
											 .filter(Objects::nonNull).collect(Collectors.toList());

		files.addAll(directories.values());

		log.info("Resulting files for directory {} are {}", dir, files);

		return files;
	}

	// TODO: Refactor
	private FileOrDirectoryInformation getFileOrDirectoryInformation(HashMap<String, FileOrDirectoryInformation> directories, FileEntity el, String basedir) {
		var fileOrDir = FileOrDirectoryInformation.fromEntity(basedir, el);

		log.info("Mapped file or directory with path {} to {}", el.getPath(), fileOrDir);

		if (fileOrDir.isDirectory()) {

			if (directories.containsKey(fileOrDir.getName())) {
				var storedFileOrDir = directories.get(fileOrDir.getName());
				storedFileOrDir.setFilesCount(storedFileOrDir.getFilesCount() + 1);
				storedFileOrDir
						.setSize(storedFileOrDir.getSize().add(BigInteger.valueOf(el.getSize())));
				directories.replace(fileOrDir.getName(), storedFileOrDir);
			} else {
				directories.put(fileOrDir.getName(), fileOrDir);
			}

			return null;
		}

		return fileOrDir;
	}

	// TODO: REFACTOR!!!!!!!!
	public void delete(String path) {
		var entity = fileInformationRepository.findByPath(path).orElseGet(FileEntity::new);

		var rt = new RestTemplate();

		entity.getStorageNodes().forEach(el -> {
			var uri = UriComponentsBuilder.fromUriString(el.getAddress() + "/file/delete")
										  .queryParam("path", path)
										  .build().toUriString();

			try {
				rt.exchange(
						uri,
						HttpMethod.DELETE,
						new HttpEntity<>(new HttpHeaders()),
						String.class
				);
			} catch (Exception e) {
				log.error(
						"Deletion of {} on node {} was not successful. Error {}.",
						entity.getPath(), el.getAddress(), e.getMessage()
				);
			}

			fileInformationRepository.delete(entity);
		});

	}

}
