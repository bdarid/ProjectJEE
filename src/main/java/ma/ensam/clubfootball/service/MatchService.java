package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Match;
import ma.ensam.clubfootball.entity.ParticipationMatch;
import ma.ensam.clubfootball.enums.StatutMatch;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EquipeRepository;
import ma.ensam.clubfootball.repository.JoueurRepository;
import ma.ensam.clubfootball.repository.MatchRepository;
import ma.ensam.clubfootball.repository.ParticipationMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;
    private final ParticipationMatchRepository participationRepository;

    public List<Match> listerTous() {
        return matchRepository.findAllByOrderByDateHeureDesc();
    }

    public List<Match> listerParEquipe(Long equipeId) {
        return matchRepository.findByEquipeIdOrderByDateHeureDesc(equipeId);
    }

    public Match trouverParId(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new RegleGestionException("Match introuvable (id=" + id + ")."));
    }

    @Transactional
    public Match planifierMatch(Match match, Long equipeId) {
        if (equipeId == null) {
            throw new RegleGestionException("Un match doit être associé à une équipe du club (RG-13).");
        }
        if (match.getAdversaire() == null || match.getAdversaire().isBlank()
                || match.getDateHeure() == null || match.getLieu() == null || match.getLieu().isBlank()) {
            throw new RegleGestionException("L'adversaire, la date/heure et le lieu du match sont obligatoires (RG-13).");
        }

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RegleGestionException("Équipe introuvable (id=" + equipeId + ")."));

        match.setEquipe(equipe);
        match.setAdversaire(match.getAdversaire().trim());
        match.setLieu(match.getLieu().trim());
        if (match.getStatut() == null) {
            match.setStatut(StatutMatch.PROGRAMME);
        }

        validerScoresPositifs(match.getButsEquipe(), match.getButsAdversaire());
        return matchRepository.save(match);
    }

    @Transactional
    public Match modifierMatch(Long id, Match donnees) {
        Match existant = trouverParId(id);

        if (donnees.getAdversaire() == null || donnees.getAdversaire().isBlank()
                || donnees.getDateHeure() == null || donnees.getLieu() == null || donnees.getLieu().isBlank()) {
            throw new RegleGestionException("L'adversaire, la date/heure et le lieu sont obligatoires (RG-13).");
        }

        existant.setAdversaire(donnees.getAdversaire().trim());
        existant.setDateHeure(donnees.getDateHeure());
        existant.setLieu(donnees.getLieu().trim());

        if (donnees.getStatut() != null) {
            if (donnees.getStatut() == StatutMatch.JOUE
                    && (existant.getButsEquipe() == null || existant.getButsAdversaire() == null)) {
                throw new RegleGestionException("Un match passé au statut JOUE doit comporter ses deux scores (RG-09).");
            }
            existant.setStatut(donnees.getStatut());
        }

        return matchRepository.save(existant);
    }

    /**
     * CU-03 & BF-17 : Saisie des scores et passage au statut JOUE.
     * Le résultat (VICTOIRE, NUL, DEFAITE) est déduit automatiquement via match.getResultat() (RG-10).
     */
    @Transactional
    public Match saisirResultatMatch(Long matchId, Integer butsEquipe, Integer butsAdversaire) {
        Match match = trouverParId(matchId);

        if (butsEquipe == null || butsAdversaire == null) {
            throw new RegleGestionException("Un match joué doit avoir ses deux scores renseignés (RG-09).");
        }
        validerScoresPositifs(butsEquipe, butsAdversaire);

        // Vérifier que les buts déjà saisis sur les participations ne dépassent pas le nouveau score
        int totalButsParticipants = participationRepository.findByMatchId(matchId).stream()
                .mapToInt(pm -> pm.getButs() != null ? pm.getButs() : 0)
                .sum();

        if (totalButsParticipants > butsEquipe) {
            throw new RegleGestionException("Incohérence : les buts attribués aux joueurs (" + totalButsParticipants
                    + ") dépassent le score de l'équipe (" + butsEquipe + ") (RG-09).");
        }

        match.setButsEquipe(butsEquipe);
        match.setButsAdversaire(butsAdversaire);
        match.setStatut(StatutMatch.JOUE);

        return matchRepository.save(match);
    }

    /**
     * CU-03 & BF-18 : Enregistrement des participations individuelles d'un match.
     */
    @Transactional
    public void enregistrerParticipations(Long matchId, List<ParticipationMatch> participations) {
        Match match = trouverParId(matchId);
        Long equipeMatchId = match.getEquipe().getId();

        if (participations == null) {
            return;
        }

        Set<Long> joueursVus = new HashSet<>();
        int sommeButsJoueurs = 0;

        for (ParticipationMatch pm : participations) {
            if (pm.getJoueur() == null || pm.getJoueur().getId() == null) {
                throw new RegleGestionException("Chaque participation doit désigner un joueur valide (RG-13).");
            }

            Long joueurId = pm.getJoueur().getId();
            if (!joueursVus.add(joueurId)) {
                throw new RegleGestionException("Un joueur ne peut avoir qu'une seule participation par match (RG-08).");
            }

            Joueur joueur = joueurRepository.findById(joueurId)
                    .orElseThrow(() -> new RegleGestionException("Joueur introuvable (id=" + joueurId + ")."));

            // RG-08 : le joueur doit appartenir à l'équipe du match
            if (joueur.getEquipe() == null || !joueur.getEquipe().getId().equals(equipeMatchId)) {
                throw new RegleGestionException("Le joueur " + joueur.getPrenom() + " " + joueur.getNom()
                        + " n'appartient pas à l'équipe disputant ce match (RG-08).");
            }

            int minutes = pm.getMinutesJouees() != null ? pm.getMinutesJouees() : 0;
            int buts = pm.getButs() != null ? pm.getButs() : 0;
            int jaunes = pm.getCartonsJaunes() != null ? pm.getCartonsJaunes() : 0;
            int rouges = pm.getCartonsRouges() != null ? pm.getCartonsRouges() : 0;

            // RG-11 : bornes réglementaires
            if (minutes < 0 || minutes > 120) {
                throw new RegleGestionException("Les minutes jouées doivent être comprises entre 0 et 120 (RG-11).");
            }
            if (buts < 0) {
                throw new RegleGestionException("Le nombre de buts d'un joueur ne peut pas être négatif.");
            }
            if (jaunes < 0 || jaunes > 2) {
                throw new RegleGestionException("Un joueur peut recevoir au plus 2 cartons jaunes par match (RG-11).");
            }
            if (rouges < 0 || rouges > 1) {
                throw new RegleGestionException("Un joueur peut recevoir au plus 1 carton rouge par match (RG-11).");
            }

            sommeButsJoueurs += buts;
        }

        // RG-09 : somme des buts des participants <= butsEquipe
        if (match.getButsEquipe() != null && sommeButsJoueurs > match.getButsEquipe()) {
            throw new RegleGestionException("Le total des buts des participants (" + sommeButsJoueurs
                    + ") dépasse le score de l'équipe (" + match.getButsEquipe() + ") (RG-09).");
        }

        // Remplacement propre des participations du match
        participationRepository.deleteByMatchId(matchId);
        for (ParticipationMatch pm : participations) {
            Joueur joueur = joueurRepository.findById(pm.getJoueur().getId()).orElseThrow();

            ParticipationMatch ligne = new ParticipationMatch();
            ligne.setMatch(match);
            ligne.setJoueur(joueur);
            ligne.setTitulaire(pm.isTitulaire());
            ligne.setMinutesJouees(pm.getMinutesJouees() != null ? pm.getMinutesJouees() : 0);
            ligne.setButs(pm.getButs() != null ? pm.getButs() : 0);
            ligne.setCartonsJaunes(pm.getCartonsJaunes() != null ? pm.getCartonsJaunes() : 0);
            ligne.setCartonsRouges(pm.getCartonsRouges() != null ? pm.getCartonsRouges() : 0);

            participationRepository.save(ligne);
        }
    }

    @Transactional
    public void supprimerMatch(Long id) {
        Match match = trouverParId(id);
        // RG-15 : supprime le match et ses participations associées
        matchRepository.delete(match);
    }

    private void validerScoresPositifs(Integer butsEquipe, Integer butsAdversaire) {
        if ((butsEquipe != null && butsEquipe < 0) || (butsAdversaire != null && butsAdversaire < 0)) {
            throw new RegleGestionException("Le score d'un match ne peut pas être négatif (RG-01).");
        }
    }
}