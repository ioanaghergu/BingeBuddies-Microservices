package org.market.movieclubsservice.repository;

import org.market.movieclubsservice.domain.ClubSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubSettingsRepository extends JpaRepository<ClubSettings, Long> {

}
