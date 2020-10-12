package fx.miserable.sdfs.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaData {

	@JsonProperty("path")
	private String path;

	@JsonProperty("storage_node_name")
	private String nodeName;

	@JsonProperty("size")
	private Long size;

	@JsonProperty("create_replica")
	private boolean createReplica;

	@JsonProperty("executable")
	@Builder.Default
	private boolean executable = true;

	@JsonProperty("readable")
	@Builder.Default
	private boolean readable = true;

	@JsonProperty("writable")
	@Builder.Default
	private boolean writable = true;

	@JsonProperty("last_update")
	private Long lastUpdate;

}
