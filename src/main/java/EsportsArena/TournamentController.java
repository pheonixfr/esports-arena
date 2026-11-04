
package EsportsArena;

import EsportsArena.entity.Tournament;
import EsportsArena.entity.TournamentRegistration;
import EsportsArena.entity.PlayerGameProfile;
import EsportsArena.entity.Team;
import EsportsArena.entity.TeamMember;
import EsportsArena.entity.User;

import EsportsArena.repository.TournamentRepository;
import EsportsArena.repository.TournamentRegistrationRepository;
import EsportsArena.repository.PlayerGameProfileRepository;
import EsportsArena.repository.UserRepository;
import EsportsArena.repository.TeamRepository;
import EsportsArena.repository.TeamMemberRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TournamentController {

    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final PlayerGameProfileRepository gameProfileRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TournamentController(
            TournamentRepository tournamentRepository,
            TournamentRegistrationRepository registrationRepository,
            PlayerGameProfileRepository gameProfileRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository) {

        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.gameProfileRepository = gameProfileRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    private User getAuthorizedOrganiser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return null;
        }

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(user.getRole());

        boolean isApprovedOrganiser =
                "ORGANISER".equalsIgnoreCase(user.getRole())
                && user.isOrganiserApproved();

        return (isAdmin || isApprovedOrganiser) ? user : null;
    }

    @GetMapping("/tournaments/create")
    public String createTournamentPage(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (getAuthorizedOrganiser(authentication) == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not authorized to create tournaments.");
            return "redirect:/tournaments";
        }

        return "create-tournament";
    }

    @PostMapping("/tournaments/create")
    public String createTournament(
            @RequestParam String tournamentName,
            @RequestParam String gameName,
            @RequestParam String registrationType,
            @RequestParam String matchMode,
            @RequestParam Integer maxSlots,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String bannerImage,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime registrationDeadline,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDateTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User organiser = getAuthorizedOrganiser(authentication);

        if (organiser == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not authorized to create tournaments.");
            return "redirect:/tournaments";
        }

        if (tournamentName == null
                || tournamentName.isBlank()
                || maxSlots == null
                || maxSlots < 2
                || maxSlots > 1000
                || registrationDeadline == null
                || startDateTime == null
                || !registrationDeadline.isAfter(LocalDateTime.now())
                || !startDateTime.isAfter(registrationDeadline)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please enter valid tournament details and dates.");

            return "redirect:/tournaments/create";
        }

        Tournament tournament = new Tournament();

        tournament.setTournamentName(tournamentName.trim());
        tournament.setGameName(gameName);
        tournament.setRegistrationType(registrationType);
        tournament.setMatchMode(matchMode);
        tournament.setMaxSlots(maxSlots);
        tournament.setDescription(description);
        tournament.setBannerImage(bannerImage);
        tournament.setRegistrationDeadline(registrationDeadline);
        tournament.setStartDateTime(startDateTime);
        tournament.setOrganiser(organiser);
        tournament.setStatus("UPCOMING");

        tournamentRepository.save(tournament);

        redirectAttributes.addFlashAttribute(
                "success",
                "Tournament created successfully!");

        return "redirect:/tournaments";
    }

    @PostMapping("/tournaments/{id}/register")
    public String registerForTournament(
            @PathVariable Long id,
            @RequestParam(required = false) Long teamId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        if (!"PLAYER".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Only players can register for tournaments.");
            return "redirect:/tournaments";
        }

        Tournament tournament = tournamentRepository.findById(id)
                .orElse(null);

        if (tournament == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Tournament not found.");
            return "redirect:/tournaments";
        }

        if (tournament.getRegistrationDeadline() == null
                || !tournament.getRegistrationDeadline()
                        .isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Registration deadline has passed.");
            return "redirect:/tournaments";
        }

        if (registrationRepository.countByTournament(tournament)
                >= tournament.getMaxSlots()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Tournament slots are full.");
            return "redirect:/tournaments";
        }

        if ("SOLO".equalsIgnoreCase(tournament.getRegistrationType())) {

            if (teamId != null) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "This tournament requires individual registration.");
                return "redirect:/tournaments";
            }

            if (registrationRepository.existsByTournamentAndUser(tournament, user)) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "You are already registered.");
                return "redirect:/tournaments";
            }

            if (!hasRequiredGameId(user, tournament.getGameName())) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Add your " + tournament.getGameName()
                        + " Game ID in My Game Profiles before registering.");
                return "redirect:/tournaments";
            }

            TournamentRegistration registration =
                    new TournamentRegistration();

            registration.setTournament(tournament);
            registration.setUser(user);

            registrationRepository.save(registration);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Tournament registration successful!");

            return "redirect:/tournaments";
        }

        if ("TEAM".equalsIgnoreCase(tournament.getRegistrationType())) {

            if (teamId == null) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Please select a team to register.");
                return "redirect:/tournaments";
            }

            Team team = teamRepository.findById(teamId).orElse(null);

            if (team == null) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Team not found.");
                return "redirect:/tournaments";
            }

            if (team.getLeader() == null
                    || !team.getLeader().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Only the team leader can register this team.");
                return "redirect:/tournaments";
            }

            if (!team.getGameName().equalsIgnoreCase(tournament.getGameName())) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "This team belongs to a different game.");
                return "redirect:/tournaments";
            }

            if (registrationRepository.existsByTournamentAndTeam(tournament, team)) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "This team is already registered.");
                return "redirect:/tournaments";
            }

            List<User> members = new ArrayList<>();
            members.add(team.getLeader());

            for (TeamMember member : teamMemberRepository.findByTeam(team)) {
                if (members.stream().noneMatch(
                        existing -> existing.getId().equals(member.getUser().getId()))) {
                    members.add(member.getUser());
                }
            }

            for (User member : members) {
                if (!hasRequiredGameId(member, tournament.getGameName())) {
                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Every team member must add their "
                            + tournament.getGameName()
                            + " Game ID before the team can register.");
                    return "redirect:/tournaments";
                }
            }

            TournamentRegistration registration =
                    new TournamentRegistration();

            registration.setTournament(tournament);
            registration.setUser(user);
            registration.setTeam(team);

            registrationRepository.save(registration);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Team registered successfully!");

            return "redirect:/tournaments";
        }

        redirectAttributes.addFlashAttribute(
                "error",
                "Invalid tournament registration type.");

        return "redirect:/tournaments";
    }

    private boolean hasRequiredGameId(User user, String gameName) {

        PlayerGameProfile gameProfile = gameProfileRepository
                .findByUserAndGameName(user, gameName)
                .orElse(null);

        return gameProfile != null
                && gameProfile.getGamePlayerId() != null
                && !gameProfile.getGamePlayerId().isBlank();
    }

    // View room details - registered participants only
    @GetMapping("/tournaments/{id}/room")
    public String viewTournamentRoom(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        Tournament tournament = tournamentRepository.findById(id)
                .orElse(null);

        if (tournament == null) {
            return "redirect:/tournaments";
        }
        // Allow room access only 5 minutes before tournament starts
        if (tournament.getStartDateTime() == null
                || LocalDateTime.now().isBefore(
                        tournament.getStartDateTime().minusMinutes(5))) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Room details will be available 5 minutes before the tournament starts.");

        return "redirect:/tournaments";
        }

        boolean registered = false;

        List<TournamentRegistration> registrations =
                registrationRepository.findByTournament(tournament);

        for (TournamentRegistration registration : registrations) {

            if (registration.getUser() != null
                    && registration.getUser().getId().equals(user.getId())) {
                registered = true;
                break;
            }

            Team team = registration.getTeam();

            if (team != null) {

                if (team.getLeader() != null
                        && team.getLeader().getId().equals(user.getId())) {
                    registered = true;
                    break;
                }

                boolean isMember = teamMemberRepository.findByTeam(team)
                        .stream()
                        .anyMatch(member ->
                                member.getUser().getId().equals(user.getId()));

                if (isMember) {
                    registered = true;
                    break;
                }
            }
        }

        if (!registered) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not registered for this tournament.");

            return "redirect:/tournaments";
        }

        if (tournament.getRoomId() == null
                || tournament.getRoomId().isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Room details have not been added yet.");

            return "redirect:/tournaments";
        }

        model.addAttribute("tournament", tournament);

        return "tournament-room-details";
    }
}
