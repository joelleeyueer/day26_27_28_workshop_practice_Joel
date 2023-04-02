package nus.iss.day26_workshop_practice_Joel.repositories;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.Document;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import nus.iss.day26_workshop_practice_Joel.models.Game;

@Repository
public class GameRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public JsonObject getGamesByName(int limit, int offset){

            Criteria criteria = Criteria.where("gid").exists(true);
            Query query = Query.query(criteria).with(Sort.by(Sort.Direction.ASC,"gid")).skip(offset).limit(limit);
    
            List<Game> gamesList = mongoTemplate.find(query, Game.class, "game");
    
            // System.out.println("Printing getGamesByName " + gamesList);
    
            //jsonobectbuilder to build the json
            // JsonObjectBuilder gamesObject = Json.createObjectBuilder();
            JsonObject gamesObject = generateJsonObject(limit, offset, gamesList);
    
            return gamesObject;
        }

    public JsonObject getGamesByRank(int limit, int offset){

        Criteria criteria = Criteria.where("ranking").exists(true);
        Query query = Query.query(criteria).with(Sort.by(Sort.Direction.ASC,"ranking")).skip(offset).limit(limit);

        List<Game> gamesListSortedRank = mongoTemplate.find(query, Game.class, "game");

        // System.out.println(gamesList);

        //jsonobectbuilder to build the json
        // JsonObjectBuilder gamesObject = Json.createObjectBuilder();
        JsonObject gamesObject = generateJsonObject(limit, offset, gamesListSortedRank);

        return gamesObject;
    }

    public JsonObject getOneGameDetail(int gid){
        Criteria criteria = Criteria.where("gid").is(gid);
        Query query = Query.query(criteria);
        String getOneGameDetails = mongoTemplate.findOne(query, String.class, "game");
        if (getOneGameDetails == null) {
            return null;
        }
        JsonObject gameDetailJson = Json.createReader(new StringReader(getOneGameDetails)).readObject();
        return gameDetailJson;

    }



    private JsonObject generateJsonObject(int limit, int offset, List<Game> gamesList) {
        JsonArrayBuilder gamesArrayBuilder = Json.createArrayBuilder();
        for (Game game : gamesList) {
            JsonObjectBuilder gamesObject = Json.createObjectBuilder();
            gamesObject.add("gid", game.getGid())
                        .add("name", game.getName())
                        .add("ranking", game.getRanking());
                        gamesArrayBuilder.add(gamesObject.build());
   
        }
   
         JsonArray gamesArray = gamesArrayBuilder.build();
   
        JsonObjectBuilder gamesObjectBuilder = Json.createObjectBuilder();
        gamesObjectBuilder.add("games", gamesArray)
                    .add("offset", offset)
                    .add("limit", limit)
                    .add("total", gamesList.size())
                    .add("timestamp", LocalDateTime.now().toString());
   
        JsonObject gamesObject = gamesObjectBuilder.build();
        return gamesObject;
    }




    
}
