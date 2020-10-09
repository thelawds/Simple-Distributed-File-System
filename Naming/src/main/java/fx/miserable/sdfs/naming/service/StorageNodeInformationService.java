package fx.miserable.sdfs.naming.service;

import fx.miserable.sdfs.naming.domain.NodeState;
import fx.miserable.sdfs.naming.domain.StorageNodeEntity;
import fx.miserable.sdfs.naming.dto.request.FileUpdateRequest;
import fx.miserable.sdfs.naming.exception.NotEnoughStorageServersException;
import fx.miserable.sdfs.naming.repository.StorageNodeInformationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static fx.miserable.sdfs.naming.domain.NodeState.OFFLINE;
import static fx.miserable.sdfs.naming.service.RandomService.getAny;
import static java.lang.String.format;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageNodeInformationService {

	private final StorageNodeInformationRepository repository;
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
		var nodes = repository.findAll();
		var availabilityThreshold = address == null ? 2 : 1;

		var nodesStream = nodes.stream().filter(el -> el.getFreeSpace() > availableSpace);

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
		} catch (Exception e){
			log.error("Exception in pruning: {}", e.getMessage());
		}
	}

	public Long getAvailableSize() {
		return repository.getAvailableSize();
	}

	public StorageNodeEntity checkHealth(StorageNodeEntity el) {
		var address = el.getAddress();
		var updated = false;

		try {
			var newFreeSpace = storageNodeRestTemplate.exchange(
					address + "/health/check",
					GET,
					new HttpEntity<>(new HttpHeaders()),
					Long.class
			).getBody();

			if (el.getFreeSpace() != newFreeSpace){
				el.setFreeSpace(newFreeSpace);
				updated = true;
			}
		} catch (Exception e){
			el.setState(OFFLINE);
			updated = true;
		}

		return updated ? repository.save(el) : el;
	}
}
