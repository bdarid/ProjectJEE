package ma.ensam.clubfootball.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ma.ensam.clubfootball.enums.ResultatMatch;
import ma.ensam.clubfootball.enums.StatutMatch;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "matchs") // "match" est un mot réservé en SQL, d'où "matchs"
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'adversaire est obligatoire")
    @Size(max = 40, message = "40 caractères maximum")
    @Column(nullable = false, length = 40)
    private String adversaire;

    @NotNull(message = "La date et l'heure sont obligatoires")
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 50, message = "50 caractères maximum")
    @Column(nullable = false, length = 50)
    private String lieu;

    // RG-01 : un score ne peut pas être négatif.
    // Les scores restent null tant que le match n'est pas joué.
    @Min(value = 0, message = "Le score ne peut pas être négatif")
    @Column(name = "buts_equipe")
    private Integer butsEquipe;

    @Min(value = 0, message = "Le score ne peut pas être négatif")
    @Column(name = "buts_adversaire")
    private Integer butsAdversaire;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatutMatch statut = StatutMatch.PROGRAMME;

    @NotNull(message = "L'équipe est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    // Composition. RG-15 : supprimer le match supprime ses participations.
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipationMatch> participations = new ArrayList<>();

    public Match() {
    }

    // ---------- Opération dérivée ----------

    /** RG-10 : le résultat est déduit des scores, jamais saisi. */
    public ResultatMatch getResultat() {
        if (statut != StatutMatch.JOUE || butsEquipe == null || butsAdversaire == null) {
            return null;
        }
        int comparaison = Integer.compare(butsEquipe, butsAdversaire);
        if (comparaison > 0) {
            return ResultatMatch.VICTOIRE;
        }
        if (comparaison < 0) {
            return ResultatMatch.DEFAITE;
        }
        return ResultatMatch.NUL;
    }

    // ---------- Getters / setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAdversaire() { return adversaire; }
    public void setAdversaire(String adversaire) { this.adversaire = adversaire; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public Integer getButsEquipe() { return butsEquipe; }
    public void setButsEquipe(Integer butsEquipe) { this.butsEquipe = butsEquipe; }

    public Integer getButsAdversaire() { return butsAdversaire; }
    public void setButsAdversaire(Integer butsAdversaire) { this.butsAdversaire = butsAdversaire; }

    public StatutMatch getStatut() { return statut; }
    public void setStatut(StatutMatch statut) { this.statut = statut; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    public List<ParticipationMatch> getParticipations() { return participations; }
}
