package org.market.movieclubsservice.service;

import org.market.movieclubsservice.dto.ScreeningEventDTO;

import java.util.List;
import java.util.Optional;

public interface ScreeningEventService {
    List<ScreeningEventDTO> getUpcomingScreeningEventsForClub(Long clubId);
    ScreeningEventDTO addScreeningEvent(Long clubId, ScreeningEventDTO screeningEventDTO);
    Optional<ScreeningEventDTO> getScreeningEvent(Long eventId);
}
