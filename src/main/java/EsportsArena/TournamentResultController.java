
package EsportsArena;

import EsportsArena.entity.Tournament;
import EsportsArena.entity.TournamentResult;
import EsportsArena.entity.Team;
import EsportsArena.entity.User;

import EsportsArena.repository.TournamentRepository;
import EsportsArena.repository.TournamentResultRepository;
import EsportsArena.repository.TeamRepository;
import EsportsArena.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/tournaments")
public class TournamentResultController {

    private final TournamentRepository tournamentRepository;
    private final TournamentResultRepository resultRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TournamentResultController(
            TournamentRepository tournamentRepository,
            TournamentResultRepository resultRepository,
            TeamRepository teamRepository,
            UserRepository userRepository) {

        this.tournamentRepository = tournamentRepository;
        this.resultRepository = resultRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    private User getAuthorizedOrganiser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Optional<User> userOptional =
                userRepository.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if ("ADMIN".equalsIgnoreCase(user.getRole())
                || ("ORGANISER".equalsIgnoreCase(user.getRole())
                && user.isOrganiserApproved())) {
            return user;
        }

        return null;
    }

    @GetMapping("/{tournamentId}/results/manage")
    public String manageResults(
            @PathVariable Long tournamentId,
            Authentication authentication,
            Model model) {

        User user = getAuthorizedOrganiser(authentication);

        if (user == null) {
            return "redirect:/tournaments";
        }

        Optional<Tournament> tournamentOptional =
                tournamentRepository.findById(tournamentId);

        if (tournamentOptional.isEmpty()) {
            return "redirect:/tournaments";
        }

        Tournament tournament = tournamentOptional.get();

        List<Team> teams =
                teamRepository.findByGameName(tournament.getGameName());

        List<TournamentResult> results =
                resultRepository.findByTournament(tournament);

        model.addAttribute("tournament", tournament);
        model.addAttribute("teams", teams);
        model.addAttribute("results", results);

        return "manage-tournament-results";
    }

    @PostMapping("/{tournamentId}/results/save")
    public String saveResult(
            @PathVariable Long tournamentId,
            @RequestParam Long teamId,
            @RequestParam int matchNumber,
            @RequestParam int position,
            @RequestParam int kills,
            @RequestParam int placementPoints,
            @RequestParam int killPoints,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = getAuthorizedOrganiser(authentication);

        if (user == null) {
            return "redirect:/tournaments";
        }

        Optional<Tournament> tournamentOptional =
                tournamentRepository.findById(tournamentId);

        Optional<Team> teamOptional =
                teamRepository.findById(teamId);

        if (tournamentOptional.isEmpty() || teamOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Tournament or team not found.");

            return "redirect:/tournaments";
        }

        Tournament tournament = tournamentOptional.get();
        Team team = teamOptional.get();

        if (matchNumber < 1 || position < 1 || position > 12
                || kills < 0 || placementPoints < 0 || killPoints < 0) {

            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid result values.");

            return "redirect:/tournaments/" + tournamentId
                    + "/results/manage";
        }

        TournamentResult result = resultRepository
                .findByTournamentAndTeamAndMatchNumber(
                        tournament, team, matchNumber)
                .orElse(new TournamentResult());

        result.setTournament(tournament);
        result.setTeam(team);
        result.setMatchNumber(matchNumber);
        result.setPosition(position);
        result.setKills(kills);
        result.setPlacementPoints(placementPoints);
        result.setKillPoints(killPoints);
        result.setTotalPoints(placementPoints + killPoints);

        resultRepository.save(result);

        redirectAttributes.addFlashAttribute(
                "success", "Match result saved successfully.");

        return "redirect:/tournaments/" + tournamentId
                + "/results/manage";
    }

    @GetMapping("/{tournamentId}/leaderboard")
    public String leaderboard(
            @PathVariable Long tournamentId,
            Model model) {

        Optional<Tournament> tournamentOptional =
                tournamentRepository.findById(tournamentId);

        if (tournamentOptional.isEmpty()) {
            return "redirect:/tournaments";
        }

        Tournament tournament = tournamentOptional.get();
        if (!tournament.isLeaderboardPublished()) {
            return "redirect:/tournaments";
        }

        List<TournamentResult> results =
                resultRepository.findByTournament(tournament);

        Map<Team, int[]> leaderboard = new HashMap<>();

        for (TournamentResult result : results) {

            Team team = result.getTeam();

            int[] totals = leaderboard.computeIfAbsent(
                    team, key -> new int[4]);

            totals[0] += result.getKills();

            if (result.getPosition() == 1) {
                totals[1]++;
            }

            totals[2] += result.getPlacementPoints();
            totals[3] += result.getKillPoints();
        }

        List<Map.Entry<Team, int[]>> rankings =
                new ArrayList<>(leaderboard.entrySet());

        rankings.sort((a, b) ->
                Integer.compare(
                        b.getValue()[2] + b.getValue()[3],
                        a.getValue()[2] + a.getValue()[3]
                ));

        model.addAttribute("tournament", tournament);
        model.addAttribute("rankings", rankings);

        return "tournament-leaderboard";
    }
    @PostMapping("/{tournamentId}/results/publish")
    public String publishLeaderboard(
            @PathVariable Long tournamentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = getAuthorizedOrganiser(authentication);

        if (user == null) {
            return "redirect:/tournaments";
        }

        Optional<Tournament> tournamentOptional =
                tournamentRepository.findById(tournamentId);

        if (tournamentOptional.isEmpty()) {
            return "redirect:/tournaments";
        }

        Tournament tournament = tournamentOptional.get();

        List<TournamentResult> results =
                resultRepository.findByTournament(tournament);

        if (results.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Add match results before publishing.");

            return "redirect:/tournaments/" + tournamentId
                    + "/results/manage";
        }

        tournament.setLeaderboardPublished(true);
        tournamentRepository.save(tournament);

        redirectAttributes.addFlashAttribute(
                "success", "Leaderboard published successfully!");

        return "redirect:/tournaments/" + tournamentId
                + "/results/manage";
    }
    @GetMapping("/{tournamentId}/room/manage")
    public String manageRoom(
            @PathVariable Long tournamentId,
            Authentication authentication,
            Model model) {

        User user = getAuthorizedOrganiser(authentication);

        if (user == null) {
            return "redirect:/tournaments";
        }

        Optional<Tournament> tournamentOptional =
                tournamentRepository.findById(tournamentId);

        if (tournamentOptional.isEmpty()) {
            return "redirect:/tournaments";
        }

        model.addAttribute("tournament", tournamentOptional.get());

        return "manage-tournament-room";
    }

    @PostMapping("/{tournamentId}/room/save")
    public String saveRoomDetails(
            @PathVariable Long tournamentId,
            @RequestParam String roomId,
            @RequestParam String roomPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = getAuthorizedOrganiser(authentication);

        if (user == null) {
            return "redirect:/tournaments";
        }

        Optional<Tournament> tournamentOptional =
                tournamentRepository.findById(tournamentId);

        if (tournamentOptional.isEmpty()) {
            return "redirect:/tournaments";
        }

        if (roomId.isBlank() || roomPassword.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Room ID and Password are required.");

            return "redirect:/tournaments/" + tournamentId
                    + "/room/manage";
        }

        Tournament tournament = tournamentOptional.get();

        tournament.setRoomId(roomId.trim());
        tournament.setRoomPassword(roomPassword.trim());

        tournamentRepository.save(tournament);

        redirectAttributes.addFlashAttribute(
                "success", "Room details saved successfully!");

        return "redirect:/tournaments/" + tournamentId
                + "/room/manage";
    }
}
