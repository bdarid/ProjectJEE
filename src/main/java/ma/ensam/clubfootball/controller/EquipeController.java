package ma.ensam.clubfootball.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.service.EntraineurService;
import ma.ensam.clubfootball.service.EquipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;
    private final EntraineurService entraineurService;

    @GetMapping
    public String listerEquipes(@RequestParam(required = false) String nom, Model model) {
        List<Equipe> equipes = equipeService.listerToutes();
        if (nom != null && !nom.isBlank()) {
            String filtre = nom.trim().toLowerCase();
            equipes = equipes.stream()
                    .filter(e -> e.getNom().toLowerCase().contains(filtre)
                            || e.getSaison().toLowerCase().contains(filtre))
                    .toList();
        }

        model.addAttribute("equipes", equipes);
        model.addAttribute("nom", nom);
        return "equipes/list";
    }

    @GetMapping("/{id}")
    public String detailEquipe(@PathVariable Long id, Model model) {
        Equipe equipe = equipeService.trouverParId(id);
        model.addAttribute("equipe", equipe);
        model.addAttribute("effectif", equipeService.consulterEffectif(id));
        model.addAttribute("bilan", equipeService.calculerBilanEquipe(id));
        return "equipes/detail";
    }

    @GetMapping("/nouvelle")
    public String formulaireCreation(Model model) {
        model.addAttribute("equipe", new Equipe());
        model.addAttribute("entraineurs", entraineurService.listerTous());
        return "equipes/form";
    }

    @PostMapping
    public String creerEquipe(@Valid @ModelAttribute("equipe") Equipe equipe,
                              BindingResult result,
                              @RequestParam(required = false) Long entraineurId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("entraineurs", entraineurService.listerTous());
            return "equipes/form";
        }
        try {
            equipeService.creerEquipe(equipe, entraineurId);
            redirectAttributes.addFlashAttribute("succes", "Équipe créée avec succès.");
            return "redirect:/equipes";
        } catch (RegleGestionException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            model.addAttribute("entraineurs", entraineurService.listerTous());
            return "equipes/form";
        }
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("equipe", equipeService.trouverParId(id));
        model.addAttribute("entraineurs", entraineurService.listerTous());
        return "equipes/form";
    }

    @PostMapping("/{id}")
    public String modifierEquipe(@PathVariable Long id,
                                 @Valid @ModelAttribute("equipe") Equipe equipe,
                                 BindingResult result,
                                 @RequestParam(required = false) Long entraineurId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            equipe.setId(id);
            model.addAttribute("entraineurs", entraineurService.listerTous());
            return "equipes/form";
        }
        try {
            equipeService.modifierEquipe(id, equipe, entraineurId);
            redirectAttributes.addFlashAttribute("succes", "Équipe mise à jour.");
            return "redirect:/equipes/" + id;
        } catch (RegleGestionException e) {
            equipe.setId(id);
            model.addAttribute("erreurMetier", e.getMessage());
            model.addAttribute("entraineurs", entraineurService.listerTous());
            return "equipes/form";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerEquipe(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipeService.supprimerEquipe(id);
            redirectAttributes.addFlashAttribute("succes", "Équipe supprimée avec succès.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/equipes";
    }
}