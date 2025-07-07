package org.market.userservice.repository.security;

import org.market.userservice.domain.security.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findAuthorityByRole(String role);
}
