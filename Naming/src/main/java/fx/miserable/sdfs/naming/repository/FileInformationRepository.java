package fx.miserable.sdfs.naming.repository;

import fx.miserable.sdfs.naming.domain.FileEntity;
import fx.miserable.sdfs.naming.dto.FileInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileInformationRepository extends JpaRepository<FileEntity, Integer> {

	Optional<FileEntity> findByPath(String path);

	Boolean existsByPath(String path);

	@Query(
			value = "select fe " +
					"from FileEntity fe " +
					"where substring(fe.path, 1, length(:dir)) = :dir "
	)
	List<FileEntity> findByDirectory(@Param("dir") String dir);

}
