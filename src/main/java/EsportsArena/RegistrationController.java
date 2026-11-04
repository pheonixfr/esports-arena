
package EsportsArena;

import EsportsArena.dto.RegistrationForm;
import EsportsArena.entity.User;
import EsportsArena.repository.UserRepository;
import EsportsArena.service.EmailService;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("registrationForm") RegistrationForm form,
            Model model) {

        String username = form.getUsername() == null
                ? "" : form.getUsername().trim();

        String email = form.getEmail() == null
                ? "" : form.getEmail().trim().toLowerCase();

        String password = form.getPassword() == null
                ? "" : form.getPassword();

        String confirmPassword = form.getConfirmPassword() == null
                ? "" : form.getConfirmPassword();

        if (username.length() < 3 || username.length() > 50) {
            return showError(form, model,
                    "Username must contain 3 to 50 characters.");
        }

        if (email.isBlank() || email.length() > 150
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return showError(form, model,
                    "Please enter a valid email address.");
        }

        if (password.length() < 8) {
            return showError(form, model,
                    "Password must contain at least 8 characters.");
        }

        if (!password.equals(confirmPassword)) {
            return showError(form, model,
                    "Passwords do not match.");
        }

        if (userRepository.existsByEmail(email)) {
            return showError(form, model,
                    "This email is already registered. Please use another email.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmailVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(30));

        try {
            userRepository.save(user);
            emailService.sendVerificationEmail(email, token);
        } catch (MailException exception) {
            userRepository.delete(user);

            return showError(form, model,
                    "We couldn't send the verification email. Check your Gmail configuration and try again.");
        } catch (RuntimeException exception) {
            if (user.getId() != null) {
                userRepository.delete(user);
            }

            return showError(form, model,
                    "We couldn't complete registration. Please try again.");
        }

        RegistrationForm emptyForm = new RegistrationForm();
        model.addAttribute("registrationForm", emptyForm);
        model.addAttribute("successMessage",
                "Registration successful! Check your email and click the verification link within 30 minutes.");

        return "register";
    }

    private String showError(
            RegistrationForm form,
            Model model,
            String message) {

        form.setPassword("");
        form.setConfirmPassword("");

        model.addAttribute("registrationForm", form);
        model.addAttribute("errorMessage", message);

        return "register";
    }
}
