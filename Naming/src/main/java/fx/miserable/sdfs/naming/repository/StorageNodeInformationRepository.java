package fx.miserable.sdfs.naming.repository;

import fx.miserable.sdfs.naming.domain.StorageNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StorageNodeInformationRepository extends JpaRepository<StorageNodeEntity, String> {

	Optional<StorageNodeEntity> findByName(String name);

	@Query(value = "select min(sne.freeSpace) from StorageNodeEntity sne")
	Long getAvailableSize();
}
