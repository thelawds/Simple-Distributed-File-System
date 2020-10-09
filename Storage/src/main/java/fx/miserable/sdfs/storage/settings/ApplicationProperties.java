package fx.miserable.sdfs.storage.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

import static java.util.Objects.requireNonNull;


@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

	private final Environment environment;

	private String nameNodeAddress;
	private String baseDirectory;
	private String address;
	private String nodeName;

	public String getAddress() {
		if (this.address == null) {
			try {
				var address = InetAddress.getLocalHost().getHostAddress();
				var port = requireNonNull(environment.getProperty("server.port"));
				this.address = "http://".concat(address).concat(":").concat(port);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return this.address;
	}

}
