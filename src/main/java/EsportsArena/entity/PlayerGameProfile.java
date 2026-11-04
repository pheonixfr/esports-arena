
package EsportsArena.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "player_game_profiles",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "game_name"}
        )
    }
)
public class PlayerGameProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "game_name", nullable = false, length = 60)
    private String gameName;

    @Column(name = "game_player_id", length = 100)
    private String gamePlayerId;

    @Column(name = "in_game_name", length = 60)
    private String inGameName;

    @Column(name = "roles", length = 300)
    private String roles;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PlayerGameProfile() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGamePlayerId() {
        return gamePlayerId;
    }

    public void setGamePlayerId(String gamePlayerId) {
        this.gamePlayerId = gamePlayerId;
    }

    public String getInGameName() {
        return inGameName;
    }

    public void setInGameName(String inGameName) {
        this.inGameName = inGameName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
