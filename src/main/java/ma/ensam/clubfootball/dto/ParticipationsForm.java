package ma.ensam.clubfootball.dto;

import lombok.Data;
import ma.ensam.clubfootball.entity.ParticipationMatch;
import java.util.ArrayList;
import java.util.List;

@Data
public class ParticipationsForm {
    private List<ParticipationMatch> participations = new ArrayList<>();
}