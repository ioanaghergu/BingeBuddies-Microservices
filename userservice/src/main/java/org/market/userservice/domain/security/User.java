package org.market.userservice.domain.security;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true)
    private String keycloakId;

    @NotBlank(message = "Username required.")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;

    @Singular
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = { @JoinColumn(name = "USER_ID", referencedColumnName = "ID")},
            inverseJoinColumns = { @JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")})
    private Set<Authority> authorities;

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // COMENTAT/ELIMINAT
    // @ToString.Exclude // COMENTAT/ELIMINAT
    // private List<Review> reviews; // COMENTAT/ELIMINAT

    // @ManyToMany(mappedBy = "members") // COMENTAT/ELIMINAT
    // @ToString.Exclude // COMENTAT/ELIMINAT
    // private Set<MovieClub> clubs = new HashSet<>(); // COMENTAT/ELIMINAT
}
