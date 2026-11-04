package EsportsArena.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "tournament_results",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"tournament_id", "team_id", "match_number"}
        )
    }
)
public class TournamentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "match_number", nullable = false)
    private int matchNumber;

    private int position;

    private int kills;

    private int placementPoints;

    private int killPoints;

    private int totalPoints;

    public TournamentResult() {
    }

    public Long getId() {
        return id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getPlacementPoints() {
        return placementPoints;
    }

    public void setPlacementPoints(int placementPoints) {
        this.placementPoints = placementPoints;
    }

    public int getKillPoints() {
        return killPoints;
    }

    public void setKillPoints(int killPoints) {
        this.killPoints = killPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}