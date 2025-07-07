package org.market.movieclubsservice.service;

import org.market.movieclubsservice.dto.MovieClubDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MovieClubService {
    List<MovieClubDTO> getPublicMovieClubs();
    Optional<MovieClubDTO> getMovieClubById(Long id);
    Boolean joinMovieClub(Long movieClubId, Long userId);
    MovieClubDTO createMovieClub(MovieClubDTO movieClubDTO, Long adminId);
    Boolean removeMemberFromClub(Long clubId, Long memberId, Long adminId);
    MovieClubDTO updateMovieClub(Long clubId, MovieClubDTO movieClubDTO, Long adminId); // Adaugat adminId pentru verificare permisiuni
    void deleteMovieClub(Long clubId, Long adminId);
}
