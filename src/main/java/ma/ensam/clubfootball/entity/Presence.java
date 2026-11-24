package ma.ensam.clubfootball.entity;

import ma.ensam.clubfootball.enums.StatutPresence;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "presence",
       // RG-02 : une seule présence par joueur et par séance
       uniqueConstraints = @UniqueConstraint(name = "uk_presence_joueur_entrainement",
                                             columnNames = {"joueur_id", "entrainement_id"}))
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le statut de présence est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutPresence statut;

    @Size(max = 150, message = "150 caractères maximum")
    @Column(length = 150)
    private String commentaire;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrainement_id", nullable = false)
    private Entrainement entrainement;

    public Presence() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatutPresence getStatut() { return statut; }
    public void setStatut(StatutPresence statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Joueur getJoueur() { return joueur; }
    public void setJoueur(Joueur joueur) { this.joueur = joueur; }

    public Entrainement getEntrainement() { return entrainement; }
    public void setEntrainement(Entrainement entrainement) { this.entrainement = entrainement; }
}
