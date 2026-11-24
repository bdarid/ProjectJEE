package ma.ensam.clubfootball.service;

import lombok.RequiredArgsConstructor;
import ma.ensam.clubfootball.entity.Entraineur;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Utilisateur;
import ma.ensam.clubfootball.enums.Role;
import ma.ensam.clubfootball.exception.RegleGestionException;
import ma.ensam.clubfootball.repository.EntraineurRepository;
import ma.ensam.clubfootball.repository.JoueurRepository;
import ma.ensam.clubfootball.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final JoueurRepository joueurRepository;
    private final EntraineurRepository entraineurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));

        return User.builder()
                .username(utilisateur.getUsername())
                .password(utilisateur.getMotDePasse())
                .disabled(!utilisateur.isActif())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())))
                .build();
    }

    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur trouverParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RegleGestionException("Compte utilisateur introuvable (id=" + id + ")."));
    }

    public Utilisateur trouverParUsername(String username) {
        return utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new RegleGestionException("Compte utilisateur introuvable : " + username));
    }

    @Transactional
    public Utilisateur creerCompte(String username, String motDePasseClair, Role role,
                                   Long joueurId, Long entraineurId) {
        if (username == null || username.isBlank() || motDePasseClair == null || motDePasseClair.isBlank() || role == null) {
            throw new RegleGestionException("L'identifiant, le mot de passe et le rôle sont obligatoires (RG-13).");
        }

        String identifiant = username.trim();
        if (utilisateurRepository.existsByUsernameIgnoreCase(identifiant)) {
            throw new RegleGestionException("L'identifiant de connexion " + identifiant + " est déjà utilisé.");
        }

        if (joueurId != null && entraineurId != null) {
            throw new RegleGestionException("Un compte ne peut pas être lié simultanément à un joueur et à un entraîneur.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUsername(identifiant);
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseClair));
        utilisateur.setRole(role);
        utilisateur.setActif(true);

        if (joueurId != null) {
            if (utilisateurRepository.findByJoueurId(joueurId).isPresent()) {
                throw new RegleGestionException("Ce joueur possède déjà un compte utilisateur associé.");
            }
            Joueur joueur = joueurRepository.findById(joueurId)
                    .orElseThrow(() -> new RegleGestionException("Joueur introuvable (id=" + joueurId + ")."));
            utilisateur.setJoueur(joueur);
        }

        if (entraineurId != null) {
            if (utilisateurRepository.findByEntraineurId(entraineurId).isPresent()) {
                throw new RegleGestionException("Cet entraîneur possède déjà un compte utilisateur associé.");
            }
            Entraineur entraineur = entraineurRepository.findById(entraineurId)
                    .orElseThrow(() -> new RegleGestionException("Entraîneur introuvable (id=" + entraineurId + ")."));
            utilisateur.setEntraineur(entraineur);
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur modifierRoleEtStatut(Long id, Role role, boolean actif) {
        Utilisateur utilisateur = trouverParId(id);
        if (role == null) {
            throw new RegleGestionException("Le rôle est obligatoire (RG-13).");
        }
        utilisateur.setRole(role);
        utilisateur.setActif(actif);
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, String nouveauMotDePasse) {
        if (nouveauMotDePasse == null || nouveauMotDePasse.isBlank()) {
            throw new RegleGestionException("Le nouveau mot de passe ne peut pas être vide (RG-13).");
        }
        Utilisateur utilisateur = trouverParId(id);
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void supprimerCompte(Long id) {
        Utilisateur utilisateur = trouverParId(id);
        utilisateurRepository.delete(utilisateur);
    }
}