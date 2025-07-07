package org.market.movieclubsservice.mapper;

import javax.annotation.processing.Generated;
import org.market.movieclubsservice.domain.ClubSettings;
import org.market.movieclubsservice.dto.ClubSettingsDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:52:46+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 23.0.2 (Azul Systems, Inc.)"
)
@Component
public class ClubSettingsMapperImpl implements ClubSettingsMapper {

    @Override
    public ClubSettings toClubSettings(ClubSettingsDTO clubSettingsDTO) {
        if ( clubSettingsDTO == null ) {
            return null;
        }

        ClubSettings clubSettings = new ClubSettings();

        clubSettings.setId( clubSettingsDTO.getId() );
        clubSettings.setIsPublic( clubSettingsDTO.getIsPublic() );
        clubSettings.setMaxMembers( clubSettingsDTO.getMaxMembers() );

        return clubSettings;
    }

    @Override
    public ClubSettingsDTO toClubSettingsDTO(ClubSettings clubSettings) {
        if ( clubSettings == null ) {
            return null;
        }

        ClubSettingsDTO.ClubSettingsDTOBuilder clubSettingsDTO = ClubSettingsDTO.builder();

        clubSettingsDTO.id( clubSettings.getId() );
        clubSettingsDTO.isPublic( clubSettings.getIsPublic() );
        clubSettingsDTO.maxMembers( clubSettings.getMaxMembers() );

        return clubSettingsDTO.build();
    }
}
