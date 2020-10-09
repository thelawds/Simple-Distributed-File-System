package fx.miserable.sdfs.naming.service;

import fx.miserable.sdfs.naming.domain.FileEntity;
import fx.miserable.sdfs.naming.dto.request.FileUpdateRequest;
import fx.miserable.sdfs.naming.exception.StorageServerNotFoundException;
import fx.miserable.sdfs.naming.repository.FileInformationRepository;
import fx.miserable.sdfs.naming.repository.StorageNodeInformationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

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

}
