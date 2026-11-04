
package EsportsArena;

import EsportsArena.entity.User;
import EsportsArena.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin-dashboard";
    }

    @PostMapping("/admin/users/{id}/role")
    public String updateUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentAdmin = userRepository
                .findByEmail(authentication.getName()).orElse(null);

        if (currentAdmin == null || id.equals(currentAdmin.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "You cannot change your own role.");
            return "redirect:/admin/dashboard";
        }

        User targetUser = userRepository.findById(id).orElse(null);

        if (targetUser == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "User not found.");
            return "redirect:/admin/dashboard";
        }

        String newRole = role.trim().toUpperCase();

        if (!newRole.equals("PLAYER")
                && !newRole.equals("ORGANISER")
                && !newRole.equals("ADMIN")) {

            redirectAttributes.addFlashAttribute(
                    "error", "Invalid role selected.");
            return "redirect:/admin/dashboard";
        }

        targetUser.setRole(newRole);

        if (!newRole.equals("ORGANISER")) {
            targetUser.setOrganiserApproved(false);
        } else {
            targetUser.setOrganiserApproved(false);
        }

        userRepository.save(targetUser);

        redirectAttributes.addFlashAttribute(
                "success", "User role updated. Organiser approval is pending.");

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/users/{id}/organiser-approval")
    public String updateOrganiserApproval(
            @PathVariable Long id,
            @RequestParam boolean approved,
            RedirectAttributes redirectAttributes) {

        User targetUser = userRepository.findById(id).orElse(null);

        if (targetUser == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "User not found.");
            return "redirect:/admin/dashboard";
        }

        if (!"ORGANISER".equalsIgnoreCase(targetUser.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Only organisers can be approved.");
            return "redirect:/admin/dashboard";
        }

        targetUser.setOrganiserApproved(approved);
        userRepository.save(targetUser);

        redirectAttributes.addFlashAttribute(
                "success",
                approved ? "Organiser approved successfully."
                         : "Organiser approval removed.");

        return "redirect:/admin/dashboard";
    }
}
