package fx.miserable.sdfs.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
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

}
