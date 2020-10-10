package fx.miserable.sdfs.naming.repository;

import fx.miserable.sdfs.naming.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileInformationRepository extends JpaRepository<FileEntity, Integer> {

	Optional<FileEntity> findByPath(String path);

	Boolean existsByPath(String path);

}
