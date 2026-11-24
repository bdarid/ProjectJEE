package ma.ensam.clubfootball.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.enums.PosteJoueur;
import ma.ensam.clubfootball.enums.StatutJoueur;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.service.EquipeService;
import ma.ensam.clubfootball.service.JoueurService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/joueurs")
@RequiredArgsConstructor
public class JoueurController {

    private final JoueurService joueurService;
    private final EquipeService equipeService;

    @GetMapping
    public String listerJoueurs(@RequestParam(required = false) String nom,
                                @RequestParam(required = false) PosteJoueur poste,
                                @RequestParam(required = false) Long equipeId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Page<Joueur> pageJoueurs = joueurService.rechercherJoueurs(
                nom, poste, equipeId, PageRequest.of(page, size, Sort.by("nom", "prenom")));

        model.addAttribute("pageJoueurs", pageJoueurs);
        model.addAttribute("nom", nom);
        model.addAttribute("poste", poste);
        model.addAttribute("equipeId", equipeId);
        model.addAttribute("postes", PosteJoueur.values());
        model.addAttribute("equipes", equipeService.listerToutes());
        return "joueurs/list";
    }

    @GetMapping("/{id}")
    public String detailJoueur(@PathVariable Long id, Model model) {
        Joueur joueur = joueurService.trouverParId(id);
        model.addAttribute("joueur", joueur);
        model.addAttribute("statistiques", joueurService.calculerStatistiquesJoueur(id));
        return "joueurs/detail";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("joueur", new Joueur());
        chargerListesFormulaire(model);
        return "joueurs/form";
    }

    @PostMapping
    public String creerJoueur(@Valid @ModelAttribute("joueur") Joueur joueur,
                              BindingResult result,
                              @RequestParam(required = false) Long equipeId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            chargerListesFormulaire(model);
            return "joueurs/form";
        }
        try {
            joueurService.creerJoueur(joueur, equipeId);
            redirectAttributes.addFlashAttribute("succes", "Joueur créé avec succès.");
            return "redirect:/joueurs";
        } catch (RegleGestionException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            chargerListesFormulaire(model);
            return "joueurs/form";
        }
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("joueur", joueurService.trouverParId(id));
        chargerListesFormulaire(model);
        return "joueurs/form";
    }

    @PostMapping("/{id}")
    public String modifierJoueur(@PathVariable Long id,
                                 @Valid @ModelAttribute("joueur") Joueur joueur,
                                 BindingResult result,
                                 @RequestParam(required = false) Long equipeId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            joueur.setId(id);
            chargerListesFormulaire(model);
            return "joueurs/form";
        }
        try {
            joueurService.modifierJoueur(id, joueur, equipeId);
            redirectAttributes.addFlashAttribute("succes", "Fiche joueur mise à jour.");
            return "redirect:/joueurs/" + id;
        } catch (RegleGestionException e) {
            joueur.setId(id);
            model.addAttribute("erreurMetier", e.getMessage());
            chargerListesFormulaire(model);
            return "joueurs/form";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerJoueur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            joueurService.supprimerJoueur(id);
            redirectAttributes.addFlashAttribute("succes", "Joueur supprimé avec succès.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/joueurs";
    }

    private void chargerListesFormulaire(Model model) {
        model.addAttribute("postes", PosteJoueur.values());
        model.addAttribute("statuts", StatutJoueur.values());
        model.addAttribute("equipes", equipeService.listerToutes());
    }
}