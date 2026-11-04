package EsportsArena.repository;

import EsportsArena.entity.Team;
import EsportsArena.entity.TeamJoinRequest;
import EsportsArena.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamJoinRequestRepository
        extends JpaRepository<TeamJoinRequest, Long> {

    List<TeamJoinRequest> findByTeam(Team team);

    List<TeamJoinRequest> findByUser(User user);

    List<TeamJoinRequest> findByTeamAndStatus(
            Team team, String status);

    Optional<TeamJoinRequest> findByTeamAndUser(
            Team team, User user);

    boolean existsByTeamAndUserAndStatus(
            Team team, User user, String status);
}