
package EsportsArena.repository;

import EsportsArena.entity.Team;
import EsportsArena.entity.Tournament;
import EsportsArena.entity.TournamentRegistration;
import EsportsArena.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRegistrationRepository
        extends JpaRepository<TournamentRegistration, Long> {

    boolean existsByTournamentAndUser(
            Tournament tournament, User user);

    long countByTournament(Tournament tournament);

    List<TournamentRegistration> findByUser(User user);

    List<TournamentRegistration> findByTournament(Tournament tournament);

    boolean existsByTournamentAndTeam(
            Tournament tournament, Team team);
}
