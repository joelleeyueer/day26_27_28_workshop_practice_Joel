package nus.iss.day26_27_workshop_practice_Joel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.JsonObject;
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
    
    
}
