package org.market.movieclubsservice.mapper;

import org.mapstruct.Mapper;
import org.market.movieclubsservice.domain.ClubSettings;
import org.market.movieclubsservice.dto.ClubSettingsDTO;

@Mapper(componentModel = "spring")
public interface ClubSettingsMapper {
    ClubSettings toClubSettings(ClubSettingsDTO clubSettingsDTO);
    ClubSettingsDTO toClubSettingsDTO(ClubSettings clubSettings);
}
