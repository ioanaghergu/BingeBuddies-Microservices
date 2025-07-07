package org.market.movieclubsservice.repository;

import org.market.movieclubsservice.domain.ScreeningEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningEventRepository extends JpaRepository<ScreeningEvent, Long> {
    List<ScreeningEvent> findByMovieClubIdAndDateAfterOrderByDateAsc(Long movieClubId, LocalDateTime date);
}
