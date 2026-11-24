package ma.ensam.clubfootball.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ma.ensam.clubfootball.enums.StatutPresence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "entrainement")
public class Entrainement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date et l'heure sont obligatoires")
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 50, message = "50 caractères maximum")
    @Column(nullable = false, length = 50)
    private String lieu;

    @Size(max = 200, message = "200 caractères maximum")
    @Column(length = 200)
    private String description;

    @NotNull(message = "L'équipe est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    // Composition : les présences n'existent que dans une séance.
    // RG-15 : supprimer la séance supprime ses présences.
    @OneToMany(mappedBy = "entrainement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Presence> presences = new ArrayList<>();

    public Entrainement() {
    }

    // ---------- Opération dérivée ----------

    /** Nombre de joueurs présents (un joueur en retard est compté présent). */
    public int getNombrePresents() {
        return (int) presences.stream()
                .filter(p -> p.getStatut() != StatutPresence.ABSENT)
                .count();
    }

    // ---------- Getters / setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    public List<Presence> getPresences() { return presences; }
}
