package org.market.userservice.service.security;

import org.market.userservice.domain.security.Authority;

public interface AuthorityService {
    Authority getAuthorityByRole(String role);
}
