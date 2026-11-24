package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Entraineur;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EntraineurRepository;
import ma.ensam.clubfootball.repository.EquipeRepository;
import ma.ensam.clubfootball.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntraineurService {

    private final EntraineurRepository entraineurRepository;
    private final EquipeRepository equipeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<Entraineur> listerTous() {
        return entraineurRepository.findAll();
    }

    public Entraineur trouverParId(Long id) {
        return entraineurRepository.findById(id)
                .orElseThrow(() -> new RegleGestionException("Entraîneur introuvable (id=" + id + ")."));
    }

    @Transactional
    public Entraineur creerEntraineur(Entraineur entraineur) {
        validerEntraineur(entraineur, null);
        entraineur.setNom(entraineur.getNom().trim());
        entraineur.setPrenom(entraineur.getPrenom().trim());
        if (entraineur.getEmail() != null && !entraineur.getEmail().isBlank()) {
            entraineur.setEmail(entraineur.getEmail().trim());
        } else {
            entraineur.setEmail(null);
        }
        return entraineurRepository.save(entraineur);
    }

    @Transactional
    public Entraineur modifierEntraineur(Long id, Entraineur donnees) {
        Entraineur existant = trouverParId(id);
        validerEntraineur(donnees, id);

        existant.setNom(donnees.getNom().trim());
        existant.setPrenom(donnees.getPrenom().trim());
        existant.setEmail((donnees.getEmail() != null && !donnees.getEmail().isBlank())
                ? donnees.getEmail().trim() : null);
        existant.setTelephone(donnees.getTelephone() != null ? donnees.getTelephone().trim() : null);

        return entraineurRepository.save(existant);
    }

    @Transactional
    public void supprimerEntraineur(Long id) {
        Entraineur entraineur = trouverParId(id);

        // RG-16 : les équipes encadrées sont conservées sans référent (SET NULL)
        List<Equipe> equipesEncadrees = equipeRepository.findByEntraineurId(id);
        for (Equipe equipe : equipesEncadrees) {
            equipe.setEntraineur(null);
        }
        equipeRepository.saveAll(equipesEncadrees);

        // Détachement de l'éventuel compte utilisateur lié (FK SET NULL)
        utilisateurRepository.findByEntraineurId(id).ifPresent(user -> {
            user.setEntraineur(null);
            utilisateurRepository.save(user);
        });

        entraineurRepository.delete(entraineur);
    }

    private void validerEntraineur(Entraineur entraineur, Long idExclu) {
        if (entraineur.getNom() == null || entraineur.getNom().isBlank()
                || entraineur.getPrenom() == null || entraineur.getPrenom().isBlank()) {
            throw new RegleGestionException("Le nom et le prénom de l'entraîneur sont obligatoires (RG-13).");
        }

        if (entraineur.getEmail() != null && !entraineur.getEmail().isBlank()) {
            String email = entraineur.getEmail().trim();
            boolean emailExiste = (idExclu == null)
                    ? entraineurRepository.existsByEmailIgnoreCase(email)
                    : entraineurRepository.existsByEmailIgnoreCaseAndIdNot(email, idExclu);

            if (emailExiste) {
                throw new RegleGestionException("L'adresse email " + email + " est déjà utilisée par un autre entraîneur.");
            }
        }
    }
}