package ma.ensam.clubfootball.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "entraineur")
public class Entraineur {

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

    @Email(message = "Adresse email invalide")
    @Size(max = 100)
    @Column(length = 100, unique = true)
    private String email;

    @Size(max = 15, message = "15 caractères maximum")
    @Column(length = 15)
    private String telephone;

    public Entraineur() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
