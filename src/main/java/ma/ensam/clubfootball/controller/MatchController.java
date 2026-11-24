package ma.ensam.clubfootball.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.dto.ParticipationsForm;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Match;
import ma.ensam.clubfootball.entity.ParticipationMatch;
import ma.ensam.clubfootball.enums.StatutMatch;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.ParticipationMatchRepository;
import ma.ensam.clubfootball.service.EquipeService;
import ma.ensam.clubfootball.service.MatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/matchs")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final EquipeService equipeService;
    private final ParticipationMatchRepository participationRepository;

    @GetMapping
    public String listerMatchs(@RequestParam(required = false) Long equipeId, Model model) {
        model.addAttribute("matchs", (equipeId != null)
                ? matchService.listerParEquipe(equipeId)
                : matchService.listerTous());
        model.addAttribute("equipes", equipeService.listerToutes());
        model.addAttribute("equipeId", equipeId);
        return "matchs/list";
    }

    @GetMapping("/{id}")
    public String detailMatch(@PathVariable Long id, Model model) {
        Match match = matchService.trouverParId(id);
        List<Joueur> effectif = equipeService.consulterEffectif(match.getEquipe().getId());

        Map<Long, ParticipationMatch> existantes = participationRepository.findByMatchId(id).stream()
                .collect(Collectors.toMap(pm -> pm.getJoueur().getId(), Function.identity()));

        List<ParticipationMatch> lignes = new ArrayList<>();
        for (Joueur joueur : effectif) {
            ParticipationMatch pm = existantes.get(joueur.getId());
            if (pm == null) {
                pm = new ParticipationMatch();
                pm.setMatch(match);
                pm.setJoueur(joueur);
            }
            lignes.add(pm);
        }

        ParticipationsForm form = new ParticipationsForm();
        form.setParticipations(lignes);

        model.addAttribute("match", match);
        model.addAttribute("participationsForm", form);
        model.addAttribute("statutsMatch", StatutMatch.values());
        return "matchs/detail";
    }

    @GetMapping("/nouveau")
    public String formulaireCreation(Model model) {
        model.addAttribute("match", new Match());
        model.addAttribute("equipes", equipeService.listerToutes());
        model.addAttribute("statutsMatch", StatutMatch.values());
        return "matchs/form";
    }

    @PostMapping
    public String planifierMatch(@Valid @ModelAttribute("match") Match match,
                                 BindingResult result,
                                 @RequestParam Long equipeId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("equipes", equipeService.listerToutes());
            model.addAttribute("statutsMatch", StatutMatch.values());
            return "matchs/form";
        }
        try {
            matchService.planifierMatch(match, equipeId);
            redirectAttributes.addFlashAttribute("succes", "Match enregistré avec succès.");
            return "redirect:/matchs";
        } catch (RegleGestionException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            model.addAttribute("equipes", equipeService.listerToutes());
            model.addAttribute("statutsMatch", StatutMatch.values());
            return "matchs/form";
        }
    }

    @PostMapping("/{id}/resultat")
    public String saisirResultat(@PathVariable Long id,
                                 @RequestParam Integer butsEquipe,
                                 @RequestParam Integer butsAdversaire,
                                 RedirectAttributes redirectAttributes) {
        try {
            matchService.saisirResultatMatch(id, butsEquipe, butsAdversaire);
            redirectAttributes.addFlashAttribute("succes", "Score enregistré et résultat déduit.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/matchs/" + id;
    }

    @PostMapping("/{id}/participations")
    public String enregistrerParticipations(@PathVariable Long id,
                                            @ModelAttribute("participationsForm") ParticipationsForm form,
                                            RedirectAttributes redirectAttributes) {
        try {
            matchService.enregistrerParticipations(id, form.getParticipations());
            redirectAttributes.addFlashAttribute("succes", "Participations individuelles enregistrées.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/matchs/" + id;
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerMatch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            matchService.supprimerMatch(id);
            redirectAttributes.addFlashAttribute("succes", "Match supprimé.");
        } catch (RegleGestionException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/matchs";
    }
}