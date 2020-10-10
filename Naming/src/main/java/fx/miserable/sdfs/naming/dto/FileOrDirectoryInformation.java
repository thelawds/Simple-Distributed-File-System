package fx.miserable.sdfs.naming.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fx.miserable.sdfs.naming.domain.FileEntity;
import lombok.*;

import java.math.BigInteger;

@Data
@Builder
public class FileOrDirectoryInformation {

	@JsonProperty("name")
	private String name;

	@JsonProperty("directory")
	private boolean directory;

	@JsonProperty("files_count")
	private Integer filesCount;

	@JsonProperty("size")
	private BigInteger size;

	public static FileOrDirectoryInformation buildDirectory(
			String name, BigInteger size, Integer filesCount
	) {
		return FileOrDirectoryInformation.builder()
										 .name(name)
										 .size(size)
										 .directory(true)
										 .filesCount(filesCount)
										 .build();
	}

	public static FileOrDirectoryInformation fromEntity(String basedir, FileEntity entity) {
		var name = entity.getPath().substring(basedir.length());

		if (name.contains("/")) {
			return buildDirectory(
					name.substring(0, name.indexOf("/")), BigInteger.valueOf(entity.getSize()), 1
			);
		}

		return FileOrDirectoryInformation.builder()
										 .name(name)
										 .size(BigInteger.valueOf(entity.getSize()))
										 .directory(false)
										 .filesCount(0)
										 .build();
	}
}
