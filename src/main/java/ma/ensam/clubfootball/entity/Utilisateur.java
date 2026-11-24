package ma.ensam.clubfootball.entity;

import ma.ensam.clubfootball.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'identifiant de connexion est obligatoire")
    @Size(max = 30, message = "30 caractères maximum")
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    // Contiendra le hash BCrypt (jamais le mot de passe en clair) -> 255 caractères
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Role role;

    @Column(nullable = false)
    private boolean actif = true;

    // Profil joueur lié (0..1), un profil ne peut avoir qu'un seul compte
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", unique = true)
    private Joueur joueur;

    // Profil entraîneur lié (0..1), un profil ne peut avoir qu'un seul compte
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entraineur_id", unique = true)
    private Entraineur entraineur;

    public Utilisateur() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Joueur getJoueur() { return joueur; }
    public void setJoueur(Joueur joueur) { this.joueur = joueur; }

    public Entraineur getEntraineur() { return entraineur; }
    public void setEntraineur(Entraineur entraineur) { this.entraineur = entraineur; }
}
