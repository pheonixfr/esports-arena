
package EsportsArena;

import EsportsArena.entity.User;
import EsportsArena.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class VerificationController {

    private final UserRepository userRepository;

    public VerificationController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(name = "token", required = false) String token,
            Model model) {

        if (token == null || token.isBlank()) {
            model.addAttribute("title", "Invalid verification link");
            model.addAttribute("message",
                    "This verification link is invalid or incomplete.");
            model.addAttribute("success", false);
            return "verification-result";
        }

        Optional<User> optionalUser =
                userRepository.findByVerificationToken(token);

        if (optionalUser.isEmpty()) {
            model.addAttribute("title", "Invalid verification link");
            model.addAttribute("message",
                    "This link is invalid, has already been used, or the account could not be found.");
            model.addAttribute("success", false);
            return "verification-result";
        }

        User user = optionalUser.get();

        if (user.getTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getTokenExpiry())) {

            model.addAttribute("title", "Verification link expired");
            model.addAttribute("message",
                    "Your verification link has expired. Email verification must be renewed before you can use your account.");
            model.addAttribute("success", false);
            return "verification-result";
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        userRepository.save(user);

        model.addAttribute("title", "Email verified!");
        model.addAttribute("message",
                "Your email address has been verified successfully. You can now proceed to log in.");
        model.addAttribute("success", true);

        return "verification-result";
    }
}
