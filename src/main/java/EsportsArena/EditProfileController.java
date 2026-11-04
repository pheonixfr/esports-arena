
package EsportsArena;

import EsportsArena.entity.User;
import EsportsArena.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EditProfileController {

    private final UserRepository userRepository;

    public EditProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(
            Authentication authentication,
            Model model) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Logged-in user was not found."));

        model.addAttribute("player", user);
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String username,
            @RequestParam(required = false) String bio,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Logged-in user was not found."));

        String cleanUsername = username == null ? "" : username.trim();
        String cleanBio = bio == null ? "" : bio.trim();

        if (cleanUsername.length() < 3 || cleanUsername.length() > 50) {
            model.addAttribute("player", user);
            model.addAttribute("error",
                    "Username must be between 3 and 50 characters.");
            model.addAttribute("enteredUsername", cleanUsername);
            model.addAttribute("enteredBio", cleanBio);
            return "edit-profile";
        }

        if (cleanBio.length() > 300) {
            model.addAttribute("player", user);
            model.addAttribute("error",
                    "Your bio cannot exceed 300 characters.");
            model.addAttribute("enteredUsername", cleanUsername);
            model.addAttribute("enteredBio", cleanBio);
            return "edit-profile";
        }

        user.setUsername(cleanUsername);
        user.setBio(cleanBio.isEmpty() ? null : cleanBio);

        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "success", "Your profile was updated successfully!");

        return "redirect:/profile";
    }
}
