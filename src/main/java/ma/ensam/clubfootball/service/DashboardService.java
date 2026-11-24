package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.dto.DashboardGlobalDTO;
import ma.ensam.clubfootball.dto.DashboardJoueurDTO;
import ma.ensam.clubfootball.dto.StatistiquesJoueurDTO;
import ma.ensam.clubfootball.entity.Entrainement;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Match;
import ma.ensam.clubfootball.entity.Presence;
import ma.ensam.clubfootball.entity.Utilisateur;
import ma.ensam.clubfootball.enums.StatutMatch;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EntrainementRepository;
import ma.ensam.clubfootball.repository.EquipeRepository;
import ma.ensam.clubfootball.repository.JoueurRepository;
import ma.ensam.clubfootball.repository.MatchRepository;
import ma.ensam.clubfootball.repository.PresenceRepository;
import ma.ensam.clubfootball.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final JoueurRepository joueurRepository;
    private final EquipeRepository equipeRepository;
    private final MatchRepository matchRepository;
    private final EntrainementRepository entrainementRepository;
    private final PresenceRepository presenceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JoueurService joueurService;

    /**
     * BF-21 & BF-22 : Indicateurs globaux, prochains événements et derniers résultats.
     */
    public DashboardGlobalDTO construireDashboardGlobal() {
        long totalJoueurs = joueurRepository.count();
        long totalEquipes = equipeRepository.count();
        long totalMatchs = matchRepository.count();
        long totalEntrainements = entrainementRepository.count();

        LocalDateTime maintenant = LocalDateTime.now();

        List<Entrainement> prochainsEntrainements = entrainementRepository
                .findTop5ByDateHeureAfterOrderByDateHeureAsc(maintenant);

        List<Match> prochainsMatchs = matchRepository
                .findTop5ByStatutAndDateHeureAfterOrderByDateHeureAsc(StatutMatch.PROGRAMME, maintenant);

        List<Match> derniersResultats = matchRepository
                .findTop5ByStatutOrderByDateHeureDesc(StatutMatch.JOUE);

        return new DashboardGlobalDTO(
                totalJoueurs,
                totalEquipes,
                totalMatchs,
                totalEntrainements,
                prochainsEntrainements,
                prochainsMatchs,
                derniersResultats
        );
    }

    /**
     * BF-24 : Consultation personnalisée pour le joueur authentifié.
     */
    public DashboardJoueurDTO construireDashboardJoueur(String username) {
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new RegleGestionException("Utilisateur introuvable : " + username));

        Joueur joueur = utilisateur.getJoueur();
        if (joueur == null) {
            throw new RegleGestionException("Aucune fiche joueur n'est associée à ce compte utilisateur.");
        }

        Equipe equipe = joueur.getEquipe();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Entrainement> prochainsEntrainements;
        List<Match> prochainsMatchs;

        if (equipe != null) {
            // Force l'initialisation du proxy Hibernate avant la fermeture de la transaction
            equipe.getNom();

            prochainsEntrainements = entrainementRepository
                    .findByEquipeIdAndDateHeureAfterOrderByDateHeureAsc(equipe.getId(), maintenant);

            prochainsMatchs = matchRepository
                    .findByEquipeIdAndStatutAndDateHeureAfterOrderByDateHeureAsc(
                            equipe.getId(), StatutMatch.PROGRAMME, maintenant);
        } else {
            prochainsEntrainements = Collections.emptyList();
            prochainsMatchs = Collections.emptyList();
        }

        List<Presence> historiquePresences = presenceRepository
                .findByJoueurIdOrderByEntrainementDateHeureDesc(joueur.getId());

        StatistiquesJoueurDTO statistiques = joueurService.calculerStatistiquesJoueur(joueur.getId());

        return new DashboardJoueurDTO(
                joueur,
                equipe,
                prochainsEntrainements,
                prochainsMatchs,
                historiquePresences,
                statistiques
        );
    }
}