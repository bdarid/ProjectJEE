package ma.ensam.clubfootball.dto;

import ma.ensam.clubfootball.entity.Equipe;

public record BilanEquipeDTO(
        Equipe equipe,
        int nombreJoueurs,
        int matchsJoues,
        int victoires,
        int nuls,
        int defaites,
        int butsMarques,
        int butsEncaisses,
        int differenceButs,
        int points
) {
}