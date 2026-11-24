package ma.ensam.clubfootball.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ma.ensam.clubfootball.entity.Presence;
import ma.ensam.clubfootball.enums.StatutPresence;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    // Feuille d'appel d'une séance
    List<Presence> findByEntrainementId(Long entrainementId);

    // BF-14 : historique d'un joueur, de la séance la plus récente à la plus ancienne
    List<Presence> findByJoueurIdOrderByEntrainementDateHeureDesc(Long joueurId);

    // RG-02 : retrouver la présence existante pour la mettre à jour au lieu d'en créer une 2e
    Optional<Presence> findByJoueurIdAndEntrainementId(Long joueurId, Long entrainementId);

    // BF-15 : taux de présence = countByJoueurIdAndStatutIn(PRESENT, RETARD) / countByJoueurId
    long countByJoueurId(Long joueurId);

    long countByJoueurIdAndStatutIn(Long joueurId, Collection<StatutPresence> statuts);

    long countByEntrainementIdAndStatutIn(Long entrainementId, Collection<StatutPresence> statuts);

    Optional<Presence> findByEntrainementIdAndJoueurId(Long entrainementId, Long joueurId);

    List<Presence> findByJoueurId(Long joueurId);
}
