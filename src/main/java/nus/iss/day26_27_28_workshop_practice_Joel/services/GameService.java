package nus.iss.day26_27_28_workshop_practice_Joel.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.JsonObject;
import nus.iss.day26_27_28_workshop_practice_Joel.models.Comment;
import nus.iss.day26_27_28_workshop_practice_Joel.models.UpdateReview;
import nus.iss.day26_27_28_workshop_practice_Joel.repositories.GameRepository;

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

    public JsonObject getGameWithAllReviews(int gid){
        return gameRepository.getGameWithAllReviews(gid);
    }

    public JsonObject getAllGamesHighestLowestRating(String rating, int limit, int offset){
        return gameRepository.getAllGamesHighestLowestRating(rating, limit, offset);
    }

    public JsonObject testing(int gid){
        return gameRepository.getHighestReviewByGidAshi(gid);
    }

    public List<String> testing2(){
        return gameRepository.getAllGidsAshi();
    }

    
    
}
