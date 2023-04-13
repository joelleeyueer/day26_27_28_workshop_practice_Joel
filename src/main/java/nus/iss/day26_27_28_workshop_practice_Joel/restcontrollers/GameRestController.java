package nus.iss.day26_27_28_workshop_practice_Joel.restcontrollers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.JsonObject;
import nus.iss.day26_27_28_workshop_practice_Joel.models.Comment;
import nus.iss.day26_27_28_workshop_practice_Joel.models.UpdateReview;
import nus.iss.day26_27_28_workshop_practice_Joel.services.GameService;

@RestController
@RequestMapping("/games")
public class GameRestController {

    @Autowired
    private GameService gameService;

    @GetMapping
    public ResponseEntity<String> getGamesByName(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset){
        System.out.println("limit is " + limit + ". offset is " + offset);



        JsonObject incomingGameObject = gameService.getGamesByName(limit, offset);

        try {
            return ResponseEntity.ok(incomingGameObject.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rank")
    public ResponseEntity<String> getGamesByRank(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset){
        // System.out.println("limit is " + limit + ". offset is " + offset);

        JsonObject incomingGameObject = gameService.getGamesByRank(limit, offset);

        try {
            return ResponseEntity.ok(incomingGameObject.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{gid}")
    public ResponseEntity<String> getOneGameDetails(@PathVariable int gid){
        // System.out.println("limit is " + limit + ". offset is " + offset);

        JsonObject incomingGameObject = gameService.getOneGameDetails(gid);

        if (incomingGameObject == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(incomingGameObject.toString());
    }

 

    @PostMapping(path="/review", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> insertComment(@RequestParam MultiValueMap<String, String> formData){
        
        String cId = UUID.randomUUID().toString().substring(0, 8);
        String user = formData.getFirst("name");
        int rating = Integer.parseInt(formData.getFirst("rating"));
        String cText = formData.getFirst("comment");
        int gid = Integer.parseInt(formData.getFirst("gid"));

        Comment incomingComment = new Comment(cId, user, rating, cText, gid);


        JsonObject incomingGameObject = gameService.insertComment(incomingComment);

        if (incomingGameObject == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(incomingGameObject.toString());
    }

    @PutMapping(path="/review/{cid}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> updateReview(@RequestBody UpdateReview updateReview, @PathVariable String cid){

        updateReview.setCId(cid);
        
        JsonObject incomingUpdateReview = gameService.updateReview(updateReview);

        if (incomingUpdateReview == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(incomingUpdateReview.toString());
    }

    @GetMapping(path="/review/{cid}" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getReview(@PathVariable String cid){
            
            JsonObject incomingUpdateReview = gameService.getLatestReview(cid);
    
            if (incomingUpdateReview == null){
                return ResponseEntity.notFound().build();
            }
    
            return ResponseEntity.ok(incomingUpdateReview.toString());
    }

    @GetMapping(path="/review/{cid}/history" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getReviewAllEdit(@PathVariable String cid){
            
            JsonObject incomingUpdateReview = gameService.getAllEditsInComment(cid);
    
            if (incomingUpdateReview == null){
                return ResponseEntity.notFound().build();
            }
    
            return ResponseEntity.ok(incomingUpdateReview.toString());
    }

    @DeleteMapping(path="/review/{cid}")
    public ResponseEntity<String> deleteReview(@PathVariable String cid){
            
            JsonObject incomingUpdateReview = gameService.deleteReview(cid);
    
            if (incomingUpdateReview == null){
                return ResponseEntity.notFound().build();
            }
    
            return ResponseEntity.ok(incomingUpdateReview.toString());
    }

    @GetMapping(path="/{gid}/reviews" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getGameReviews(@PathVariable int gid){
            System.out.println("gid is " + gid);
            JsonObject incomingUpdateReview = gameService.getGameWithAllReviews(gid);
    
            if (incomingUpdateReview == null){
                return ResponseEntity
                        .notFound()
                        .build();
            }
        
            return ResponseEntity.ok(incomingUpdateReview.toString());
    }

    @GetMapping(path="/highest" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHighest(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset){
            JsonObject incomingObject = gameService.getAllGamesHighestLowestRating("highest", limit, offset);
    
            if (incomingObject == null){
                return ResponseEntity
                        .notFound()
                        .build();
            }
        
            return ResponseEntity.ok(incomingObject.toString());
    }

    @GetMapping(path="/lowest" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLowest(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset){
            JsonObject incomingObject = gameService.getAllGamesHighestLowestRating("lowest", limit, offset);
    
            if (incomingObject == null){
                return ResponseEntity
                        .notFound()
                        .build();
            }
        
            return ResponseEntity.ok(incomingObject.toString());
    }

    @GetMapping(path="/testing/{id}" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTesting(@PathVariable int id){
            // JsonObject incomingObject = gameService.testing(id);
            List<String> incomingObject = gameService.testing2();
    
            if (incomingObject == null){
                return ResponseEntity
                        .notFound()
                        .build();
            }
        
            return ResponseEntity.ok(incomingObject.toString());
    }


    
}
