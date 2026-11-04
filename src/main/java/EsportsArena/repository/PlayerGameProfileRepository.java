
package EsportsArena.repository;

import EsportsArena.entity.PlayerGameProfile;
import EsportsArena.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerGameProfileRepository
        extends JpaRepository<PlayerGameProfile, Long> {

    List<PlayerGameProfile> findByUser(User user);

    Optional<PlayerGameProfile> findByUserAndGameName(
            User user, String gameName);

    boolean existsByUserAndGameName(
            User user, String gameName);
}
