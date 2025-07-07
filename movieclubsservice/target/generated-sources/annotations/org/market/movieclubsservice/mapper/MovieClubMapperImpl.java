package org.market.movieclubsservice.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.market.movieclubsservice.domain.MovieClub;
import org.market.movieclubsservice.dto.MovieClubDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:52:46+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 23.0.2 (Azul Systems, Inc.)"
)
@Component
public class MovieClubMapperImpl implements MovieClubMapper {

    @Autowired
    private ClubSettingsMapper clubSettingsMapper;
    @Autowired
    private ScreeningEventMapper screeningEventMapper;

    @Override
    public MovieClub toMovieClub(MovieClubDTO movieClubDTO) {
        if ( movieClubDTO == null ) {
            return null;
        }

        MovieClub movieClub = new MovieClub();

        movieClub.setSettings( clubSettingsMapper.toClubSettings( movieClubDTO.getSettings() ) );
        movieClub.setAdminId( movieClubDTO.getAdminId() );
        movieClub.setId( movieClubDTO.getId() );
        movieClub.setName( movieClubDTO.getName() );
        movieClub.setDescription( movieClubDTO.getDescription() );

        movieClub.setMemberIds( mapUserDTOsToIds(movieClubDTO.getMembers()) );

        return movieClub;
    }

    @Override
    public MovieClubDTO toMovieClubDTO(MovieClub movieClub) {
        if ( movieClub == null ) {
            return null;
        }

        MovieClubDTO.MovieClubDTOBuilder movieClubDTO = MovieClubDTO.builder();

        movieClubDTO.settings( clubSettingsMapper.toClubSettingsDTO( movieClub.getSettings() ) );
        movieClubDTO.adminId( movieClub.getAdminId() );
        movieClubDTO.screeningEvents( screeningEventMapper.toScreeningEventDTOList( movieClub.getScreeningEvents() ) );
        movieClubDTO.id( movieClub.getId() );
        movieClubDTO.name( movieClub.getName() );
        movieClubDTO.description( movieClub.getDescription() );

        return movieClubDTO.build();
    }

    @Override
    public List<MovieClubDTO> toMovieClubDTOList(List<MovieClub> movieClubs) {
        if ( movieClubs == null ) {
            return null;
        }

        List<MovieClubDTO> list = new ArrayList<MovieClubDTO>( movieClubs.size() );
        for ( MovieClub movieClub : movieClubs ) {
            list.add( toMovieClubDTO( movieClub ) );
        }

        return list;
    }
}
