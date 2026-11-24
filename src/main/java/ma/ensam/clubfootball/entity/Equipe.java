package ma.ensam.clubfootball.entity;

import java.util.ArrayList;
import java.util.List;

import ma.ensam.clubfootball.enums.ResultatMatch;
import ma.ensam.clubfootball.enums.StatutMatch;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "equipe",
       // RG-05 : une équipe est unique pour le couple (nom, saison)
       uniqueConstraints = @UniqueConstraint(name = "uk_equipe_nom_saison", columnNames = {"nom", "saison"}))
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(max = 40, message = "40 caractères maximum")
    @Column(nullable = false, length = 40)
    private String nom;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 15, message = "15 caractères maximum")
    @Column(nullable = false, length = 15)
    private String categorie;

    @NotBlank(message = "La saison est obligatoire")
    @Size(max = 9, message = "Format attendu : 2026-2027")
    @Column(nullable = false, length = 9)
    private String saison;

    // RG-06 : au plus un référent par équipe, un entraîneur peut avoir plusieurs équipes
    // (clé étrangère equipe.entraineur_id, facultative)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entraineur_id")
    private Entraineur entraineur;

    // Pas de cascade ici : RG-14 interdit de supprimer une équipe qui a encore
    // des joueurs, des entraînements ou des matchs (clé étrangère en RESTRICT).
    @OneToMany(mappedBy = "equipe")
    private List<Joueur> joueurs = new ArrayList<>();

    @OneToMany(mappedBy = "equipe")
    private List<Entrainement> entrainements = new ArrayList<>();

    @OneToMany(mappedBy = "equipe")
    private List<Match> matchs = new ArrayList<>();

    public Equipe() {
    }

    // ---------- Opérations dérivées (non persistées) ----------

    public int getNombreJoueurs() {
        return joueurs.size();
    }

    /** Matchs dont le statut est JOUE et dont les deux scores sont connus. */
    private List<Match> matchsJoues() {
        return matchs.stream()
                .filter(m -> m.getStatut() == StatutMatch.JOUE && m.getResultat() != null)
                .toList();
    }

    public int getVictoires() {
        return (int) matchsJoues().stream().filter(m -> m.getResultat() == ResultatMatch.VICTOIRE).count();
    }

    public int getNuls() {
        return (int) matchsJoues().stream().filter(m -> m.getResultat() == ResultatMatch.NUL).count();
    }

    public int getDefaites() {
        return (int) matchsJoues().stream().filter(m -> m.getResultat() == ResultatMatch.DEFAITE).count();
    }

    public int getButsMarques() {
        return matchsJoues().stream().mapToInt(Match::getButsEquipe).sum();
    }

    public int getButsEncaisses() {
        return matchsJoues().stream().mapToInt(Match::getButsAdversaire).sum();
    }

    /** Barème classique : 3 points pour une victoire, 1 pour un nul. */
    public int getPoints() {
        return 3 * getVictoires() + getNuls();
    }

    // ---------- Getters / setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }

    public Entraineur getEntraineur() { return entraineur; }
    public void setEntraineur(Entraineur entraineur) { this.entraineur = entraineur; }

    public List<Joueur> getJoueurs() { return joueurs; }
    public List<Entrainement> getEntrainements() { return entrainements; }
    public List<Match> getMatchs() { return matchs; }
}
