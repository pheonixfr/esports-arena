package EsportsArena;

import EsportsArena.entity.Game;
import EsportsArena.entity.Team;
import EsportsArena.entity.Tournament;
import EsportsArena.entity.User;
import EsportsArena.entity.PlayerGameProfile;

import EsportsArena.repository.GameRepository;
import EsportsArena.repository.TeamRepository;
import EsportsArena.repository.TournamentRepository;
import EsportsArena.repository.TournamentRegistrationRepository;
import EsportsArena.repository.UserRepository;
import EsportsArena.repository.PlayerGameProfileRepository;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final PlayerGameProfileRepository playerGameProfileRepository;

    public HomeController(
            GameRepository gameRepository,
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository,
            TournamentRegistrationRepository registrationRepository,
            UserRepository userRepository,
            PlayerGameProfileRepository playerGameProfileRepository) {

        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.playerGameProfileRepository = playerGameProfileRepository;
    }

    // HOME PAGE
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {

        List<Game> games = gameRepository.findAll();
        model.addAttribute("games", games);

        String loggedInUserRole = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            User user = userRepository.findByEmail(authentication.getName())
                    .orElse(null);

            if (user != null) {
                loggedInUserRole = user.getRole();
            }
        }

        model.addAttribute("loggedInUserRole", loggedInUserRole);

        return "index";
    }

    // TOURNAMENTS PAGE
    @GetMapping("/tournaments")
    public String tournaments(
            Model model,
            Authentication authentication) {

        List<Tournament> tournaments = tournamentRepository.findAll();

        boolean canCreateTournament = false;
        boolean isLoggedIn = false;

        User currentUser = null;
        List<Team> myTeams = List.of();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            currentUser = userRepository.findByEmail(authentication.getName())
                    .orElse(null);

            if (currentUser != null) {
                isLoggedIn = true;

                boolean isAdmin =
                        "ADMIN".equalsIgnoreCase(currentUser.getRole());

                boolean isApprovedOrganiser =
                        "ORGANISER".equalsIgnoreCase(currentUser.getRole())
                        && currentUser.isOrganiserApproved();

                canCreateTournament = isAdmin || isApprovedOrganiser;

                myTeams = teamRepository.findByLeader(currentUser);
            }
        }

        Map<Long, Long> registrationCounts = new HashMap<>();
        Map<Long, Boolean> registeredTournaments = new HashMap<>();

        for (Tournament tournament : tournaments) {

            registrationCounts.put(
                    tournament.getId(),
                    registrationRepository.countByTournament(tournament)
            );

            boolean alreadyRegistered = currentUser != null
                    && registrationRepository.existsByTournamentAndUser(
                            tournament, currentUser);

            registeredTournaments.put(
                    tournament.getId(),
                    alreadyRegistered
            );
        }

        model.addAttribute("tournaments", tournaments);
        model.addAttribute("canCreateTournament", canCreateTournament);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("registrationCounts", registrationCounts);
        model.addAttribute("registeredTournaments", registeredTournaments);
        model.addAttribute("myTeams", myTeams);

        return "tournaments";
    }

    // PLAYERS PAGE
    @GetMapping("/players")
    public String players(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String game,
            Model model) {

        List<PlayerGameProfile> playerProfiles =
                playerGameProfileRepository.findAll();

        // Show only verified player accounts
        playerProfiles = playerProfiles.stream()
                .filter(profile ->
                        profile.getUser().isEmailVerified()
                        && "PLAYER".equalsIgnoreCase(
                                profile.getUser().getRole()))
                .collect(Collectors.toList());

        // Search by username
        if (search != null && !search.trim().isEmpty()) {

            String searchText = search.trim().toLowerCase();

            playerProfiles = playerProfiles.stream()
                    .filter(profile ->
                            profile.getUser().getUsername()
                                    .toLowerCase()
                                    .contains(searchText))
                    .collect(Collectors.toList());
        }

        // Filter by game
        if (game != null && !game.trim().isEmpty()) {

            playerProfiles = playerProfiles.stream()
                    .filter(profile ->
                            profile.getGameName().equalsIgnoreCase(game.trim()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("playerProfiles", playerProfiles);
        model.addAttribute("games", gameRepository.findAll());
        model.addAttribute("search", search);
        model.addAttribute("selectedGame", game);

        return "players";
    }

    // TEAMS PAGE
    @GetMapping("/teams")
    public String teams(
            Model model,
            Authentication authentication) {

        List<Team> teams = teamRepository.findAll();

        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        User currentUser = null;

        if (isLoggedIn) {
            currentUser = userRepository.findByEmail(authentication.getName())
                    .orElse(null);

            isLoggedIn = currentUser != null;
        }

        model.addAttribute("teams", teams);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("currentUser", currentUser);

        return "teams";
    }

    // GAME DETAILS PAGE
    @GetMapping("/games/{id}")
    public String gameDetails(
            @PathVariable Long id,
            Model model) {

        Optional<Game> game = gameRepository.findById(id);

        if (game.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("game", game.get());

        List<Tournament> gameTournaments =
                tournamentRepository.findByGameName(game.get().getName());

        model.addAttribute("gameTournaments", gameTournaments);

        return "game-details";
    }
}