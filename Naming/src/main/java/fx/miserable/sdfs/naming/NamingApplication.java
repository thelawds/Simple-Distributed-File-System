package fx.miserable.sdfs.naming;

import fx.miserable.sdfs.naming.settings.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class NamingApplication {

	public static void main(String[] args) {
		SpringApplication.run(NamingApplication.class, args);
	}

}
