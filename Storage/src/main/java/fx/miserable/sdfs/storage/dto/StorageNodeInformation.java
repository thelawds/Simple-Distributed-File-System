package fx.miserable.sdfs.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	@Builder.Default
	private String state = "ONLINE";

	@JsonProperty("free_space")
	private Long freeSpace;
}
