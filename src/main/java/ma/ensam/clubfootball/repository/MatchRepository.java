package ma.ensam.clubfootball.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ma.ensam.clubfootball.entity.Match;
import ma.ensam.clubfootball.enums.StatutMatch;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByEquipeIdOrderByDateHeureDesc(Long equipeId);

    Page<Match> findByEquipeId(Long equipeId, Pageable pageable);

    // BF-19 : bilan d'une équipe (matchs joués)
    List<Match> findByEquipeIdAndStatut(Long equipeId, StatutMatch statut);

    // BF-22 : derniers résultats (statut JOUE)
    List<Match> findTop5ByStatutOrderByDateHeureDesc(StatutMatch statut);

    // BF-22 : prochains matchs (statut PROGRAMME, passer LocalDateTime.now())
    List<Match> findTop5ByStatutAndDateHeureAfterOrderByDateHeureAsc(StatutMatch statut, LocalDateTime maintenant);

    // RG-14 : une équipe qui a encore des matchs ne peut pas être supprimée
    long countByEquipeId(Long equipeId);
}
