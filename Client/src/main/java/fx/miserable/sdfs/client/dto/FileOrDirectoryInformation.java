package fx.miserable.sdfs.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOrDirectoryInformation {

	@JsonProperty("name")
	private String name;

	@JsonProperty("directory")
	private boolean directory;

	@JsonProperty("files_count")
	private Integer filesCount;

	@JsonProperty("size")
	private BigInteger size;
}
