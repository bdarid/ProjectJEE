package ma.ensam.clubfootball.entity;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import ma.ensam.clubfootball.enums.PosteJoueur;
import ma.ensam.clubfootball.enums.StatutJoueur;
import ma.ensam.clubfootball.enums.StatutPresence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "joueur",
       // RG-03 : numéro de maillot unique pour une équipe
       uniqueConstraints = @UniqueConstraint(name = "uk_joueur_equipe_maillot",
                                             columnNames = {"equipe_id", "numero_maillot"}))
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 20, message = "20 caractères maximum")
    @Column(nullable = false, length = 20)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 20, message = "20 caractères maximum")
    @Column(nullable = false, length = 20)
    private String prenom;

    // RG-12 : la date de naissance doit être passée
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @NotNull(message = "Le poste est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PosteJoueur poste;

    // RG-03 : numéro compris entre 1 et 99
    @Min(value = 1, message = "Le numéro doit être compris entre 1 et 99")
    @Max(value = 99, message = "Le numéro doit être compris entre 1 et 99")
    @Column(name = "numero_maillot")
    private Integer numeroMaillot;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatutJoueur statut;

    // RG-04 : au plus une équipe à un instant donné (facultative)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    // RG-15 : supprimer un joueur supprime ses présences et ses participations
    @OneToMany(mappedBy = "joueur", cascade = CascadeType.REMOVE)
    private List<Presence> presences = new ArrayList<>();

    @OneToMany(mappedBy = "joueur", cascade = CascadeType.REMOVE)
    private List<ParticipationMatch> participations = new ArrayList<>();

    public Joueur() {
    }

    // ---------- Opérations dérivées (non persistées) ----------

    public int getAge() {
        return dateNaissance == null ? 0 : Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    /**
     * Taux de présence en pourcentage (0 à 100).
     * Choix : un joueur en retard est compté comme présent.
     */
    public double getTauxPresence() {
        if (presences.isEmpty()) {
            return 0.0;
        }
        long presents = presences.stream()
                .filter(p -> p.getStatut() != StatutPresence.ABSENT)
                .count();
        return presents * 100.0 / presences.size();
    }

    public int getTotalButs() {
        return participations.stream()
                .mapToInt(p -> p.getButs() == null ? 0 : p.getButs())
                .sum();
    }

    // ---------- Getters / setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public PosteJoueur getPoste() { return poste; }
    public void setPoste(PosteJoueur poste) { this.poste = poste; }

    public Integer getNumeroMaillot() { return numeroMaillot; }
    public void setNumeroMaillot(Integer numeroMaillot) { this.numeroMaillot = numeroMaillot; }

    public StatutJoueur getStatut() { return statut; }
    public void setStatut(StatutJoueur statut) { this.statut = statut; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    public List<Presence> getPresences() { return presences; }
    public List<ParticipationMatch> getParticipations() { return participations; }
}
