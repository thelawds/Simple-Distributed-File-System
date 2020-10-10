package fx.miserable.sdfs.naming.repository;

import fx.miserable.sdfs.naming.domain.FileEntity;
import fx.miserable.sdfs.naming.domain.StorageNodeEntity;
import fx.miserable.sdfs.naming.dto.FileInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface StorageNodeInformationRepository extends JpaRepository<StorageNodeEntity, String> {

	Optional<StorageNodeEntity> findByName(String name);

	@Query(value = "select min(sne.freeSpace) from StorageNodeEntity sne")
	Long getAvailableSize();

	@Query(value = "select distinct sne.storedFiles from StorageNodeEntity sne where sne.state = 0")
	List<FileEntity> findAllOffline();

	@Modifying
	@Transactional
	@Query(value = "delete from StorageNodeEntity where state = 0")
	void dropAllOffline();
}
