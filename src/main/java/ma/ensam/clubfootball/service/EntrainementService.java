package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Entrainement;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Presence;
import ma.ensam.clubfootball.enums.StatutPresence;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EntrainementRepository;
import ma.ensam.clubfootball.repository.EquipeRepository;
import ma.ensam.clubfootball.repository.JoueurRepository;
import ma.ensam.clubfootball.repository.PresenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntrainementService {

    private final EntrainementRepository entrainementRepository;
    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;
    private final PresenceRepository presenceRepository;

    public List<Entrainement> listerTous() {
        return entrainementRepository.findAllByOrderByDateHeureDesc();
    }

    public List<Entrainement> listerParEquipe(Long equipeId) {
        return entrainementRepository.findByEquipeIdOrderByDateHeureDesc(equipeId);
    }

    public Entrainement trouverParId(Long id) {
        return entrainementRepository.findById(id)
                .orElseThrow(() -> new RegleGestionException("Séance d'entraînement introuvable (id=" + id + ")."));
    }

    @Transactional
    public Entrainement planifierSeance(Entrainement entrainement, Long equipeId) {
        validerSeance(entrainement, equipeId);
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RegleGestionException("Équipe introuvable (id=" + equipeId + ")."));

        entrainement.setEquipe(equipe);
        entrainement.setLieu(entrainement.getLieu().trim());
        return entrainementRepository.save(entrainement);
    }

    @Transactional
    public Entrainement modifierSeance(Long id, Entrainement donnees, Long equipeId) {
        Entrainement existant = trouverParId(id);
        validerSeance(donnees, equipeId);

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RegleGestionException("Équipe introuvable (id=" + equipeId + ")."));

        existant.setDateHeure(donnees.getDateHeure());
        existant.setLieu(donnees.getLieu().trim());
        existant.setDescription(donnees.getDescription());
        existant.setEquipe(equipe);

        return entrainementRepository.save(existant);
    }

    @Transactional
    public void supprimerSeance(Long id) {
        Entrainement seance = trouverParId(id);
        // RG-15 : la suppression d'une séance supprime les présences liées
        entrainementRepository.delete(seance);
    }

    /**
     * CU-02 : Prépare la feuille d'appel à partir de l'effectif de l'équipe,
     * en préremplissant les présences déjà saisies si la séance a déjà été pointée.
     */
    public List<Presence> preparerFeuillePresence(Long entrainementId) {
        Entrainement seance = trouverParId(entrainementId);
        List<Joueur> effectif = joueurRepository.findByEquipeIdOrderByNumeroMaillotAsc(
                seance.getEquipe().getId());

        Map<Long, Presence> existantesParJoueur = presenceRepository.findByEntrainementId(entrainementId).stream()
                .collect(Collectors.toMap(p -> p.getJoueur().getId(), Function.identity()));

        List<Presence> feuille = new ArrayList<>();
        for (Joueur joueur : effectif) {
            Presence ligne = existantesParJoueur.get(joueur.getId());
            if (ligne == null) {
                ligne = new Presence();
                ligne.setEntrainement(seance);
                ligne.setJoueur(joueur);
                ligne.setStatut(StatutPresence.PRESENT);
            }
            feuille.add(ligne);
        }
        return feuille;
    }

    /**
     * CU-02 & BF-13 : Enregistrement groupé des présences d'une séance.
     */
    @Transactional
    public void enregistrerPresencesSeance(Long entrainementId, List<Presence> saisies) {
        Entrainement seance = trouverParId(entrainementId);
        Long equipeSeanceId = seance.getEquipe().getId();

        if (saisies == null || saisies.isEmpty()) {
            throw new RegleGestionException("Aucune présence transmise pour la séance.");
        }

        Set<Long> joueursTraites = new HashSet<>();

        for (Presence saisie : saisies) {
            if (saisie.getJoueur() == null || saisie.getJoueur().getId() == null) {
                throw new RegleGestionException("Chaque ligne de présence doit référencer un joueur valide (RG-13).");
            }
            if (saisie.getStatut() == null) {
                throw new RegleGestionException("Le statut de présence (PRESENT, ABSENT, RETARD) est obligatoire (RG-13).");
            }

            Long joueurId = saisie.getJoueur().getId();
            if (!joueursTraites.add(joueurId)) {
                throw new RegleGestionException("Doublon détecté : un joueur ne peut avoir qu'une présence par séance (RG-02).");
            }

            Joueur joueur = joueurRepository.findById(joueurId)
                    .orElseThrow(() -> new RegleGestionException("Joueur introuvable (id=" + joueurId + ")."));

            // RG-07 : le joueur doit appartenir à l'équipe de la séance
            if (joueur.getEquipe() == null || !joueur.getEquipe().getId().equals(equipeSeanceId)) {
                throw new RegleGestionException("Le joueur " + joueur.getPrenom() + " " + joueur.getNom()
                        + " n'appartient pas à l'équipe concernée par cette séance (RG-07).");
            }

            // RG-02 : mise à jour si existante, création sinon
            Presence presence = presenceRepository.findByEntrainementIdAndJoueurId(entrainementId, joueurId)
                    .orElseGet(Presence::new);

            presence.setEntrainement(seance);
            presence.setJoueur(joueur);
            presence.setStatut(saisie.getStatut());
            presence.setCommentaire(saisie.getCommentaire() != null ? saisie.getCommentaire().trim() : null);

            presenceRepository.save(presence);
        }
    }

    public List<Presence> consulterHistoriqueParSeance(Long entrainementId) {
        trouverParId(entrainementId);
        return presenceRepository.findByEntrainementId(entrainementId);
    }

    public List<Presence> consulterHistoriqueParJoueur(Long joueurId) {
        return presenceRepository.findByJoueurIdOrderByEntrainementDateHeureDesc(joueurId);
    }

    public int compterPresentsSeance(Long entrainementId) {
        return (int) presenceRepository.findByEntrainementId(entrainementId).stream()
                .filter(p -> p.getStatut() == StatutPresence.PRESENT || p.getStatut() == StatutPresence.RETARD)
                .count();
    }

    public double calculerTauxPresence(Long joueurId) {
        List<Presence> historique = presenceRepository.findByJoueurId(joueurId);
        if (historique.isEmpty()) {
            return 0.0;
        }
        long presents = historique.stream()
                .filter(p -> p.getStatut() == StatutPresence.PRESENT || p.getStatut() == StatutPresence.RETARD)
                .count();
        return Math.round((presents * 1000.0) / historique.size()) / 10.0;
    }

    private void validerSeance(Entrainement entrainement, Long equipeId) {
        if (equipeId == null) {
            throw new RegleGestionException("Une séance d'entraînement doit être rattachée à une équipe (RG-13).");
        }
        if (entrainement.getDateHeure() == null || entrainement.getLieu() == null || entrainement.getLieu().isBlank()) {
            throw new RegleGestionException("La date/heure et le lieu de la séance sont obligatoires (RG-13).");
        }
    }
}