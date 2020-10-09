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
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageNodeInformation {

	@JsonProperty("name")
	private String name;

	@JsonProperty("address")
	private String address;

	@JsonProperty("state")
	private NodeState state;

	@JsonProperty("free_space")
	private Long freeSpace;

	public static StorageNodeInformation fromEntity(StorageNodeEntity entity) {
		return StorageNodeInformation.builder()
									 .name(entity.getName())
									 .address(entity.getAddress())
									 .state(entity.getState())
									 .freeSpace(entity.getFreeSpace())
									 .build();
	}

	public static List<StorageNodeInformation> fromEntities(Collection<StorageNodeEntity> entities) {
		return entities.stream()
					   .map(StorageNodeInformation::fromEntity)
					   .collect(Collectors.toList());
	}

}
