package fx.miserable.sdfs.storage.service;

import fx.miserable.sdfs.storage.dto.FileMetaData;
import fx.miserable.sdfs.storage.settings.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FileStorageService {

	private final ApplicationProperties applicationProperties;

	public File createFile(FileMetaData metadata) throws IOException {
		var fullPath = this.getFullPath(metadata.getPath());
		this.createDirs(fullPath);

		var file = new File(fullPath);
		this.updateFile(metadata, file);
		file.createNewFile();

		return new File(fullPath);
	}

	public File saveFile(FileMetaData metadata, MultipartFile multipartFile) throws IOException {
		var file = this.createFile(metadata);
		multipartFile.transferTo(file);

		return new File(this.getFullPath(metadata.getPath()));
	}

	private void updateFile(FileMetaData metadata, File file) throws IOException {
		var lastUpdate = metadata.getLastUpdate() == null
						 ? Instant.now().getEpochSecond()
						 : metadata.getLastUpdate();

		file.setExecutable(metadata.isExecutable());
		file.setReadable(metadata.isReadable());
		file.setReadable(metadata.isReadable());
		file.setLastModified(lastUpdate);
	}

	public void deleteFileByPath(String path) throws FileNotFoundException {
		var fullPath = getFullPath(path);
		var file = new File(fullPath);
		var deleted = file.delete();

		if (!deleted) {
			throw new FileNotFoundException("File was not found by path " + path);
		}
	}

	public void createDirs(String fullPath) {
		var pathEnd = fullPath.lastIndexOf("/");
		var path = fullPath.substring(0, pathEnd);
		var pathFile = new File(path);
		pathFile.mkdirs();
	}

	public Long prune() {
		var baseDir = new File(applicationProperties.getBaseDirectory());
		FileSystemUtils.deleteRecursively(baseDir);
		baseDir.mkdirs();
		return baseDir.getFreeSpace();
	}

	public Long getAvailableSpace() {
		return (new File(applicationProperties.getBaseDirectory())).getFreeSpace();
	}

	public File getByPath(String path) {
		return new File(this.getFullPath(path));
	}

	private String getFullPath(String path) {
		return applicationProperties.getBaseDirectory() + path;
	}


}
