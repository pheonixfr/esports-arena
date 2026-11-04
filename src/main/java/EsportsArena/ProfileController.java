
package EsportsArena;

import EsportsArena.entity.User;
import EsportsArena.repository.UserRepository;
import EsportsArena.repository.PlayerGameProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PlayerGameProfileRepository gameProfileRepository;

    public ProfileController(
            UserRepository userRepository,
            PlayerGameProfileRepository gameProfileRepository) {
        this.userRepository = userRepository;
        this.gameProfileRepository = gameProfileRepository;
    }

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException("Logged-in user was not found.")
        );

        model.addAttribute("player", user);

        return "profile";
    }

    @GetMapping("/profile/details")
    public String showProfileDetails(
            Authentication authentication, Model model) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException("Logged-in user was not found.")
        );

        model.addAttribute("player", user);
        model.addAttribute(
                "gameProfiles",
                gameProfileRepository.findByUser(user)
        );

        return "profile-details";
    }
}
