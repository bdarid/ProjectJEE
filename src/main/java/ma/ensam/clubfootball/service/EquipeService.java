package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.dto.BilanEquipeDTO;
import ma.ensam.clubfootball.entity.Entraineur;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Match;
import ma.ensam.clubfootball.enums.ResultatMatch;
import ma.ensam.clubfootball.enums.StatutMatch;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EntraineurRepository;
import ma.ensam.clubfootball.repository.EntrainementRepository;
import ma.ensam.clubfootball.repository.EquipeRepository;
import ma.ensam.clubfootball.repository.JoueurRepository;
import ma.ensam.clubfootball.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final EntraineurRepository entraineurRepository;
    private final JoueurRepository joueurRepository;
    private final EntrainementRepository entrainementRepository;
    private final MatchRepository matchRepository;

    public List<Equipe> listerToutes() {
        return equipeRepository.findAll();
    }

    public Equipe trouverParId(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RegleGestionException("Équipe introuvable (id=" + id + ")."));
    }

    @Transactional
    public Equipe creerEquipe(Equipe equipe, Long entraineurId) {
        validerChampsEquipe(equipe);

        String nom = equipe.getNom().trim();
        String saison = equipe.getSaison().trim();

        if (equipeRepository.existsByNomIgnoreCaseAndSaison(nom, saison)) {
            throw new RegleGestionException("Une équipe portant ce nom existe déjà pour la saison "
                    + saison + " (RG-05).");
        }

        equipe.setNom(nom);
        equipe.setCategorie(equipe.getCategorie().trim());
        equipe.setSaison(saison);
        associerReferent(equipe, entraineurId);

        return equipeRepository.save(equipe);
    }

    @Transactional
    public Equipe modifierEquipe(Long id, Equipe donnees, Long entraineurId) {
        Equipe existante = trouverParId(id);
        validerChampsEquipe(donnees);

        String nom = donnees.getNom().trim();
        String saison = donnees.getSaison().trim();

        if (equipeRepository.existsByNomIgnoreCaseAndSaisonAndIdNot(nom, saison, id)) {
            throw new RegleGestionException("Le couple (nom, saison) est déjà utilisé par une autre équipe (RG-05).");
        }

        existante.setNom(nom);
        existante.setCategorie(donnees.getCategorie().trim());
        existante.setSaison(saison);
        associerReferent(existante, entraineurId);

        return equipeRepository.save(existante);
    }

    @Transactional
    public Equipe affecterEntraineur(Long equipeId, Long entraineurId) {
        Equipe equipe = trouverParId(equipeId);
        associerReferent(equipe, entraineurId);
        return equipeRepository.save(equipe);
    }

    public List<Joueur> consulterEffectif(Long equipeId) {
        trouverParId(equipeId);
        return joueurRepository.findByEquipeIdOrderByNumeroMaillotAsc(equipeId);
    }

    @Transactional
    public void supprimerEquipe(Long id) {
        Equipe equipe = trouverParId(id);

        long nbJoueurs = joueurRepository.countByEquipeId(id);
        long nbEntrainements = entrainementRepository.countByEquipeId(id);
        long nbMatchs = matchRepository.countByEquipeId(id);

        if (nbJoueurs > 0 || nbEntrainements > 0 || nbMatchs > 0) {
            throw new RegleGestionException("Suppression impossible : l'équipe possède encore "
                    + nbJoueurs + " joueur(s), " + nbEntrainements + " entraînement(s) et "
                    + nbMatchs + " match(s) (RG-14).");
        }

        equipeRepository.delete(equipe);
    }

    public BilanEquipeDTO calculerBilanEquipe(Long equipeId) {
        Equipe equipe = trouverParId(equipeId);
        int nombreJoueurs = (int) joueurRepository.countByEquipeId(equipeId);
        List<Match> matchsJoues = matchRepository.findByEquipeIdAndStatut(equipeId, StatutMatch.JOUE);

        int victoires = 0;
        int nuls = 0;
        int defaites = 0;
        int butsMarques = 0;
        int butsEncaisses = 0;

        for (Match match : matchsJoues) {
            int bm = match.getButsEquipe() != null ? match.getButsEquipe() : 0;
            int be = match.getButsAdversaire() != null ? match.getButsAdversaire() : 0;
            butsMarques += bm;
            butsEncaisses += be;

            ResultatMatch res = match.getResultat();
            if (res == ResultatMatch.VICTOIRE) {
                victoires++;
            } else if (res == ResultatMatch.NUL) {
                nuls++;
            } else if (res == ResultatMatch.DEFAITE) {
                defaites++;
            }
        }

        int points = (victoires * 3) + nuls;
        int differenceButs = butsMarques - butsEncaisses;

        return new BilanEquipeDTO(
                equipe,
                nombreJoueurs,
                matchsJoues.size(),
                victoires,
                nuls,
                defaites,
                butsMarques,
                butsEncaisses,
                differenceButs,
                points
        );
    }

    private void validerChampsEquipe(Equipe equipe) {
        if (equipe.getNom() == null || equipe.getNom().isBlank()
                || equipe.getCategorie() == null || equipe.getCategorie().isBlank()
                || equipe.getSaison() == null || equipe.getSaison().isBlank()) {
            throw new RegleGestionException("Le nom, la catégorie et la saison de l'équipe sont obligatoires (RG-13).");
        }
    }

    private void associerReferent(Equipe equipe, Long entraineurId) {
        // RG-06 : une équipe a au plus un référent (0..1)
        if (entraineurId == null) {
            equipe.setEntraineur(null);
            return;
        }
        Entraineur entraineur = entraineurRepository.findById(entraineurId)
                .orElseThrow(() -> new RegleGestionException("Entraîneur introuvable (id=" + entraineurId + ")."));
        equipe.setEntraineur(entraineur);
    }
}