package ma.ensam.clubfootball.dto;

import ma.ensam.clubfootball.entity.Entrainement;
import ma.ensam.clubfootball.entity.Match;

import java.util.List;

public record DashboardGlobalDTO(
        long totalJoueurs,
        long totalEquipes,
        long totalMatchs,
        long totalEntrainements,
        List<Entrainement> prochainsEntrainements,
        List<Match> prochainsMatchs,
        List<Match> derniersResultats
) {
}