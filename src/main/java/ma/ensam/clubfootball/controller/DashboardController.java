package ma.ensam.clubfootball.controller;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.dto.DashboardGlobalDTO;
import ma.ensam.clubfootball.dto.DashboardJoueurDTO;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String afficherDashboard(Authentication authentication, Model model) {
        boolean estJoueur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JOUEUR"));

        if (estJoueur) {
            try {
                DashboardJoueurDTO dashboardJoueur = dashboardService.construireDashboardJoueur(authentication.getName());
                model.addAttribute("dashboard", dashboardJoueur);
                return "dashboard/joueur";
            } catch (RegleGestionException e) {
                model.addAttribute("avertissement", e.getMessage());
            }
        }

        DashboardGlobalDTO dashboardGlobal = dashboardService.construireDashboardGlobal();
        model.addAttribute("dashboard", dashboardGlobal);
        return "dashboard/index";
    }
}