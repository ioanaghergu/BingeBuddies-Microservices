package org.market.userservice.service.impl;

import org.market.userservice.domain.security.Authority;
import org.market.userservice.repository.security.AuthorityRepository;
import org.market.userservice.service.security.AuthorityService;
import org.springframework.stereotype.Service;

@Service
public class AuthorityServiceImpl implements AuthorityService {
    private final AuthorityRepository authorityRepository;
    public AuthorityServiceImpl(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Override
    public Authority getAuthorityByRole(String role) {
        return authorityRepository.findAuthorityByRole(role);
    }
}
