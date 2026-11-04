
package EsportsArena.repository;

import EsportsArena.entity.Team;
import EsportsArena.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByLeader(User leader);

    List<Team> findByGameName(String gameName);

    boolean existsByTeamNameAndGameName(String teamName, String gameName);

    Optional<Team> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);
}
