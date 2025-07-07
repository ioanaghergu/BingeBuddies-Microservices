package org.market.movieclubsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.market.movieclubsservice.domain.MovieClub;
import org.market.movieclubsservice.dto.MovieClubDTO;
import org.market.movieclubsservice.dto.UserDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ClubSettingsMapper.class, ScreeningEventMapper.class})
public interface MovieClubMapper {

    @Mapping(target = "settings", source = "settings")
    @Mapping(target = "adminId", source = "adminId")
    @Mapping(target = "memberIds", expression = "java(mapUserDTOsToIds(movieClubDTO.getMembers()))")
    @Mapping(target = "screeningEvents", ignore = true)
    MovieClub toMovieClub(MovieClubDTO movieClubDTO);

    @Mapping(source = "settings", target = "settings")
    @Mapping(source = "adminId", target = "adminId")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "screeningEvents", source = "screeningEvents")
    MovieClubDTO toMovieClubDTO(MovieClub movieClub);

    List<MovieClubDTO> toMovieClubDTOList(List<MovieClub> movieClubs);

    default Set<Long> mapUserDTOsToIds(Set<UserDTO> userDTOS) {
        if (userDTOS == null) {
            return null;
        }
        return userDTOS.stream()
                .map(UserDTO::getId)
                .collect(Collectors.toSet());
    }
}
