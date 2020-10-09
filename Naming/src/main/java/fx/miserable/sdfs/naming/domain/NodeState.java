package fx.miserable.sdfs.naming.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NodeState {

	@JsonProperty("ONLINE")
	ONLINE,

	@JsonProperty("OFFLINE")
	OFFLINE;

}