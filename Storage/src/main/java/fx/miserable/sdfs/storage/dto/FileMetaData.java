package fx.miserable.sdfs.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

	public static FileMetaData fromFile(File file, String path) {
		var size = file.length();
		var executable = file.canExecute();
		var readable = file.canRead();
		var writable = file.canWrite();
		var lastUpdate = file.lastModified();

		return FileMetaData.builder()
						   .path(path)
						   .size(size)
						   .executable(executable)
						   .readable(readable)
						   .writable(writable)
						   .lastUpdate(lastUpdate)
						   .createReplica(false)
						   .build();
	}

}
