package nus.iss.day26_27_workshop_practice_Joel.restcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.JsonObject;
import nus.iss.day26_27_workshop_practice_Joel.services.GameService;

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
    
}
