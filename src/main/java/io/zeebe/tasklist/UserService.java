package io.zeebe.tasklist;

import io.zeebe.tasklist.entity.UserEntity;
import io.zeebe.tasklist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserService implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder bCryptPasswordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("no user found with name: " + username));
  }

  public boolean hasUserWithName(String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  public UserEntity newUser(String username, String password) {

    final UserEntity user = new UserEntity();
    user.setUsername(username);

    final String encodedPassword = bCryptPasswordEncoder.encode(password);
    user.setPassword(encodedPassword);

    return userRepository.save(user);
  }

  public void newAdminUser(String username, String password) {

    final UserEntity user = newUser(username, password);
    user.setRoles(Roles.ADMIN.name());

    userRepository.save(user);
  }
}
