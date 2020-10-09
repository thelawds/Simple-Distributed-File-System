package fx.miserable.sdfs.naming.repository;

import fx.miserable.sdfs.naming.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileInformationRepository extends JpaRepository<FileEntity, Integer> {

	Optional<FileEntity> findByPath(String path);

	Boolean existsByPath(String path);


	@Query(
			nativeQuery = true,
			value = "SELECT fi FROM " +
					"	file_information fi, " +
					"WHERE COUNT(" +
					"	SELECT sn FROM storage_node sn, file_information_to_storage_node fits" +
					"	WHERE fits.name = sn.name and fits.id = fi.id" +
					") = 1"
	)
	List<FileEntity> getAllWithOneOnlineServer();
}
