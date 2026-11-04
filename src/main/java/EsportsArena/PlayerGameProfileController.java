
package EsportsArena;

import EsportsArena.entity.PlayerGameProfile;
import EsportsArena.entity.User;
import EsportsArena.repository.PlayerGameProfileRepository;
import EsportsArena.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile/game-profiles")
public class PlayerGameProfileController {

    private final PlayerGameProfileRepository gameProfileRepository;
    private final UserRepository userRepository;

    public PlayerGameProfileController(
            PlayerGameProfileRepository gameProfileRepository,
            UserRepository userRepository) {
        this.gameProfileRepository = gameProfileRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentPlayer(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Player not found"));
    }

    private PlayerGameProfile getOwnedProfile(
            Long id, User player) {

        PlayerGameProfile profile = gameProfileRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Game profile not found"));

        if (!profile.getUser().getId().equals(player.getId())) {
            throw new IllegalArgumentException("Unauthorized access");
        }

        return profile;
    }

    @GetMapping
    public String showGameProfiles(
            Authentication authentication,
            Model model) {

        User player = getCurrentPlayer(authentication);

        model.addAttribute("player", player);
        model.addAttribute(
                "gameProfiles",
                gameProfileRepository.findByUser(player));

        return "game-profiles";
    }

    @PostMapping("/add")
    public String addGameProfile(
            Authentication authentication,
            @RequestParam String gameName,
            @RequestParam(required = false) String gamePlayerId,
            @RequestParam(required = false) String inGameName,
            @RequestParam(required = false) String roles,
            RedirectAttributes redirectAttributes) {

        User player = getCurrentPlayer(authentication);
        String normalizedGameName = gameName.trim();

        if (!java.util.Set.of(
                "Free Fire",
                "BGMI",
                "Minecraft PE",
                "Mobile Legends: Bang Bang"
        ).contains(normalizedGameName)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please select a valid game.");
            return "redirect:/profile/game-profiles";
        }

        if (gameProfileRepository.existsByUserAndGameName(
                player, normalizedGameName)) {
            redirectAttributes.addFlashAttribute(
                    "error", "You already have a profile for this game.");
            return "redirect:/profile/game-profiles";
        }

        PlayerGameProfile profile = new PlayerGameProfile();
        profile.setUser(player);
        profile.setGameName(normalizedGameName);
        profile.setGamePlayerId(
                gamePlayerId == null ? null : gamePlayerId.trim());
        profile.setInGameName(
                inGameName == null ? null : inGameName.trim());
        profile.setRoles(
                roles == null ? null : roles.trim());

        gameProfileRepository.save(profile);

        redirectAttributes.addFlashAttribute(
                "success", "Game profile saved successfully!");

        return "redirect:/profile/game-profiles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        User player = getCurrentPlayer(authentication);
        PlayerGameProfile profile = getOwnedProfile(id, player);

        model.addAttribute("gameProfile", profile);

        return "edit-game-profile";
    }

    @PostMapping("/update/{id}")
    public String updateGameProfile(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String gamePlayerId,
            @RequestParam(required = false) String inGameName,
            @RequestParam(required = false) String roles,
            RedirectAttributes redirectAttributes) {

        User player = getCurrentPlayer(authentication);
        PlayerGameProfile profile = getOwnedProfile(id, player);

        profile.setGamePlayerId(
                gamePlayerId == null ? null : gamePlayerId.trim());
        profile.setInGameName(
                inGameName == null ? null : inGameName.trim());
        profile.setRoles(
                roles == null ? null : roles.trim());

        gameProfileRepository.save(profile);

        redirectAttributes.addFlashAttribute(
                "success", "Game profile updated successfully!");

        return "redirect:/profile/game-profiles";
    }

    @PostMapping("/delete/{id}")
    public String deleteGameProfile(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User player = getCurrentPlayer(authentication);
        PlayerGameProfile profile = getOwnedProfile(id, player);

        gameProfileRepository.delete(profile);

        redirectAttributes.addFlashAttribute(
                "success", "Game profile deleted successfully!");

        return "redirect:/profile/game-profiles";
    }
}
