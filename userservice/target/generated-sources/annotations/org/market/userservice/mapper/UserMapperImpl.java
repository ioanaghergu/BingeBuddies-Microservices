package org.market.userservice.mapper;

import javax.annotation.processing.Generated;
import org.market.userservice.domain.security.User;
import org.market.userservice.dto.UserDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:38:52+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 23.0.2 (Azul Systems, Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDTO.getId() );
        user.keycloakId( userDTO.getKeycloakId() );
        user.username( userDTO.getUsername() );

        return user.build();
    }

    @Override
    public UserDTO toUserDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.id( user.getId() );
        userDTO.keycloakId( user.getKeycloakId() );
        userDTO.username( user.getUsername() );

        return userDTO.build();
    }
}
