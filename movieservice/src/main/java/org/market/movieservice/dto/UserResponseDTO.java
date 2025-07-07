package org.market.movieservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private Set<String> roles;
    private String instancePort;
}
