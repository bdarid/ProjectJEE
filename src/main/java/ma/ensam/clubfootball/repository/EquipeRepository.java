package ma.ensam.clubfootball.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ma.ensam.clubfootball.entity.Equipe;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    // BF-06 / BF-23 : liste filtrée et paginée
    Page<Equipe> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    List<Equipe> findBySaisonOrderByNomAsc(String saison);

    // Équipes encadrées par un entraîneur
    List<Equipe> findByEntraineurId(Long entraineurId);

    // Valeurs du filtre "saison"
    @Query("SELECT DISTINCT e.saison FROM Equipe e ORDER BY e.saison DESC")
    List<String> findSaisons();

    // RG-05 : unicité du couple (nom, saison)
    boolean existsByNomAndSaison(String nom, String saison);

    boolean existsByNomAndSaisonAndIdNot(String nom, String saison, Long id);

    // RG-16 : à la suppression d'un entraîneur, ses équipes restent sans référent.
    // À appeler par le service AVANT de supprimer l'entraîneur.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Equipe e SET e.entraineur = null WHERE e.entraineur.id = :entraineurId")
    int retirerEntraineur(@Param("entraineurId") Long entraineurId);
}
