package fx.miserable.sdfs.storage.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

	@Bean
	public RestTemplate namingServerRestTemplate(){
		return new RestTemplate();
	}

	@Bean
	public RestTemplate storageServerRestTemplate(){
		return new RestTemplate();
	}

}
