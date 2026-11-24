package ma.ensam.clubfootball.dto;

import ma.ensam.clubfootball.entity.Entrainement;
import ma.ensam.clubfootball.entity.Equipe;
import ma.ensam.clubfootball.entity.Joueur;
import ma.ensam.clubfootball.entity.Match;
import ma.ensam.clubfootball.entity.Presence;

import java.util.List;

public record DashboardJoueurDTO(
        Joueur joueur,
        Equipe equipe,
        List<Entrainement> prochainsEntrainements,
        List<Match> prochainsMatchs,
        List<Presence> historiquePresences,
        StatistiquesJoueurDTO statistiques
) {
}