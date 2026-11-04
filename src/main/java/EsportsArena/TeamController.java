package EsportsArena;

import EsportsArena.entity.Team;
import EsportsArena.entity.TeamMember;
import EsportsArena.entity.TeamJoinRequest;
import EsportsArena.entity.TournamentResult;
import EsportsArena.entity.User;

import EsportsArena.repository.TeamMemberRepository;
import EsportsArena.repository.TeamJoinRequestRepository;
import EsportsArena.repository.TeamRepository;
import EsportsArena.repository.TournamentResultRepository;
import EsportsArena.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamJoinRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final TournamentResultRepository tournamentResultRepository;

    public TeamController(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            TeamJoinRequestRepository requestRepository,
            UserRepository userRepository,
            TournamentResultRepository tournamentResultRepository) {

        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.tournamentResultRepository = tournamentResultRepository;
    }

    // SHOW CREATE TEAM PAGE
    @GetMapping("/create")
    public String showCreateTeamPage() {
        return "create-team";
    }

    // CREATE TEAM
    @PostMapping("/create")
    public String createTeam(
            @RequestParam String teamName,
            @RequestParam String gameName,
            @RequestParam(required = false) String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User leader = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newTeamName = teamName.trim();

        if (newTeamName.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team name cannot be empty.");
            return "redirect:/teams/create";
        }

        if (teamRepository.existsByTeamNameAndGameName(newTeamName, gameName)) {
            redirectAttributes.addFlashAttribute(
                    "error", "A team with this name already exists for this game.");
            return "redirect:/teams/create";
        }

        Team team = new Team();
        team.setTeamName(newTeamName);
        team.setGameName(gameName);
        team.setDescription(description);
        team.setLeader(leader);

        Team savedTeam = teamRepository.save(team);

        TeamMember member = new TeamMember();
        member.setTeam(savedTeam);
        member.setUser(leader);

        teamMemberRepository.save(member);

        redirectAttributes.addFlashAttribute(
                "success", "Your team has been created successfully!");

        return "redirect:/teams/dashboard/" + savedTeam.getId();
    }

    // SHOW TEAM DASHBOARD
    @GetMapping("/dashboard/{teamId}")
    public String showTeamDashboard(
            @PathVariable Long teamId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = teamRepository.findById(teamId).orElse(null);

        if (team == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team not found.");
            return "redirect:/teams";
        }

        if (!team.getLeader().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Only the team leader can access this dashboard.");
            return "redirect:/teams";
        }

        List<TeamMember> members = teamMemberRepository.findByTeam(team);

        List<TeamJoinRequest> requests =
                requestRepository.findByTeamAndStatus(team, "PENDING");

        model.addAttribute("team", team);
        model.addAttribute("members", members);
        model.addAttribute("requests", requests);

        // TEAM STATISTICS
        List<TournamentResult> results =
                tournamentResultRepository.findByTeam(team);

        int totalWins = (int) results.stream()
                .filter(result -> result.getPosition() == 1)
                .count();

        int totalKills = results.stream()
                .mapToInt(TournamentResult::getKills)
                .sum();

        int totalPoints = results.stream()
                .mapToInt(TournamentResult::getTotalPoints)
                .sum();

        model.addAttribute("totalWins", totalWins);
        model.addAttribute("totalKills", totalKills);
        model.addAttribute("totalPoints", totalPoints);

        // TEAM ACHIEVEMENTS
        List<String> achievements = new ArrayList<>();

        if (totalWins >= 1) {
            achievements.add("First Victory");
        }

        if (totalWins >= 5) {
            achievements.add("5 Tournament Wins");
        }

        if (totalWins >= 10) {
            achievements.add("10 Tournament Wins");
        }

        if (totalKills >= 100) {
            achievements.add("100 Kills Club");
        }

        model.addAttribute("achievements", achievements);

        return "team-dashboard";
    }

    // SHOW EDIT TEAM PAGE
    @GetMapping("/edit/{teamId}")
    public String showEditTeamPage(
            @PathVariable Long teamId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = teamRepository.findById(teamId).orElse(null);

        if (team == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team not found.");
            return "redirect:/teams";
        }

        if (!team.getLeader().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Only the team leader can edit this team.");
            return "redirect:/teams";
        }

        model.addAttribute("team", team);

        return "edit-team";
    }

    // UPDATE TEAM DETAILS
    @PostMapping("/edit/{teamId}")
    public String updateTeam(
            @PathVariable Long teamId,
            @RequestParam String teamName,
            @RequestParam(required = false) String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Team team = teamRepository.findById(teamId).orElse(null);

        if (team == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team not found.");
            return "redirect:/teams";
        }

        if (!team.getLeader().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Only the team leader can edit this team.");
            return "redirect:/teams";
        }

        String updatedTeamName = teamName.trim();

        if (updatedTeamName.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team name cannot be empty.");
            return "redirect:/teams/edit/" + teamId;
        }

        boolean duplicateName = teamRepository.findByGameName(team.getGameName())
                .stream()
                .anyMatch(existingTeam ->
                        !existingTeam.getId().equals(teamId)
                        && existingTeam.getTeamName().equalsIgnoreCase(updatedTeamName));

        if (duplicateName) {
            redirectAttributes.addFlashAttribute(
                    "error", "Another team already uses this name for this game.");
            return "redirect:/teams/edit/" + teamId;
        }

        team.setTeamName(updatedTeamName);
        team.setDescription(description);

        teamRepository.save(team);

        redirectAttributes.addFlashAttribute(
                "success", "Team details updated successfully!");

        return "redirect:/teams/dashboard/" + teamId;
    }

    // APPROVE JOIN REQUEST
    @PostMapping("/requests/{requestId}/approve")
    public String approveRequest(
            @PathVariable Long requestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User leader = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        TeamJoinRequest request = requestRepository.findById(requestId)
                .orElse(null);

        if (request == null ||
                !request.getTeam().getLeader().getId().equals(leader.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Invalid join request.");
            return "redirect:/teams";
        }

        Team team = request.getTeam();

        if (!"PENDING".equals(request.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "error", "This request has already been processed.");
            return "redirect:/teams/dashboard/" + team.getId();
        }

        if (teamMemberRepository.countByTeam(team) >= 5) {
            redirectAttributes.addFlashAttribute(
                    "error", "Your team is already full.");
            return "redirect:/teams/dashboard/" + team.getId();
        }

        if (teamMemberRepository.findByUser(request.getUser()).stream()
                .anyMatch(member ->
                        member.getTeam().getGameName().equals(team.getGameName()))) {
            redirectAttributes.addFlashAttribute(
                    "error", "This player already belongs to a team for this game.");
            return "redirect:/teams/dashboard/" + team.getId();
        }

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(request.getUser());

        teamMemberRepository.save(member);

        request.setStatus("ACCEPTED");
        requestRepository.save(request);

        redirectAttributes.addFlashAttribute(
                "success", "Player added to your team successfully!");

        return "redirect:/teams/dashboard/" + team.getId();
    }

    // REJECT JOIN REQUEST
    @PostMapping("/requests/{requestId}/reject")
    public String rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User leader = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        TeamJoinRequest request = requestRepository.findById(requestId)
                .orElse(null);

        if (request == null ||
                !request.getTeam().getLeader().getId().equals(leader.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Invalid join request.");
            return "redirect:/teams";
        }

        if (!"PENDING".equals(request.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "error", "This request has already been processed.");
            return "redirect:/teams/dashboard/" + request.getTeam().getId();
        }

        request.setStatus("REJECTED");
        requestRepository.save(request);

        redirectAttributes.addFlashAttribute(
                "success", "Join request rejected.");

        return "redirect:/teams/dashboard/" + request.getTeam().getId();
    }

    // REMOVE TEAM MEMBER - LEADER ONLY
    @PostMapping("/members/{memberId}/remove")
    public String removeMember(
            @PathVariable Long memberId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User leader = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElse(null);

        if (member == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team member not found.");
            return "redirect:/teams";
        }

        Team team = member.getTeam();

        if (!team.getLeader().getId().equals(leader.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Only the team leader can remove members.");
            return "redirect:/teams";
        }

        if (member.getUser().getId().equals(team.getLeader().getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "The team leader cannot be removed.");
            return "redirect:/teams/dashboard/" + team.getId();
        }

        teamMemberRepository.delete(member);

        redirectAttributes.addFlashAttribute(
                "success", "Team member removed successfully.");

        return "redirect:/teams/dashboard/" + team.getId();
    }

    // LEAVE TEAM - REGULAR MEMBERS ONLY
    @PostMapping("/members/leave/{teamId}")
    public String leaveTeam(
            @PathVariable Long teamId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        Team team = teamRepository.findById(teamId).orElse(null);

        if (team == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Team not found.");
            return "redirect:/teams";
        }

        if (team.getLeader().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute(
                    "error", "The team leader cannot leave the team.");
            return "redirect:/teams/dashboard/" + teamId;
        }

        TeamMember member = teamMemberRepository
                .findByTeamAndUser(team, user)
                .orElse(null);

        if (member == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "You are not a member of this team.");
            return "redirect:/teams";
        }

        teamMemberRepository.delete(member);

        redirectAttributes.addFlashAttribute(
                "success", "You have left the team successfully.");

        return "redirect:/teams";
    }
}