package fx.miserable.sdfs.naming.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import fx.miserable.sdfs.naming.dto.StorageNodeInformation;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static fx.miserable.sdfs.naming.domain.NodeState.OFFLINE;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "storage_node")
public class StorageNodeEntity {

	@Id
	@Column(name = "name")
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid")
	private String name;

	@Column(name = "address", unique = true)
	private String address;

	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	private NodeState state = OFFLINE;

	@Column(name = "free_space")
	private Long freeSpace;

	@ManyToMany(cascade = ALL, fetch = LAZY)
	@JoinTable(
			name = "file_information_to_storage_node",
			joinColumns = @JoinColumn(name = "name"),
			inverseJoinColumns = @JoinColumn(name = "id")
	)
	private Set<FileEntity> storedFiles;

	public static StorageNodeEntity fromStorageNodeInformation(
			StorageNodeInformation request
	) {
		return StorageNodeEntity.builder()
								.name(request.getName())
								.address(request.getAddress())
								.state(request.getState())
								.freeSpace(request.getFreeSpace())
								.storedFiles(new HashSet<>())
								.build();
	}

}
