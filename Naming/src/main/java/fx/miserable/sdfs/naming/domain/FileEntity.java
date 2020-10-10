package fx.miserable.sdfs.naming.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_information")
public class FileEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_id_sequence_generator")
	private Integer id;

	@JsonProperty("size")
	private Long size;

	@Column(name = "path", unique = true)
	private String path;

	@JsonProperty("executable")
	private boolean executable;

	@JsonProperty("readable")
	private boolean readable;

	@JsonProperty("writable")
	private boolean writable;

	@JsonProperty("last_update")
	private Long lastUpdate;

	@ManyToMany(cascade = ALL, fetch = EAGER)
	@JoinTable(
			name = "file_information_to_storage_node",
			joinColumns = @JoinColumn(name = "id"),
			inverseJoinColumns = @JoinColumn(name = "name")
	)
	private Set<StorageNodeEntity> storageNodes = new HashSet<>();

}
