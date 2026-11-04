package EsportsArena.repository;

import EsportsArena.entity.Tournament;
import EsportsArena.entity.TournamentResult;
import EsportsArena.entity.Team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentResultRepository
        extends JpaRepository<TournamentResult, Long> {

    List<TournamentResult> findByTournament(Tournament tournament);

    List<TournamentResult> findByTournamentOrderByTotalPointsDesc(
            Tournament tournament);

    Optional<TournamentResult> findByTournamentAndTeamAndMatchNumber(
            Tournament tournament, Team team, int matchNumber);

    List<TournamentResult> findByTournamentAndMatchNumberOrderByTotalPointsDesc(
            Tournament tournament, int matchNumber);
    List<TournamentResult> findByTeam(Team team);
}