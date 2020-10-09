package fx.miserable.sdfs.storage;

import fx.miserable.sdfs.storage.service.NamingNodeConnectionService;
import fx.miserable.sdfs.storage.settings.ApplicationProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AllArgsConstructor
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class StorageApplication {
	private final NamingNodeConnectionService namingNodeConnectionService;

	public static void main(String[] args) {
		SpringApplication.run(StorageApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(){
		return (args) -> {
			namingNodeConnectionService.initialize();
		};
	}
}
