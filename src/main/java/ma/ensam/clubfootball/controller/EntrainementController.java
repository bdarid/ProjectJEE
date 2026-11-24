package ma.ensam.clubfootball.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.dto.FeuillePresenceForm;
import ma.ensam.clubfootball.entity.Entrainement;
import ma.ensam.clubfootball.enums.StatutPresence;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.service.EntrainementService;
import ma.ensam.clubfootball.service.EquipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/entrainements")
@RequiredArgsConstructor
public class EntrainementController {

    private final EntrainementService entrainementService;
    private final EquipeService equipeService;

    @GetMapping
    public String listerSeances(@RequestParam(required = false) Long equipeId, Model model) {
        model.addAttribute("seances", (equipeId != null)
                ? entrainementService.listerParEquipe(equipeId)
                : entrainementService.listerTous());
        model.addAttribute("equipes", equipeService.listerToutes());
        model.addAttribute("equipeId", equipeId);
        return "entrainements/list";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("entrainement", new Entrainement());
        model.addAttribute("equipes", equipeService.listerToutes());
        return "entrainements/form";
    }

    @PostMapping
    public String planifierSeance(@Valid @ModelAttribute("entrainement") Entrainement entrainement,
                                  BindingResult result,
                                  @RequestParam Long equipeId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("equipes", equipeService.listerToutes());
            return "entrainements/form";
        }
        try {
            entrainementService.planifierSeance(entrainement, equipeId);
            redirectAttributes.addFlashAttribute("succes", "Séance d'entraînement planifiée.");
            return "redirect:/entrainements";
        } catch (RegleGestionException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            model.addAttribute("equipes", equipeService.listerToutes());
            return "entrainements/form";
        }
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("entrainement", entrainementService.trouverParId(id));
        model.addAttribute("equipes", equipeService.listerToutes());
        return "entrainements/form";
    }

    @PostMapping("/{id}")
    public String modifierSeance(@PathVariable Long id,
                                 @Valid @ModelAttribute("entrainement") Entrainement entrainement,
                                 BindingResult result,
                                 @RequestParam Long equipeId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            entrainement.setId(id);
            model.addAttribute("equipes", equipeService.listerToutes());
            return "entrainements/form";
        }
        try {
            entrainementService.modifierSeance(id, entrainement, equipeId);
            redirectAttributes.addFlashAttribute("succes", "Séance modifiée avec succès.");
            return "redirect:/entrainements";
        } catch (RegleGestionException e) {
            entrainement.setId(id);
            model.addAttribute("erreurMetier", e.getMessage());
            model.addAttribute("equipes", equipeService.listerToutes());
            return "entrainements/form";
        }
    }

    @GetMapping("/{id}/presences")
    public String afficherFeuillePresence(@PathVariable Long id, Model model) {
        Entrainement seance = entrainementService.trouverParId(id);
        FeuillePresenceForm form = new FeuillePresenceForm();
        form.setPresences(entrainementService.preparerFeuillePresence(id));

        model.addAttribute("seance", seance);
        model.addAttribute("feuilleForm", form);
        model.addAttribute("statutsPresence", StatutPresence.values());
        return "entrainements/presences";
    }

    @PostMapping("/{id}/presences")
    public String enregistrerPresences(@PathVariable Long id,
                                       @ModelAttribute("feuilleForm") FeuillePresenceForm form,
                                       RedirectAttributes redirectAttributes) {
        try {
            entrainementService.enregistrerPresencesSeance(id, form.getPresences());
            redirectAttributes.addFlashAttribute("succes", "Feuille de présence enregistrée.");
            return "redirect:/entrainements/" + id + "/presences";
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/entrainements/" + id + "/presences";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerSeance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            entrainementService.supprimerSeance(id);
            redirectAttributes.addFlashAttribute("succes", "Séance supprimée.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/entrainements";
    }
}