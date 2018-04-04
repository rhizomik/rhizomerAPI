package net.rhizomik.rhizomer.config;

import net.rhizomik.rhizomer.model.User;
import net.rhizomik.rhizomer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;

@Configuration
public class AuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {

  @Value("${default-password}")
  String defaultPassword;

  @Autowired
  BasicUserDetailsService basicUserDetailsService;

  @Autowired
  UserRepository userRepository;

  @Override
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth
        .userDetailsService(basicUserDetailsService)
        .passwordEncoder(User.passwordEncoder);

    if (!userRepository.exists("user")) {
      User user = new User();
      user.setUsername("user");
      user.setPassword(defaultPassword);
      user.encodePassword();
      userRepository.save(user);
    }
  }
}