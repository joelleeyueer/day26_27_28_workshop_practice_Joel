package nus.iss.day26_27_workshop_practice_Joel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.JsonObject;
import nus.iss.day26_27_workshop_practice_Joel.models.Comment;
import nus.iss.day26_27_workshop_practice_Joel.models.UpdateReview;
import nus.iss.day26_27_workshop_practice_Joel.repositories.GameRepository;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public JsonObject getGamesByName(int limit, int offset){
        return gameRepository.getGamesByName(limit, offset);
    }

    public JsonObject getGamesByRank(int limit, int offset){
        return gameRepository.getGamesByRank(limit, offset);
    }

    public JsonObject getOneGameDetails(int gid){
        return gameRepository.getOneGameDetail(gid);
    }

    public JsonObject insertComment(Comment comment){
        return gameRepository.insertComment(comment);
    }

    public JsonObject updateReview(UpdateReview updateReview){
        return gameRepository.updateReview(updateReview);
    }

    public JsonObject getLatestReview(String cid){
        return gameRepository.getLatestReview(cid);
    }

    public JsonObject deleteReview(String cid){
        return gameRepository.deleteReview(cid);
    }

    public JsonObject getAllEditsInComment(String cid){
        return gameRepository.getAllEditsInComment(cid);
    }

    
    
}
