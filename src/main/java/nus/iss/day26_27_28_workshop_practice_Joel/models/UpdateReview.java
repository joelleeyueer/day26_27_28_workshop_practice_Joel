package nus.iss.day26_27_28_workshop_practice_Joel.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReview {

    private String comment;
    private int rating;
    private String cId;
    
}
