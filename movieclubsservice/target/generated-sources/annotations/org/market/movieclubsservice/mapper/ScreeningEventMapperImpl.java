package org.market.movieclubsservice.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.market.movieclubsservice.domain.MovieClub;
import org.market.movieclubsservice.domain.ScreeningEvent;
import org.market.movieclubsservice.dto.ScreeningEventDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:52:46+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 23.0.2 (Azul Systems, Inc.)"
)
@Component
public class ScreeningEventMapperImpl implements ScreeningEventMapper {

    @Override
    public ScreeningEvent toScreeningEvent(ScreeningEventDTO screeningEventDTO) {
        if ( screeningEventDTO == null ) {
            return null;
        }

        ScreeningEvent screeningEvent = new ScreeningEvent();

        screeningEvent.setId( screeningEventDTO.getId() );
        screeningEvent.setDate( screeningEventDTO.getDate() );
        screeningEvent.setMovieId( screeningEventDTO.getMovieId() );

        return screeningEvent;
    }

    @Override
    public ScreeningEventDTO toScreeningEventDTO(ScreeningEvent screeningEvent) {
        if ( screeningEvent == null ) {
            return null;
        }

        ScreeningEventDTO.ScreeningEventDTOBuilder screeningEventDTO = ScreeningEventDTO.builder();

        screeningEventDTO.movieClubId( screeningEventMovieClubId( screeningEvent ) );
        screeningEventDTO.movieId( screeningEvent.getMovieId() );
        screeningEventDTO.id( screeningEvent.getId() );
        screeningEventDTO.date( screeningEvent.getDate() );

        return screeningEventDTO.build();
    }

    @Override
    public List<ScreeningEventDTO> toScreeningEventDTOList(List<ScreeningEvent> screeningEventList) {
        if ( screeningEventList == null ) {
            return null;
        }

        List<ScreeningEventDTO> list = new ArrayList<ScreeningEventDTO>( screeningEventList.size() );
        for ( ScreeningEvent screeningEvent : screeningEventList ) {
            list.add( toScreeningEventDTO( screeningEvent ) );
        }

        return list;
    }

    private Long screeningEventMovieClubId(ScreeningEvent screeningEvent) {
        MovieClub movieClub = screeningEvent.getMovieClub();
        if ( movieClub == null ) {
            return null;
        }
        return movieClub.getId();
    }
}
