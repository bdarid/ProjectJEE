package ma.ensam.clubfootball.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ma.ensam.clubfootball.entity.Entraineur;

public interface EntraineurRepository extends JpaRepository<Entraineur, Long> {

    Optional<Entraineur> findByEmail(String email);

    // Unicité de l'email (création / modification)
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // BF-08 : recherche par nom ou prénom, avec pagination
    Page<Entraineur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long idExclu);
}
