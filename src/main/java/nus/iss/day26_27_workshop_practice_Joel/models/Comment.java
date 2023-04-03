package nus.iss.day26_27_workshop_practice_Joel.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private String cId;
    private String user;
    private int rating;
    private String cText;
    private int gid;
    

}
