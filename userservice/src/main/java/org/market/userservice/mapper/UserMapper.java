package org.market.userservice.mapper;

import org.mapstruct.Mapper;
import org.market.userservice.domain.security.User;
import org.market.userservice.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDTO userDTO);
    UserDTO toUserDTO(User user);
}
