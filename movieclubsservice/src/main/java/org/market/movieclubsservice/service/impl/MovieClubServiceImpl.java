package org.market.movieclubsservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.market.movieclubsservice.client.MovieClient;
import org.market.movieclubsservice.client.UserClient;
import org.market.movieclubsservice.domain.ClubSettings;
import org.market.movieclubsservice.domain.MovieClub;
import org.market.movieclubsservice.dto.MovieClubDTO;
import org.market.movieclubsservice.dto.MovieDTO;
import org.market.movieclubsservice.dto.UserDTO;
import org.market.movieclubsservice.exception.MovieClubAlreadyExistsException;
import org.market.movieclubsservice.exception.MovieClubNotFoundException;
import org.market.movieclubsservice.exception.PermissionDeniedException;
import org.market.movieclubsservice.exception.UserNotFoundException;
import org.market.movieclubsservice.mapper.MovieClubMapper;
import org.market.movieclubsservice.repository.MovieClubRepository;
import org.market.movieclubsservice.service.MovieClubService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieClubServiceImpl implements MovieClubService {

    private final MovieClubRepository movieClubRepository;
    private final MovieClubMapper movieClubMapper;
    private final UserClient userClient;
    private final MovieClient movieClient;

    public MovieClubServiceImpl(MovieClubRepository movieClubRepository, MovieClubMapper movieClubMapper, UserClient userClient, MovieClient movieClient) {
        this.movieClubRepository = movieClubRepository;
        this.movieClubMapper = movieClubMapper;
        this.userClient = userClient;
        this.movieClient = movieClient;
        log.info("MovieClubServiceImpl initialized with MovieClubRepository, MovieClubMapper, UserClient, and MovieClient.");
    }

    @Override
    public List<MovieClubDTO> getPublicMovieClubs() {
        log.info("Attempting to retrieve all public movie clubs.");
        List<MovieClub> movieClubs = movieClubRepository.findAllBySettingsIsPublic(true);
        log.debug("Found {} public movie clubs.", movieClubs.size());
        List<MovieClubDTO> publicClubsDTOs = movieClubs.stream().map(this::mapMovieClubToDTOWithExternalData).collect(Collectors.toList());
        log.info("Finished retrieving and mapping public movie clubs.");
        return publicClubsDTOs;
    }

    @Override
    public Optional<MovieClubDTO> getMovieClubById(Long id) {
        log.info("Attempting to retrieve movie club by ID: {}.", id);
        Optional<MovieClub> movieClubOptional = movieClubRepository.findById(id);

        if(movieClubOptional.isEmpty()) {
            log.warn("Movie club with ID: {} not found. Throwing MovieClubNotFoundException.", id);
            throw new MovieClubNotFoundException("Movie club with id " + id + " not found");
        }

        MovieClubDTO movieClubDTO = mapMovieClubToDTOWithExternalData(movieClubOptional.get());
        log.info("Movie club with ID: {} found and mapped.", id);
        return Optional.of(movieClubDTO);
    }

    @Override
    @Transactional
    public Boolean joinMovieClub(Long movieClubId, Long userId) {
        log.info("Attempting to join movie club ID: {} by user ID: {}.", movieClubId, userId);
        MovieClub movieClub = movieClubRepository.findById(movieClubId)
                .orElseThrow(() -> {
                    log.warn("Movie club with ID: {} not found when user ID: {} tried to join. Throwing MovieClubNotFoundException.", movieClubId, userId);
                    return new MovieClubNotFoundException("Movie club with id " + movieClubId + " not found");
                });
        log.debug("Movie club ID: {} found.", movieClubId);

        UserDTO user = userClient.getUserById(userId);
        if (user == null) {
            log.warn("User with ID: {} not found when trying to join club ID: {}. Throwing UserNotFoundException.", userId, movieClubId);
            throw new UserNotFoundException("User with id " + userId + " not found");
        }
        log.debug("User ID: {} verified for joining club ID: {}.", userId, movieClubId);

        if (movieClub.getMemberIds().contains(userId)) {
            log.info("User ID: {} is already a member of club ID: {}. No action needed.", userId, movieClubId);
            return false;
        }

        if (movieClub.getMemberIds().size() >= movieClub.getSettings().getMaxMembers()) {
            log.warn("Movie club ID: {} has reached its maximum capacity ({} members). User ID: {} cannot join.",
                    movieClubId, movieClub.getSettings().getMaxMembers(), userId);
            throw new IllegalStateException("Club has reached its maximum capacity.");
        }

        movieClub.getMemberIds().add(userId);
        movieClubRepository.save(movieClub);
        log.info("User ID: {} successfully joined movie club ID: {}.", userId, movieClubId);
        return true;
    }

    @Override
    @Transactional
    public MovieClubDTO createMovieClub(MovieClubDTO movieClubDTO, Long adminId) {
        log.info("Attempting to create new movie club with name: '{}' by admin ID: {}.", movieClubDTO.getName(), adminId);
        UserDTO admin = userClient.getUserById(adminId);
        if (admin == null) {
            log.warn("Admin user with ID: {} not found when creating club. Throwing UserNotFoundException.", adminId);
            throw new UserNotFoundException("Admin user with id " + adminId + " not found");
        }
        log.debug("Admin user ID: {} verified for club creation.", adminId);

        if (movieClubRepository.findByName(movieClubDTO.getName()).isPresent()) {
            log.warn("Movie club with name '{}' already exists. Throwing MovieClubAlreadyExistsException.", movieClubDTO.getName());
            throw new MovieClubAlreadyExistsException("Movie club with name " + movieClubDTO.getName() + " already exists");
        }

        MovieClub movieClub = movieClubMapper.toMovieClub(movieClubDTO);
        movieClub.setAdminId(adminId);
        movieClub.getMemberIds().add(adminId);
        log.debug("New movie club object created with admin ID: {} and admin added as member.", adminId);

        ClubSettings settings = movieClub.getSettings();
        if (settings == null) {
            settings = new ClubSettings();
            settings.setIsPublic(true);
            settings.setMaxMembers(50);
            log.debug("Club settings were null, defaulting to public=true and maxMembers=50.");
        }
        movieClub.setSettings(settings);
        settings.setMovieClub(movieClub);

        MovieClub savedClub = movieClubRepository.save(movieClub);
        log.info("New movie club '{}' (ID: {}) created and saved successfully by admin ID: {}.", savedClub.getName(), savedClub.getId(), adminId);
        return mapMovieClubToDTOWithExternalData(savedClub);
    }

    @Override
    @Transactional
    public Boolean removeMemberFromClub(Long clubId, Long memberId, Long adminId) {
        log.info("Attempting to remove member ID: {} from club ID: {} by admin ID: {}.", memberId, clubId, adminId);
        MovieClub movieClub = movieClubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.warn("Movie club with ID: {} not found when removing member. Throwing MovieClubNotFoundException.", clubId);
                    return new MovieClubNotFoundException("Movie club with id " + clubId + " not found");
                });
        log.debug("Movie club ID: {} found.", clubId);

        if (!movieClub.getAdminId().equals(adminId)) {
            log.warn("Admin ID: {} attempted to remove member from club ID: {}, but is not the club admin (actual admin ID: {}). Throwing PermissionDeniedException.",
                    adminId, clubId, movieClub.getAdminId());
            throw new PermissionDeniedException("Only the admin can remove members from the club");
        }
        log.debug("Admin ID: {} confirmed as admin for club ID: {}.", adminId, clubId);

        if (!movieClub.getMemberIds().contains(memberId)) {
            log.info("Member ID: {} is not a member of club ID: {}. No action needed.", memberId, clubId);
            return false;
        }

        if (movieClub.getAdminId().equals(memberId)) {
            log.warn("Attempted to remove admin ID: {} from club ID: {}. Admin cannot be removed directly. Throwing IllegalArgumentException.", memberId, clubId);
            throw new IllegalArgumentException("Admin cannot be removed from the club directly. Delete the club instead.");
        }

        movieClub.getMemberIds().remove(memberId);
        movieClubRepository.save(movieClub);
        log.info("Member ID: {} successfully removed from movie club ID: {}.", memberId, clubId);
        return true;
    }

    @Override
    @Transactional
    public MovieClubDTO updateMovieClub(Long clubId, MovieClubDTO movieClubDTO, Long adminId) {
        log.info("Attempting to update movie club ID: {} by admin ID: {}.", clubId, adminId);
        MovieClub existingClub = movieClubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.warn("Movie club with ID: {} not found for update. Throwing MovieClubNotFoundException.", clubId);
                    return new MovieClubNotFoundException("Movie club with id " + clubId + " not found");
                });
        log.debug("Movie club ID: {} found for update.", clubId);


        if (!existingClub.getAdminId().equals(adminId)) {
            log.warn("Admin ID: {} attempted to update club ID: {}, but is not the club admin (actual admin ID: {}). Throwing PermissionDeniedException.",
                    adminId, clubId, existingClub.getAdminId());
            throw new PermissionDeniedException("Only the admin of the club can update the club");
        }
        log.debug("Admin ID: {} confirmed as admin for club ID: {}.", adminId, clubId);

        if (!existingClub.getName().equals(movieClubDTO.getName())) {
            log.debug("Club name changed from '{}' to '{}'. Checking for name conflict.", existingClub.getName(), movieClubDTO.getName());
            if (movieClubRepository.findByName(movieClubDTO.getName()).isPresent()) {
                log.warn("New club name '{}' already exists. Throwing MovieClubAlreadyExistsException.", movieClubDTO.getName());
                throw new MovieClubAlreadyExistsException("Movie club with name " + movieClubDTO.getName() + " already exists");
            }
        }

        existingClub.setName(movieClubDTO.getName());
        existingClub.setDescription(movieClubDTO.getDescription());
        log.debug("Club ID: {} name and description updated in memory.", clubId);

        ClubSettings existingSettings = existingClub.getSettings();
        if (existingSettings == null) {
            existingSettings = new ClubSettings();
            existingClub.setSettings(existingSettings);
            existingSettings.setMovieClub(existingClub);
            log.debug("Club settings were null, initializing new settings for club ID: {}.", clubId);
        }
        existingSettings.setIsPublic(movieClubDTO.getSettings().getIsPublic());
        existingSettings.setMaxMembers(movieClubDTO.getSettings().getMaxMembers());
        log.debug("Club ID: {} settings updated to Public: {}, MaxMembers: {}.", clubId, existingSettings.getIsPublic(), existingSettings.getMaxMembers());

        MovieClub updatedClub = movieClubRepository.save(existingClub);
        log.info("Movie club ID: {} updated and saved successfully.", updatedClub.getId());
        return mapMovieClubToDTOWithExternalData(updatedClub);
    }

    @Override
    @Transactional
    public void deleteMovieClub(Long clubId, Long adminId) {
        log.info("Attempting to delete movie club ID: {} by admin ID: {}.", clubId, adminId);
        Optional<MovieClub> clubOptional = movieClubRepository.findById(clubId);

        if (clubOptional.isEmpty()) {
            log.warn("Movie club with ID: {} not found for deletion. Throwing MovieClubNotFoundException.", clubId);
            throw new MovieClubNotFoundException("Movie club with id " + clubId + " not found");
        }

        MovieClub clubToDelete = clubOptional.get();

        if (!clubToDelete.getAdminId().equals(adminId)) {
            log.warn("Admin ID: {} attempted to delete club ID: {}, but is not the club admin (actual admin ID: {}). Throwing PermissionDeniedException.",
                    adminId, clubId, clubToDelete.getAdminId());
            throw new PermissionDeniedException("Only the admin of the club can delete the club");
        }
        log.debug("Admin ID: {} confirmed as admin for club ID: {}.", adminId, clubId);

        movieClubRepository.deleteById(clubId);
        log.info("Successfully deleted club with ID: {}.", clubId); // Înlocuit System.out.println
    }

    private MovieClubDTO mapMovieClubToDTOWithExternalData(MovieClub movieClub) {
        log.debug("Mapping movie club ID: {} to DTO and fetching external data.", movieClub.getId());
        MovieClubDTO movieClubDTO = movieClubMapper.toMovieClubDTO(movieClub);

        if (movieClub.getAdminId() != null) {
            try {
                log.debug("Fetching admin details for admin ID: {}.", movieClub.getAdminId());
                UserDTO adminDTO = userClient.getUserById(movieClub.getAdminId());
                if (adminDTO != null) {
                    movieClubDTO.setAdminId(adminDTO.getId());
                    log.debug("Successfully fetched admin details for admin ID: {}.", movieClub.getAdminId());
                } else {
                    log.warn("Admin user details not found for ID: {}.", movieClub.getAdminId());
                }
            } catch (Exception e) {
                log.error("Could not fetch admin details for ID {}: {}", movieClub.getAdminId(), e.getMessage());
            }
        }

        if (movieClub.getMemberIds() != null && !movieClub.getMemberIds().isEmpty()) {
            try {
                Set<Long> memberIds = movieClub.getMemberIds();
                Set<UserDTO> memberUsers = new HashSet<>();
                log.debug("Fetching details for {} members for club ID: {}.", memberIds.size(), movieClub.getId());

                for (Long memberId : memberIds) {
                    UserDTO memberDTO = userClient.getUserById(memberId);
                    if (memberDTO != null) {
                        memberUsers.add(memberDTO);
                        log.trace("Fetched member ID: {} for club ID: {}.", memberId, movieClub.getId());
                    } else {
                        log.warn("Member user details not found for ID: {} for club ID: {}.", memberId, movieClub.getId());
                    }
                }
                movieClubDTO.setMembers(memberUsers);
                log.debug("Successfully fetched details for {} members for club ID: {}.", memberUsers.size(), movieClub.getId());
            } catch (Exception e) {
                log.error("Could not fetch member details for club ID {}: {}", movieClub.getId(), e.getMessage());
                movieClubDTO.setMembers(new HashSet<>());
            }
        } else {
            movieClubDTO.setMembers(new HashSet<>());
            log.debug("No members found for club ID: {}.", movieClub.getId());
        }

        if (movieClubDTO.getScreeningEvents() != null && !movieClubDTO.getScreeningEvents().isEmpty()) {
            log.debug("Processing {} screening events for club ID: {}.", movieClubDTO.getScreeningEvents().size(), movieClub.getId());
            movieClubDTO.getScreeningEvents().forEach(eventDTO -> {
                if (eventDTO.getMovieId() != null) {
                    try {
                        log.debug("Fetching movie details for event movie ID: {} for club ID: {}.", eventDTO.getMovieId(), movieClub.getId());
                        MovieDTO movieDTO = movieClient.getMovieById(eventDTO.getMovieId());
                        if (movieDTO != null) {
                            eventDTO.setMovieId(movieDTO.getId());
                            eventDTO.setMovieTitle(movieDTO.getTitle());
                            log.debug("Successfully fetched movie title '{}' for event movie ID: {}.", movieDTO.getTitle(), eventDTO.getMovieId());
                        } else {
                            log.warn("Movie details not found for ID: {} for event in club ID: {}. Setting title to 'Unknown Movie'.", eventDTO.getMovieId(), movieClub.getId());
                            eventDTO.setMovieTitle("Unknown Movie");
                        }
                    } catch (Exception e) {
                        log.error("Could not fetch movie details for ID {} for event in club ID {}: {}", eventDTO.getMovieId(), movieClub.getId(), e.getMessage());
                        eventDTO.setMovieTitle("Unknown Movie");
                    }
                }
            });
        }
        log.info("Finished mapping movie club ID: {} to DTO with external data.", movieClub.getId());
        return movieClubDTO;
    }
}