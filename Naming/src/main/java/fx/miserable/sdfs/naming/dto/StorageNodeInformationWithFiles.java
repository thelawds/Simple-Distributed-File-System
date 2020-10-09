package fx.miserable.sdfs.naming.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fx.miserable.sdfs.naming.domain.NodeState;
import fx.miserable.sdfs.naming.domain.StorageNodeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageNodeInformationWithFiles {

	@JsonProperty("stored_files")
	Set<FileInformation> storedFiles;

	@JsonProperty("name")
	private String name;

	@JsonProperty("address")
	private String address;

	@JsonProperty("state")
	private NodeState state;

	public static StorageNodeInformationWithFiles fromEntity(StorageNodeEntity entity) {
		return StorageNodeInformationWithFiles.builder()
											  .name(entity.getName())
											  .address(entity.getAddress())
											  .state(entity.getState())
											  .storedFiles(FileInformation.fromEntitySet(entity.getStoredFiles()))
											  .build();
	}

	public static List<StorageNodeInformationWithFiles> fromEntities(Collection<StorageNodeEntity> entities) {
		return entities.stream()
					   .map(StorageNodeInformationWithFiles::fromEntity)
					   .collect(Collectors.toList());
	}

}
