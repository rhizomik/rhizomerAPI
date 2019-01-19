package net.rhizomik.rhizomer.model;

import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

@Entity
public class Admin extends User {

  @Override
  @Transient
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN");
  }

}
