package ma.ensam.clubfootball.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ma.ensam.clubfootball.entity.ParticipationMatch;

public interface ParticipationMatchRepository extends JpaRepository<ParticipationMatch, Long> {

    List<ParticipationMatch> findByMatchId(Long matchId);

    List<ParticipationMatch> findByJoueurId(Long joueurId);

    // RG-08 : une seule participation par joueur et par match
    Optional<ParticipationMatch> findByJoueurIdAndMatchId(Long joueurId, Long matchId);

    boolean existsByJoueurIdAndMatchId(Long joueurId, Long matchId);

    // BF-20 : nombre de matchs d'un joueur
    long countByJoueurId(Long joueurId);

    // BF-20 : buts et cartons d'un joueur
    @Query("SELECT COALESCE(SUM(p.buts), 0L) FROM ParticipationMatch p WHERE p.joueur.id = :joueurId")
    long totalButsDuJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT COALESCE(SUM(p.cartonsJaunes), 0L) FROM ParticipationMatch p WHERE p.joueur.id = :joueurId")
    long totalCartonsJaunesDuJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT COALESCE(SUM(p.cartonsRouges), 0L) FROM ParticipationMatch p WHERE p.joueur.id = :joueurId")
    long totalCartonsRougesDuJoueur(@Param("joueurId") Long joueurId);

    // RG-09 : les buts des participants ne doivent pas dépasser ceux de l'équipe
    @Query("SELECT COALESCE(SUM(p.buts), 0L) FROM ParticipationMatch p WHERE p.match.id = :matchId")
    long totalButsDuMatch(@Param("matchId") Long matchId);

    void deleteByMatchId(Long matchId);
}
