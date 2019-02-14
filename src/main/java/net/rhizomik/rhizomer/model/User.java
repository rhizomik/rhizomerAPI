package net.rhizomik.rhizomer.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by http://rhizomik.net/~roberto/
 */
@Entity
@Table(name = "RhizomerUser")
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="username")
public class User implements UserDetails {

  public static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Id
  private String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotBlank
  @Length(min=8, max=256)
  private String password;

  public String getId() { return username; }

  @Override
  public String getUsername(){
    return username;
  }

  public void setUsername(String username) { this.username = username; }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) { this.password = password; }

  public void encodePassword() {
    this.password = passwordEncoder.encode(this.password);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
  }

  @Override
  public boolean isAccountNonExpired() { return true; }

  @Override
  public boolean isAccountNonLocked() { return true; }

  @Override
  public boolean isCredentialsNonExpired() { return true; }

  @Override
  public boolean isEnabled() { return true; }
}
