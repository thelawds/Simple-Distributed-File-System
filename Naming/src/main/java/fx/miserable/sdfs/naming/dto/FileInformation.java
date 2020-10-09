package fx.miserable.sdfs.naming.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fx.miserable.sdfs.naming.domain.FileEntity;
import fx.miserable.sdfs.naming.domain.StorageNodeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInformation {

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("size")
	private Long size;

	@JsonProperty("path")
	private String path;

	@JsonProperty("executable")
	private boolean executable;

	@JsonProperty("readable")
	private boolean readable;

	@JsonProperty("writable")
	private boolean writable;

	@JsonProperty("last_update")
	private Long lastUpdate;

	@JsonProperty("nodes_names")
	private List<String> nodeNames;

	public static FileInformation fromEntity(FileEntity entity) {
		var storageNodes = entity.getStorageNodes().stream()
								 .map(StorageNodeEntity::getName)
								 .collect(Collectors.toList());

		return FileInformation.builder()
							  .id(entity.getId())
							  .size(entity.getSize())
							  .path(entity.getPath())
							  .executable(entity.isExecutable())
							  .readable(entity.isReadable())
							  .writable(entity.isWritable())
							  .lastUpdate(entity.getLastUpdate())
							  .nodeNames(storageNodes)
							  .build();
	}

	public static Set<FileInformation> fromEntitySet(Set<FileEntity> entities) {
		return entities.stream()
					   .map(FileInformation::fromEntity)
					   .collect(Collectors.toSet());
	}

}
