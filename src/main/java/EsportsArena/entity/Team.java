
package EsportsArena.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "teams",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_name", "game_name"}),
        @UniqueConstraint(columnNames = {"join_code"})
    }
)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(length = 500)
    private String description;

    @Column(name = "join_code", nullable = false, unique = true, length = 20)
    private String joinCode;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    private LocalDateTime createdAt;

    public Team() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (joinCode == null || joinCode.isBlank()) {
            joinCode = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
