package ma.ensam.clubfootball.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Entraineur;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.service.EntraineurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/entraineurs")
@RequiredArgsConstructor
public class EntraineurController {

    private final EntraineurService entraineurService;

    @GetMapping
    public String listerEntraineurs(Model model) {
        model.addAttribute("entraineurs", entraineurService.listerTous());
        return "entraineurs/list";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("entraineur", new Entraineur());
        return "entraineurs/form";
    }

    @PostMapping
    public String creerEntraineur(@Valid @ModelAttribute("entraineur") Entraineur entraineur,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "entraineurs/form";
        }
        try {
            entraineurService.creerEntraineur(entraineur);
            redirectAttributes.addFlashAttribute("succes", "Entraîneur ajouté avec succès.");
            return "redirect:/entraineurs";
        } catch (RegleGestionException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            return "entraineurs/form";
        }
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("entraineur", entraineurService.trouverParId(id));
        return "entraineurs/form";
    }

    @PostMapping("/{id}")
    public String modifierEntraineur(@PathVariable Long id,
                                     @Valid @ModelAttribute("entraineur") Entraineur entraineur,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            entraineur.setId(id);
            return "entraineurs/form";
        }
        try {
            entraineurService.modifierEntraineur(id, entraineur);
            redirectAttributes.addFlashAttribute("succes", "Entraîneur modifié avec succès.");
            return "redirect:/entraineurs";
        } catch (RegleGestionException e) {
            entraineur.setId(id);
            model.addAttribute("erreurMetier", e.getMessage());
            return "entraineurs/form";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerEntraineur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            entraineurService.supprimerEntraineur(id);
            redirectAttributes.addFlashAttribute("succes", "Entraîneur supprimé (les équipes restent sans référent).");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/entraineurs";
    }
}