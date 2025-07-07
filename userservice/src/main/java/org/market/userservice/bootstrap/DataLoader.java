package org.market.userservice.bootstrap;

import lombok.AllArgsConstructor;
import org.market.userservice.domain.security.Authority;
import org.market.userservice.domain.security.User;
import org.market.userservice.repository.security.AuthorityRepository;
import org.market.userservice.repository.security.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@AllArgsConstructor
@Component
@Profile({"mysql", "h2"})
public class DataLoader implements CommandLineRunner {
    private AuthorityRepository authorityRepository;
    private UserRepository userRepository;

    private void loadUserData() {
        if(userRepository.count() == 0) {
            Authority adminRole = authorityRepository.save(Authority.builder().role("ADMIN").build());
            Authority userRole = authorityRepository.save(Authority.builder().role("USER").build());

            User admin = User.builder()
                    .username("admin")
                    .authorities(Set.of(adminRole, userRole))
                    .build();

            User user = User.builder()
                    .username("user")
                    .authorities(Set.of(userRole))
                    .build();

            userRepository.save(admin);
            userRepository.save(user);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        loadUserData();
    }
}
