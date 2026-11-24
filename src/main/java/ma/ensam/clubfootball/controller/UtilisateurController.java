package ma.ensam.clubfootball.controller;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.enums.Role;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.service.EntraineurService;
import ma.ensam.clubfootball.service.JoueurService;
import ma.ensam.clubfootball.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final JoueurService joueurService;
    private final EntraineurService entraineurService;

    @GetMapping
    public String listerComptes(Model model) {
        model.addAttribute("utilisateurs", utilisateurService.listerTous());
        model.addAttribute("roles", Role.values());
        return "utilisateurs/list";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        chargerListes(model);
        return "utilisateurs/form";
    }

    @PostMapping
    public String creerCompte(@RequestParam String username,
                              @RequestParam String motDePasse,
                              @RequestParam Role role,
                              @RequestParam(required = false) Long joueurId,
                              @RequestParam(required = false) Long entraineurId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.creerCompte(username, motDePasse, role, joueurId, entraineurId);
            redirectAttributes.addFlashAttribute("succes", "Compte utilisateur créé avec succès.");
            return "redirect:/utilisateurs";
        } catch (RegleGestionException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            chargerListes(model);
            return "utilisateurs/form";
        }
    }

    @PostMapping("/{id}/statut")
    public String modifierRoleEtStatut(@PathVariable Long id,
                                       @RequestParam Role role,
                                       @RequestParam(defaultValue = "false") boolean actif,
                                       RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.modifierRoleEtStatut(id, role, actif);
            redirectAttributes.addFlashAttribute("succes", "Compte mis à jour.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/utilisateurs";
    }

    @PostMapping("/{id}/mot-de-passe")
    public String reinitialiserMotDePasse(@PathVariable Long id,
                                          @RequestParam String nouveauMotDePasse,
                                          RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.reinitialiserMotDePasse(id, nouveauMotDePasse);
            redirectAttributes.addFlashAttribute("succes", "Mot de passe réinitialisé.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/utilisateurs";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerCompte(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.supprimerCompte(id);
            redirectAttributes.addFlashAttribute("succes", "Compte supprimé.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/utilisateurs";
    }

    private void chargerListes(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("joueurs", joueurService.listerTous());
        model.addAttribute("entraineurs", entraineurService.listerTous());
    }
}