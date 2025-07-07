package org.market.movieclubsservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.market.movieclubsservice.domain.ScreeningEvent;
import org.market.movieclubsservice.dto.ScreeningEventDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScreeningEventMapper {

    @Mapping(target = "movieClub", ignore = true)
    ScreeningEvent toScreeningEvent(ScreeningEventDTO screeningEventDTO);

    @Mapping(source = "movieClub.id", target = "movieClubId")
    @Mapping(source = "movieId", target = "movieId")
    @Mapping(target = "movieTitle", ignore = true)
    ScreeningEventDTO toScreeningEventDTO(ScreeningEvent screeningEvent);

    List<ScreeningEventDTO> toScreeningEventDTOList(List<ScreeningEvent> screeningEventList);
}
