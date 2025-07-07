package org.market.movieclubsservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.market.movieclubsservice.client.MovieClient;
import org.market.movieclubsservice.domain.MovieClub;
import org.market.movieclubsservice.domain.ScreeningEvent;
import org.market.movieclubsservice.dto.MovieDTO;
import org.market.movieclubsservice.dto.ScreeningEventDTO;
import org.market.movieclubsservice.exception.MovieClubNotFoundException;
import org.market.movieclubsservice.exception.MovieNotFoundException;
import org.market.movieclubsservice.mapper.ScreeningEventMapper;
import org.market.movieclubsservice.repository.MovieClubRepository;
import org.market.movieclubsservice.repository.ScreeningEventRepository;
import org.market.movieclubsservice.service.ScreeningEventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScreeningEventServiceImpl implements ScreeningEventService {

    private final ScreeningEventRepository screeningEventRepository;
    private final ScreeningEventMapper screeningEventMapper;
    private final MovieClubRepository movieClubRepository;
    private final MovieClient movieClient;

    public ScreeningEventServiceImpl(ScreeningEventRepository screeningEventRepository, ScreeningEventMapper screeningEventMapper, MovieClubRepository movieClubRepository, MovieClient movieClient) {
        this.screeningEventRepository = screeningEventRepository;
        this.screeningEventMapper = screeningEventMapper;
        this.movieClubRepository = movieClubRepository;
        this.movieClient = movieClient;
        log.info("ScreeningEventServiceImpl initialized with ScreeningEventRepository, ScreeningEventMapper, MovieClubRepository, and MovieClient.");
    }

    @Override
    public Optional<ScreeningEventDTO> getScreeningEvent(Long id) {
        log.info("Attempting to retrieve screening event by ID: {}.", id);
        Optional<ScreeningEvent> event = screeningEventRepository.findById(id);

        if (event.isEmpty()) {
            log.warn("Screening event with ID: {} not found.", id);
            return Optional.empty();
        }
        log.debug("Screening event with ID: {} found. Mapping to DTO.", id);
        ScreeningEventDTO eventDTO = screeningEventMapper.toScreeningEventDTO(event.get());
        log.info("Finished retrieving and mapping screening event ID: {}.", id);
        return Optional.ofNullable(eventDTO);
    }

    @Override
    @Transactional
    public ScreeningEventDTO addScreeningEvent(Long clubId, ScreeningEventDTO screeningEventDTO) {
        log.info("Attempting to add a new screening event for club ID: {} with movie ID: {}.", clubId, screeningEventDTO.getMovieId());
        Optional<MovieClub> club = movieClubRepository.findById(clubId);

        if(club.isEmpty()){
            log.warn("Movie club with ID: {} not found when adding screening event. Throwing MovieClubNotFoundException.", clubId);
            throw new MovieClubNotFoundException("Movie club with id " + clubId + " not found");
        }
        log.debug("Movie club with ID: {} found for screening event.", clubId);

        MovieDTO movie = movieClient.getMovieById(screeningEventDTO.getMovieId());
        if(movie == null){
            log.warn("Movie with ID: {} not found by Movie Service when adding screening event. Throwing MovieNotFoundException.", screeningEventDTO.getMovieId());
            throw new MovieNotFoundException("Movie with id " + screeningEventDTO.getMovieId() + " not found");
        }
        log.debug("Movie with ID: {} verified by Movie Service for screening event. Title: '{}'.", movie.getId(), movie.getTitle());

        ScreeningEvent event = screeningEventMapper.toScreeningEvent(screeningEventDTO);
        event.setMovieClub(club.get());
        event.setMovieId(movie.getId());
        log.debug("Screening event object prepared for saving. Date: {}, Movie ID: {}, Club ID: {}.",
                event.getDate(), event.getMovieId(), event.getMovieClub().getId());


        ScreeningEvent savedEvent = screeningEventRepository.save(event);
        log.info("Screening event ID: {} for movie ID: {} in club ID: {} saved successfully.",
                savedEvent.getId(), savedEvent.getMovieId(), savedEvent.getMovieClub().getId());

        ScreeningEventDTO resultDTO = screeningEventMapper.toScreeningEventDTO(savedEvent);
        resultDTO.setMovieTitle(movie.getTitle());
        log.info("Finished adding and mapping screening event ID: {}.", savedEvent.getId());
        return resultDTO;
    }

    @Override
    public List<ScreeningEventDTO> getUpcomingScreeningEventsForClub(Long clubId) {
        log.info("Attempting to retrieve upcoming screening events for club ID: {}.", clubId);
        List<ScreeningEvent> events = screeningEventRepository.findByMovieClubIdAndDateAfterOrderByDateAsc(clubId, LocalDateTime.now());
        log.debug("Found {} upcoming screening events for club ID: {}.", events.size(), clubId);

        List<ScreeningEventDTO> eventDTOs = events.stream().map(event -> {
            ScreeningEventDTO eventDTO = screeningEventMapper.toScreeningEventDTO(event);
            if (event.getMovieId() != null) {
                try {
                    log.debug("Fetching movie details for event movie ID: {} for event ID: {}.", event.getMovieId(), event.getId());
                    MovieDTO movieDTO = movieClient.getMovieById(event.getMovieId());
                    if (movieDTO != null) {
                        eventDTO.setMovieTitle(movieDTO.getTitle());
                        log.debug("Successfully fetched movie title '{}' for event movie ID: {}.", movieDTO.getTitle(), event.getMovieId());
                    } else {
                        log.warn("Movie details not found for ID: {} for event ID: {}. Setting title to 'Unknown Movie'.", event.getMovieId(), event.getId());
                        eventDTO.setMovieTitle("Unknown Movie");
                    }
                } catch (Exception e) {
                    log.error("Could not fetch movie details for ID {} for event ID {}: {}", event.getMovieId(), event.getId(), e.getMessage());
                    eventDTO.setMovieTitle("Unknown Movie");
                }
            } else {
                log.warn("Event ID: {} has no associated movie ID. Setting movie title to 'Unknown Movie'.", event.getId());
                eventDTO.setMovieTitle("Unknown Movie");
            }
            return eventDTO;
        }).collect(Collectors.toList());
        log.info("Finished retrieving and mapping upcoming screening events for club ID: {}.", clubId);
        return eventDTOs;
    }
}