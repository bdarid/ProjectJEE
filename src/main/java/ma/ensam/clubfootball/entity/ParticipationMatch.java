package ma.ensam.clubfootball.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "participation_match",
       // RG-08 : une seule participation par joueur et par match
       uniqueConstraints = @UniqueConstraint(name = "uk_participation_joueur_match",
                                             columnNames = {"joueur_id", "match_id"}))
public class ParticipationMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean titulaire;

    // RG-11 : minutes entre 0 et 120, au plus 2 jaunes et 1 rouge
    @Min(value = 0, message = "Minimum 0 minute")
    @Max(value = 120, message = "Maximum 120 minutes")
    @Column(name = "minutes_jouees")
    private Integer minutesJouees = 0;

    @Min(value = 0, message = "Le nombre de buts ne peut pas être négatif")
    private Integer buts = 0;

    @Min(value = 0, message = "Minimum 0 carton")
    @Max(value = 2, message = "Maximum 2 cartons jaunes")
    @Column(name = "cartons_jaunes")
    private Integer cartonsJaunes = 0;

    @Min(value = 0, message = "Minimum 0 carton")
    @Max(value = 1, message = "Maximum 1 carton rouge")
    @Column(name = "cartons_rouges")
    private Integer cartonsRouges = 0;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    public ParticipationMatch() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean isTitulaire() { return titulaire; }
    public void setTitulaire(boolean titulaire) { this.titulaire = titulaire; }

    public Integer getMinutesJouees() { return minutesJouees; }
    public void setMinutesJouees(Integer minutesJouees) { this.minutesJouees = minutesJouees; }

    public Integer getButs() { return buts; }
    public void setButs(Integer buts) { this.buts = buts; }

    public Integer getCartonsJaunes() { return cartonsJaunes; }
    public void setCartonsJaunes(Integer cartonsJaunes) { this.cartonsJaunes = cartonsJaunes; }

    public Integer getCartonsRouges() { return cartonsRouges; }
    public void setCartonsRouges(Integer cartonsRouges) { this.cartonsRouges = cartonsRouges; }

    public Joueur getJoueur() { return joueur; }
    public void setJoueur(Joueur joueur) { this.joueur = joueur; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
}
