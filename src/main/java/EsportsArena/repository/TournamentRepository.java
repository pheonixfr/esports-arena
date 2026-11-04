
package EsportsArena.repository;

import EsportsArena.entity.Tournament;
import EsportsArena.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByOrganiser(User organiser);

    List<Tournament> findByGameName(String gameName);

    List<Tournament> findByStatus(String status);

    List<Tournament> findByOrganiserAndStatus(User organiser, String status);
}

