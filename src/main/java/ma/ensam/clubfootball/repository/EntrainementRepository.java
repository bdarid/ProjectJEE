package ma.ensam.clubfootball.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ma.ensam.clubfootball.entity.Entrainement;

public interface EntrainementRepository extends JpaRepository<Entrainement, Long> {

    List<Entrainement> findByEquipeIdOrderByDateHeureDesc(Long equipeId);

    Page<Entrainement> findByEquipeId(Long equipeId, Pageable pageable);

    // Calendrier d'une équipe sur une période
    List<Entrainement> findByEquipeIdAndDateHeureBetweenOrderByDateHeureAsc(
            Long equipeId, LocalDateTime debut, LocalDateTime fin);

    // BF-22 : prochains entraînements (passer LocalDateTime.now())
    List<Entrainement> findTop5ByDateHeureAfterOrderByDateHeureAsc(LocalDateTime maintenant);

    // RG-14 : une équipe qui a encore des entraînements ne peut pas être supprimée
    long countByEquipeId(Long equipeId);
}
