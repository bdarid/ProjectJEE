package ma.ensam.clubfootball.dto;

public record StatistiquesJoueurDTO(
        int age,
        double tauxPresence,
        int matchsJoues,
        int titularisations,
        int minutesJouees,
        int totalButs,
        int cartonsJaunes,
        int cartonsRouges
) {
}