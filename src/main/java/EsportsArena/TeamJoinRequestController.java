package EsportsArena;

import EsportsArena.entity.Team;
import EsportsArena.entity.TeamJoinRequest;
import EsportsArena.entity.User;
import EsportsArena.repository.TeamJoinRequestRepository;
import EsportsArena.repository.TeamMemberRepository;
import EsportsArena.repository.TeamRepository;
import EsportsArena.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teams")
public class TeamJoinRequestController {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamJoinRequestRepository requestRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamJoinRequestController(
            TeamRepository teamRepository,
            UserRepository userRepository,
            TeamJoinRequestRepository requestRepository,
            TeamMemberRepository teamMemberRepository) {

        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @PostMapping("/request-join")
    public String requestToJoin(
            @RequestParam String joinCode,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        Team team = teamRepository
                .findByJoinCode(joinCode.trim().toUpperCase())
                .orElse(null);

        if (team == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Invalid team join code.");
            return "redirect:/teams";
        }

        if (team.getLeader().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "You are already the team leader.");
            return "redirect:/teams";
        }

        if (teamMemberRepository.existsByTeamAndUser(team, user)) {
            redirectAttributes.addFlashAttribute(
                    "error", "You are already a member of this team.");
            return "redirect:/teams";
        }

        if (requestRepository.existsByTeamAndUserAndStatus(
                team, user, "PENDING")) {
            redirectAttributes.addFlashAttribute(
                    "error", "You already have a pending request.");
            return "redirect:/teams";
        }

        TeamJoinRequest request = requestRepository
                .findByTeamAndUser(team, user)
                .orElse(new TeamJoinRequest());

        request.setTeam(team);
        request.setUser(user);
        request.setStatus("PENDING");

        requestRepository.save(request);

        redirectAttributes.addFlashAttribute(
                "success", "Join request sent successfully!");

        return "redirect:/teams";
    }
}