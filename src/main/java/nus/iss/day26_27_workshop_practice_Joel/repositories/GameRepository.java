package nus.iss.day26_27_workshop_practice_Joel.repositories;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import nus.iss.day26_27_workshop_practice_Joel.models.Comment;
import nus.iss.day26_27_workshop_practice_Joel.models.Game;
import nus.iss.day26_27_workshop_practice_Joel.models.UpdateReview;

@Repository
public class GameRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

//    db.game.find({}, { gid: 1, name: 1}).limit(20).skip(0).sort({ gid: 1 })

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
    // {
    //     "_id" : ObjectId("6423b69fdcece521dc1419ec"),
    //     "c_id" : "091910b8",
    //     "user" : "PAYDIRT",
    //     "rating" : NumberInt(6),
    //     "c_text" : "A detailed tactical game on air and air to ground combat missions in the The Vietnam War/Second Indochina War.  Worth a look if the topic interests you.  The 2nd edition bookcase version is a cleaned up and better version.",
    //     "gid" : NumberInt(6228)
    // }
    public JsonObject insertComment(Comment incomingComment){
        // Criteria criteria = Criteria.where("gid").is(incomingComment.getGid());
        // Query query = Query.query(criteria);

        Criteria criteria = Criteria.where("gid").is(incomingComment.getGid());
        Query query = Query.query(criteria);
        query.fields().include("name").exclude("_id");
        

        String theGame = null;
        try {
            theGame = mongoTemplate.findOne(query, Game.class, "game").getName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (theGame == null) {
            return null;
        }

        Document commentDocForJson = new Document()
        .append("user", incomingComment.getUser())
        .append("rating", incomingComment.getRating())
        .append("comment", incomingComment.getCText())
        .append("ID", incomingComment.getGid())
        .append("posted", LocalDateTime.now().toLocalDate().toString())
        .append("name", theGame);

        Document commentDocForMongo = new Document()
        .append("c_id", incomingComment.getCId())
        .append("user", incomingComment.getUser())
        .append("rating", incomingComment.getRating())
        .append("c_text", incomingComment.getCText())
        .append("gid", incomingComment.getGid());

        

        Document result = mongoTemplate.insert(commentDocForMongo, "comment");

        System.out.printf("inserted: %s\n", result.toString());

        String jsonString = commentDocForJson.toJson();
        JsonObject commentJson = Json.createReader(new StringReader(jsonString)).readObject();

        return commentJson;
    }

    public JsonObject updateReview(UpdateReview incomingUpdate){

        Criteria criteria = Criteria.where("c_id").is(incomingUpdate.getCId());
        Query query = Query.query(criteria);

        String theCid = null;
        try {
            theCid = mongoTemplate.findOne(query, String.class, "comment").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (theCid == null) {
            return null;
        }

        String timeNow = LocalDateTime.now().toLocalDate().toString();

        JsonObject updateDocForJson = Json.createObjectBuilder()
        .add("comment", incomingUpdate.getComment())
        .add("rating", incomingUpdate.getRating())
        .add("posted", timeNow)
        .build();

        Document updateDocForMongo = new Document()
        .append("comment", incomingUpdate.getComment())
        .append("rating", incomingUpdate.getRating())
        .append("posted", timeNow);

        Update updateOps = new Update()
            .push("edited",updateDocForMongo);

        UpdateResult result = mongoTemplate.updateFirst(query, updateOps, Document.class, "comment");

        System.out.printf("matched: %d\n", result.getMatchedCount());
        System.out.printf("modified: %d\n", result.getModifiedCount());
        System.out.printf("ack: %b\n", result.wasAcknowledged());

        return updateDocForJson;

    }
    


    //helper
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
