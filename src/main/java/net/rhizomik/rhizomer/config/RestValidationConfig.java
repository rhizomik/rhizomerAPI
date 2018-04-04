package net.rhizomik.rhizomer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class RestValidationConfig extends RepositoryRestConfigurerAdapter {

  @Bean
  @Primary
  LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }

  @Override
  public void configureValidatingRepositoryEventListener(
      ValidatingRepositoryEventListener validatingListener) {
    validatingListener.addValidator("beforeCreate", validator());
    validatingListener.addValidator("beforeSave", validator());
    super.configureValidatingRepositoryEventListener(validatingListener);
  }
}