package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.dto.StatistiquesJoueurDTO;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.ParticipationMatch;
import ma.ensam.clubfootball.entity.Presence;
import ma.ensam.clubfootball.enums.PosteJoueur;
import ma.ensam.clubfootball.enums.StatutMatch;
import ma.ensam.clubfootball.enums.StatutPresence;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EquipeRepository;
import ma.ensam.clubfootball.repository.JoueurRepository;
import ma.ensam.clubfootball.repository.ParticipationMatchRepository;
import ma.ensam.clubfootball.repository.PresenceRepository;
import ma.ensam.clubfootball.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final EquipeRepository equipeRepository;
    private final PresenceRepository presenceRepository;
    private final ParticipationMatchRepository participationRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<Joueur> listerTous() {
        return joueurRepository.findAll();
    }

    public Page<Joueur> rechercherJoueurs(String nom, PosteJoueur poste, Long equipeId, Pageable pageable) {
        String filtreNom = (nom != null && !nom.isBlank()) ? nom.trim() : null;
        return joueurRepository.rechercher(filtreNom, poste, equipeId, pageable);
    }

    public Joueur trouverParId(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() -> new RegleGestionException("Joueur introuvable (id=" + id + ")."));
    }

    @Transactional
    public Joueur creerJoueur(Joueur joueur, Long equipeId) {
        validerDonneesJoueur(joueur);
        joueur.setNom(joueur.getNom().trim());
        joueur.setPrenom(joueur.getPrenom().trim());
        appliquerAffectationEquipe(joueur, equipeId, joueur.getNumeroMaillot(), null);
        return joueurRepository.save(joueur);
    }

    @Transactional
    public Joueur modifierJoueur(Long id, Joueur donnees, Long equipeId) {
        Joueur existant = trouverParId(id);
        validerDonneesJoueur(donnees);

        existant.setNom(donnees.getNom().trim());
        existant.setPrenom(donnees.getPrenom().trim());
        existant.setDateNaissance(donnees.getDateNaissance());
        existant.setPoste(donnees.getPoste());
        existant.setStatut(donnees.getStatut());

        appliquerAffectationEquipe(existant, equipeId, donnees.getNumeroMaillot(), existant.getId());
        return joueurRepository.save(existant);
    }

    @Transactional
    public Joueur affecterEquipe(Long joueurId, Long equipeId, Integer numeroMaillot) {
        Joueur joueur = trouverParId(joueurId);
        appliquerAffectationEquipe(joueur, equipeId, numeroMaillot, joueur.getId());
        return joueurRepository.save(joueur);
    }

    @Transactional
    public void supprimerJoueur(Long id) {
        Joueur joueur = trouverParId(id);

        // Détachement de l'éventuel compte utilisateur lié (FK SET NULL)
        utilisateurRepository.findByJoueurId(id).ifPresent(user -> {
            user.setJoueur(null);
            utilisateurRepository.save(user);
        });

        // RG-15 : la suppression du joueur supprime ses présences et participations en cascade
        joueurRepository.delete(joueur);
    }

    public StatistiquesJoueurDTO calculerStatistiquesJoueur(Long joueurId) {
        Joueur joueur = trouverParId(joueurId);

        int age = Period.between(joueur.getDateNaissance(), LocalDate.now()).getYears();

        List<Presence> presences = presenceRepository.findByJoueurId(joueurId);
        long totalSeances = presences.size();
        long seancesPresentes = presences.stream()
                .filter(p -> p.getStatut() == StatutPresence.PRESENT || p.getStatut() == StatutPresence.RETARD)
                .count();
        double tauxPresence = (totalSeances == 0) ? 0.0 : (seancesPresentes * 100.0) / totalSeances;

        List<ParticipationMatch> participationsJouees = participationRepository.findByJoueurId(joueurId).stream()
                .filter(pm -> pm.getMatch().getStatut() == StatutMatch.JOUE)
                .toList();

        int matchsJoues = participationsJouees.size();
        int titularisations = (int) participationsJouees.stream()
                .filter(ParticipationMatch::isTitulaire)
                .count();
        int minutesJouees = participationsJouees.stream()
                .mapToInt(pm -> pm.getMinutesJouees() != null ? pm.getMinutesJouees() : 0)
                .sum();
        int totalButs = participationsJouees.stream()
                .mapToInt(pm -> pm.getButs() != null ? pm.getButs() : 0)
                .sum();
        int cartonsJaunes = participationsJouees.stream()
                .mapToInt(pm -> pm.getCartonsJaunes() != null ? pm.getCartonsJaunes() : 0)
                .sum();
        int cartonsRouges = participationsJouees.stream()
                .mapToInt(pm -> pm.getCartonsRouges() != null ? pm.getCartonsRouges() : 0)
                .sum();

        return new StatistiquesJoueurDTO(
                age,
                Math.round(tauxPresence * 10.0) / 10.0,
                matchsJoues,
                titularisations,
                minutesJouees,
                totalButs,
                cartonsJaunes,
                cartonsRouges
        );
    }

    private void validerDonneesJoueur(Joueur joueur) {
        if (joueur.getNom() == null || joueur.getNom().isBlank()
                || joueur.getPrenom() == null || joueur.getPrenom().isBlank()) {
            throw new RegleGestionException("Le nom et le prénom du joueur sont obligatoires (RG-13).");
        }
        if (joueur.getDateNaissance() == null || !joueur.getDateNaissance().isBefore(LocalDate.now())) {
            throw new RegleGestionException("La date de naissance du joueur doit être antérieure à aujourd'hui (RG-12).");
        }
        if (joueur.getPoste() == null || joueur.getStatut() == null) {
            throw new RegleGestionException("Le poste et le statut du joueur sont obligatoires (RG-13).");
        }
    }

    private void appliquerAffectationEquipe(Joueur joueur, Long equipeId, Integer numeroMaillot, Long joueurIdExclu) {
        // RG-04 : un joueur appartient à au plus une équipe à un instant donné (0..1)
        if (equipeId == null) {
            joueur.setEquipe(null);
            if (numeroMaillot != null && (numeroMaillot < 1 || numeroMaillot > 99)) {
                throw new RegleGestionException("Le numéro de maillot doit être compris entre 1 et 99 (RG-03).");
            }
            joueur.setNumeroMaillot(numeroMaillot);
            return;
        }

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RegleGestionException("Équipe introuvable (id=" + equipeId + ")."));

        if (numeroMaillot == null || numeroMaillot < 1 || numeroMaillot > 99) {
            throw new RegleGestionException("Un numéro de maillot entre 1 et 99 est requis pour l'affectation (RG-03).");
        }

        boolean numeroDejaPris = (joueurIdExclu == null)
                ? joueurRepository.existsByEquipeIdAndNumeroMaillot(equipeId, numeroMaillot)
                : joueurRepository.existsByEquipeIdAndNumeroMaillotAndIdNot(equipeId, numeroMaillot, joueurIdExclu);

        if (numeroDejaPris) {
            throw new RegleGestionException("Le numéro " + numeroMaillot + " est déjà attribué dans l'équipe "
                    + equipe.getNom() + " (" + equipe.getSaison() + ") (RG-03).");
        }

        joueur.setEquipe(equipe);
        joueur.setNumeroMaillot(numeroMaillot);
    }
}