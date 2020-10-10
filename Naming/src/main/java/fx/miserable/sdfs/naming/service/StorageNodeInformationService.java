package fx.miserable.sdfs.naming.service;

import fx.miserable.sdfs.naming.domain.FileEntity;
import fx.miserable.sdfs.naming.domain.StorageNodeEntity;
import fx.miserable.sdfs.naming.dto.FileInformation;
import fx.miserable.sdfs.naming.dto.request.FileUpdateRequest;
import fx.miserable.sdfs.naming.exception.NotEnoughStorageServersException;
import fx.miserable.sdfs.naming.repository.FileInformationRepository;
import fx.miserable.sdfs.naming.repository.StorageNodeInformationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static fx.miserable.sdfs.naming.domain.NodeState.OFFLINE;
import static fx.miserable.sdfs.naming.domain.NodeState.ONLINE;
import static fx.miserable.sdfs.naming.service.RandomService.getAny;
import static java.lang.String.format;
import static org.springframework.http.HttpMethod.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageNodeInformationService {

	private final StorageNodeInformationRepository repository;
	private final FileInformationRepository fileInformationRepository;
	private final RestTemplate storageNodeRestTemplate;

	public StorageNodeEntity submit(StorageNodeEntity storageNodeEntity) {
		var entity = repository.findByName(storageNodeEntity.getName())
							   .orElseGet(StorageNodeEntity::new);

		this.update(entity, storageNodeEntity);
		return repository.save(entity);
	}

	private void update(StorageNodeEntity entity, StorageNodeEntity updater) {
		entity.setAddress(updater.getAddress());
		entity.setState(updater.getState());
		entity.setFreeSpace(updater.getFreeSpace());
	}

	public List<StorageNodeEntity> getAll() {
		return repository.findAll();
	}

	public StorageNodeEntity getAvailableStorageNode(String address, Long availableSpace) {
		log.warn("In getAvailableStorageNode with address {} and availableSpace {}", address, availableSpace); // todo
		var nodes = repository.findAll();
		var availabilityThreshold = address == null ? 2 : 1;

		var nodesStream = nodes.stream().filter(
				el -> el.getFreeSpace() > availableSpace && el.getState() == ONLINE
		);

		if (address != null) {
			nodes = nodesStream.filter(el -> !el.getAddress().equals(address))
							   .collect(Collectors.toList());
		}

		if (nodes.size() < availabilityThreshold) {
			throw new NotEnoughStorageServersException(
					format("Storage node not found for address != %s", address)
			);
		}

		return (StorageNodeEntity) getAny(nodes);
	}

	public void updateAvailableSizeByFileUpdateRequest(FileUpdateRequest fileUpdateRequest) {
		var storageNode = repository.findByName(fileUpdateRequest.getStorageNodeName())
									.orElseThrow(() -> new NotEnoughStorageServersException(
											format(
													"Storage node not found for name = %s",
													fileUpdateRequest.getStorageNodeName()
											)
									));

		storageNode.setFreeSpace(storageNode.getFreeSpace() - fileUpdateRequest.getSize());
		repository.save(storageNode);
	}

	public void pruneAll() {
		repository.findAll().forEach(this::prune);
	}

	private void prune(StorageNodeEntity storageNodeEntity) {
		try {
			var freeSpace = storageNodeRestTemplate.exchange(
					storageNodeEntity.getAddress() + "/file/prune",
					DELETE,
					new HttpEntity<>(new HttpHeaders()),
					Long.class
			).getBody();

			storageNodeEntity.setStoredFiles(new HashSet<>());
			storageNodeEntity.setFreeSpace(freeSpace);
			repository.save(storageNodeEntity);
		} catch (Exception e) {
			log.error("Exception in pruning: {}", e.getMessage());
		}
	}

	public Long getAvailableSize() {
		return repository.getAvailableSize();
	}

	public StorageNodeEntity checkHealth(StorageNodeEntity el) {
		log.info("Running Health Check for {} with address {}", el.getName(), el.getAddress());

		var address = el.getAddress();
		var updated = false;

		try {
			var newFreeSpace = storageNodeRestTemplate.exchange(
					address + "/health/check",
					GET,
					new HttpEntity<>(new HttpHeaders()),
					Long.class
			).getBody();

			if (!el.getFreeSpace().equals(newFreeSpace)) {
				el.setFreeSpace(newFreeSpace);
				updated = true;
			}
		} catch (Exception e) {
			el.setState(OFFLINE);
			updated = true;
		}

		log.info(
				"Health check for {} is finished. Storage node free space is {} updated.",
				el.getName(), updated ? "" : "not"
		);

		return updated ? repository.save(el) : el;
	}

	public void replicateFile(FileEntity fileEntity) {
		var storageAddress = this.getStoringNode(fileEntity).getAddress();
		var replicationAddress = this.getAvailableStorageNode(storageAddress, fileEntity.getSize());

		log.info(
				"Replicating file with path {} from {} to {}",
				fileEntity.getPath(), storageAddress, replicationAddress.getAddress()
		);

		var uri = UriComponentsBuilder.fromUriString(storageAddress + "/file/replicate")
									  .queryParam("path", fileEntity.getPath())
									  .queryParam("executable", fileEntity.isExecutable())
									  .queryParam("readable", fileEntity.isReadable())
									  .queryParam("writable", fileEntity.isWritable())
									  .queryParam("last_update", fileEntity.getLastUpdate())
									  .queryParam("replica_address", replicationAddress)
									  .build().encode().toUri();

		storageNodeRestTemplate.exchange(
				uri,
				POST,
				new HttpEntity<>(new HttpHeaders()),
				Void.class
		);
	}

	private StorageNodeEntity getStoringNode(FileEntity fileEntity) {
		return fileEntity.getStorageNodes().stream().filter(el -> el.getState() == ONLINE)
						 .findFirst().orElseThrow(() -> new NotEnoughStorageServersException(format(
						"There are no storage servers currecntly available for file %s",
						FileInformation.fromEntity(fileEntity).toString()
				)));
	}


	public void dropAllOffline() {
		repository.dropAllOffline();
	}
}
