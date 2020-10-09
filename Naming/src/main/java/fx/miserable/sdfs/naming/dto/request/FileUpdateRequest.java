package fx.miserable.sdfs.naming.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdateRequest {

	@JsonProperty("path")
	private String path;

	@JsonProperty("storage_node_name")
	private String storageNodeName;

	@JsonProperty("size")
	private Long size;

	@JsonProperty("executable")
	private boolean executable;

	@JsonProperty("readable")
	private boolean readable;

	@JsonProperty("writable")
	private boolean writable;

	@JsonProperty("last_update")
	private long lastUpdate;

}
