package nus.iss.day26_27_28_workshop_practice_Joel.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {

    private int gid;
    private String name;
    private int ranking;
    
}
