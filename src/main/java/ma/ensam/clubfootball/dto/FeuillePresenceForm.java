package ma.ensam.clubfootball.dto;

import lombok.Data;
import ma.ensam.clubfootball.entity.Presence;
import java.util.ArrayList;
import java.util.List;

@Data
public class FeuillePresenceForm {
    private List<Presence> presences = new ArrayList<>();
}