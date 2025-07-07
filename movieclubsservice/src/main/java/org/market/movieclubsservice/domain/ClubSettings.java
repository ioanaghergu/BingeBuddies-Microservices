package org.market.movieclubsservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClubSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Club status required.")
    private Boolean isPublic;

    @NotNull(message = "Club max capacity required.")
    @Min(value = 1, message = "A club must have at least one member.")
    private Integer maxMembers;

    @OneToOne(mappedBy = "settings")
    @EqualsAndHashCode.Exclude
    private MovieClub movieClub;

}
