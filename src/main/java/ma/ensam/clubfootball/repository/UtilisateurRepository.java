package ma.ensam.clubfootball.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ma.ensam.clubfootball.entity.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Utilisé par Spring Security pour charger le compte à la connexion
    Optional<Utilisateur> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    // BF-24 : retrouver le compte lié à une fiche joueur / entraîneur
    Optional<Utilisateur> findByJoueurId(Long joueurId);

    Optional<Utilisateur> findByEntraineurId(Long entraineurId);

    // À la suppression d'une fiche, le compte reste mais sans profil lié (SET NULL).
    // À appeler par le service AVANT de supprimer le joueur / l'entraîneur.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Utilisateur u SET u.joueur = null WHERE u.joueur.id = :joueurId")
    int detacherJoueur(@Param("joueurId") Long joueurId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Utilisateur u SET u.entraineur = null WHERE u.entraineur.id = :entraineurId")
    int detacherEntraineur(@Param("entraineurId") Long entraineurId);
}
