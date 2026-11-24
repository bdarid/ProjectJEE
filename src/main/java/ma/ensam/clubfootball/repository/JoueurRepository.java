package ma.ensam.clubfootball.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.enums.PosteJoueur;

public interface JoueurRepository extends JpaRepository<Joueur, Long> {

    // BF-10 : effectif d'une équipe
    List<Joueur> findByEquipeIdOrderByNomAscPrenomAsc(Long equipeId);

    // RG-14 : une équipe qui a encore des joueurs ne peut pas être supprimée
    long countByEquipeId(Long equipeId);

    // RG-03 : numéro de maillot unique dans une équipe
    boolean existsByEquipeIdAndNumeroMaillot(Long equipeId, Integer numeroMaillot);

    boolean existsByEquipeIdAndNumeroMaillotAndIdNot(Long equipeId, Integer numeroMaillot, Long id);

    // BF-05 / BF-23 : recherche multicritères.
    // Chaque filtre est facultatif : passer null pour l'ignorer (et null, pas "", pour q).
    @Query("""
            SELECT j FROM Joueur j
            WHERE (:q IS NULL
                   OR LOWER(j.nom) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(j.prenom) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:poste IS NULL OR j.poste = :poste)
              AND (:equipeId IS NULL OR j.equipe.id = :equipeId)
            """)
    Page<Joueur> rechercher(@Param("q") String q,
                            @Param("poste") PosteJoueur poste,
                            @Param("equipeId") Long equipeId,
                            Pageable pageable);
}
