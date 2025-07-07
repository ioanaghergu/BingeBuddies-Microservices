package org.market.movieclubsservice.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MovieClub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //@NotNull(message = "A movie club must have an admin.")
    private Long adminId;

    @NotBlank(message = "Club name required")
    @Size(min = 3, max = 100, message = "Club name must be between 3 and 100 characters.")
    private String name;

    @Size(max = 100, message = "Club description must not exceed 100 characters.")
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SETTINGS_ID", referencedColumnName = "ID")
    @EqualsAndHashCode.Exclude
    @NotNull(message = "Club settings required.")
    @Valid
    private ClubSettings settings;

    @ElementCollection
    @CollectionTable(name = "club_members", joinColumns = @JoinColumn(name = "CLUB_ID"))
    @Column(name = "USER_ID")
    private Set<Long> memberIds = new HashSet<>();

    @OneToMany(mappedBy = "movieClub", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ScreeningEvent> screeningEvents;

}
