package nus.iss.day26_27_28_workshop_practice_Joel.repositories;


import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import nus.iss.day26_27_28_workshop_practice_Joel.models.Comment;
import nus.iss.day26_27_28_workshop_practice_Joel.models.Game;
import nus.iss.day26_27_28_workshop_practice_Joel.models.UpdateReview;

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

    public JsonObject getLatestReview(String incomingCid){
        Criteria criteria = Criteria.where("c_id").is(incomingCid);
        Query query = Query.query(criteria);

        Document theReview = null;
        try {
            theReview = mongoTemplate.findOne(query, Document.class, "comment");
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        if (theReview == null) {
            return null;
        }

        // System.out.println("Printing gid " + theReview.get("gid")); for testing only

        Criteria criteriaGame = Criteria.where("gid").is(theReview.get("gid"));
        Query queryGame = Query.query(criteriaGame);
        queryGame.fields().include("name").exclude("_id");

        String theGame = null;
        try {
            theGame = mongoTemplate.findOne(queryGame, Game.class, "game").getName();

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        if (theGame == null) {
            return null;
        }

        
        // System.out.println("Printing game name " + theGame); for testing

        String timeNow = LocalDateTime.now().toLocalDate().toString();

        Boolean hasEdited = theReview.containsKey("edited");
        Document latestEdit = null;
        if (hasEdited){
            List<Document> editedList = (List<Document>) theReview.get("edited");
            latestEdit = editedList.get(editedList.size() - 1);
        }

        try {
        // System.out.println("in try updateDocForJson"); for testing
        JsonObject updateDocForJson = Json.createObjectBuilder()
        .add("user", theReview.getString("user"))
        .add("rating", hasEdited ? latestEdit.getInteger("rating").toString() : theReview.getInteger("rating").toString())
        .add("comment", hasEdited ? latestEdit.getString("comment") : theReview.getString("c_text"))
        .add("ID", theReview.getInteger("gid").toString())
        .add("posted", hasEdited ? latestEdit.getString("posted") : "")
        .add("name", theGame)
        .add("edited", Boolean.toString(hasEdited))
        .add("timestamp", timeNow)
        .build();

        // System.out.println("Printing jsonobject: " + updateDocForJson.toString()); for testing

        return updateDocForJson;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    

    public JsonObject getAllEditsInComment(String incomingCid){
        Criteria criteria = Criteria.where("c_id").is(incomingCid);
        Query query = Query.query(criteria);

        Document theReview = null;
        try {
            theReview = mongoTemplate.findOne(query, Document.class, "comment");
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        if (theReview == null) {
            return null;
        }

        // System.out.println("Printing gid " + theReview.get("gid")); for testing only

        Criteria criteriaGame = Criteria.where("gid").is(theReview.get("gid"));
        Query queryGame = Query.query(criteriaGame);
        queryGame.fields().include("name").exclude("_id");

        String theGame = null;
        try {
            theGame = mongoTemplate.findOne(queryGame, Game.class, "game").getName();

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        if (theGame == null) {
            return null;
        }

        
        // System.out.println("Printing game name " + theGame); for testing

        String timeNow = LocalDateTime.now().toLocalDate().toString();

        Boolean hasEdited = theReview.containsKey("edited");
        if (!hasEdited){
            System.out.println("No edits found, returning latest review");
            return getLatestReview(incomingCid);
        }
        System.out.println("Edits found, returning all edits");
        Document latestEdit = null;
        String editedString = null;
        JsonArray editedArray = null;
        if (hasEdited){
            List<Document> editedList = (List<Document>) theReview.get("edited");
            latestEdit = editedList.get(editedList.size() - 1);
            // editedString = "[";
            // for (int i = 0; i < editedList.size(); i++) {
            //     Document editedDoc = editedList.get(i);
            //     editedString += "{ comment: " + editedDoc.getString("comment") +
            //                     ", rating: " + editedDoc.getInteger("rating") +
            //                     ", posted: " + editedDoc.getString("posted") + " }";
            //     if (i != editedList.size() - 1) {
            //         editedString += ", ";
            //     }
            // }
            // editedString += "]";
            JsonArrayBuilder historyArrayBuilder = Json.createArrayBuilder();
            for (Document currentReview : editedList) {
            JsonObjectBuilder reviewObjectBuilder = Json.createObjectBuilder();
            reviewObjectBuilder.add("comment", currentReview.get("comment").toString())
                        .add("name", currentReview.get("rating").toString())
                        .add("posted", currentReview.get("posted").toString());
                        historyArrayBuilder.add(reviewObjectBuilder.build());
                }

             editedArray = historyArrayBuilder.build();
        }

        try {
        // System.out.println("in try updateDocForJson under method getAllEditsInComment");
        JsonObjectBuilder updateDocForJson = Json.createObjectBuilder()
        .add("user", theReview.getString("user"))
        .add("rating", hasEdited ? latestEdit.getInteger("rating").toString() : theReview.getInteger("rating").toString())
        .add("comment", hasEdited ? latestEdit.getString("comment") : theReview.getString("c_text"))
        .add("ID", theReview.getInteger("gid").toString())
        .add("posted", hasEdited ? latestEdit.getString("posted") : "")
        .add("name", theGame)
        .add("edited", editedArray)
        .add("timestamp", timeNow);

        JsonObject updateDocForJsonFinal = updateDocForJson.build();

        // System.out.println("Printing jsonobject: " + updateDocForJson.toString()); for testing

        return updateDocForJsonFinal;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //out of scope --reference only
    public JsonObject deleteReview(String incomingCid){
        Criteria criteria = Criteria.where("c_id").is(incomingCid);
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

        JsonObject deleteDocForJson = Json.createObjectBuilder()
        .add("c_id", incomingCid)
        .add("timestamp", timeNow)
        .build();

        DeleteResult result = mongoTemplate.remove(query, Document.class, "comment");

        System.out.printf("deleted: %d\n", result.getDeletedCount());
        System.out.printf("ack: %b\n", result.wasAcknowledged());

        return deleteDocForJson;
    }
    // db.game.aggregate([
    //     { $match: { gid: 99 } },
    //     {
    //       $lookup: {
    //         from: "comment",
    //         localField: "gid",
    //         foreignField: "gid",
    //         as: "comments"
    //       }
    //     },
    //     {
    //       $addFields: {
    //         average_rating: { $avg: "$comments.rating" },
    //         reviews: "$comments.c_id"
    //       }
    //     },
    //     {
    //       $project: {
    //         _id: 0,
    //         gid: 1,
    //         boardgame_name: "$name",
    //         year: 1,
    //         rank: 1,
    //         average_rating: 1,
    //         users_rated: 1,
    //         url: 1,
    //         image: 1,
    //         reviews: {
    //           $map: {
    //            input: "$comments",
    //             as: "r",
    //             in: {
    //               $concat: ["/review/", "$$r.c_id"]
    //             }
    //           }
    //         },
    //         timestamp: { $toDate: Date.now() }
    //       }
    //     }
    //   ]);
    public JsonObject getGameWithAllReviews(int gid){

        Float incomingAverage = getAverageRating(gid);
        List<String> commentIds = getAllCommentIdsForGid(gid);
        //turn commentIds into JsonArray
        JsonArrayBuilder commentIdsArrayBuilder = Json.createArrayBuilder();
        for (String currentCid : commentIds) {
            String string = "/games/review/" + currentCid;
            commentIdsArrayBuilder.add(string);
        }

        JsonArray commentIdsArray = commentIdsArrayBuilder.build();

        JsonObject gameDetail = getOneGameDetail(gid);
        System.out.println(gameDetail.toString());


        JsonObject averageRating = Json.createObjectBuilder()
        .add("gid", gameDetail.getInt("gid"))
        .add("name", gameDetail.getString("name"))
        .add("year", gameDetail.getInt("year"))
        .add("rank", gameDetail.getInt("ranking"))
        .add("average_rating", incomingAverage)
        .add("users_rated", gameDetail.getInt("users_rated"))
        .add("url", gameDetail.getString("url"))
        .add("thumbnail", gameDetail.getString("image"))
        .add("reviews", commentIdsArray)
        .build();

        return averageRating;
        
        
        
    }

//     db.game.aggregate([
//   {
//     $match: { gid: 99 }
//   },
//   {
//     $lookup: {
//       from: "comment",
//       localField: "gid",
//       foreignField: "gid",
//       as: "comments"
//     }
//   },
//   {
//     $addFields: {
//       average_rating: { $avg: "$comments.rating" }
//     }
//   },
//   {
//     $project: {
//       _id: 0,
//       average_rating: 1
//     }
//   }
// ])

    private Float getAverageRating(int gid){
        System.out.println("in getAverageRating");
        MatchOperation match = Aggregation.match(Criteria.where("gid").is(gid));
        LookupOperation lookupComment = Aggregation.lookup("comment", "gid", "gid", "comments");
        AddFieldsOperation addAverage = Aggregation
                                        .addFields()
                                        .addFieldWithValue("average_rating", new Document("$avg", "$comments.rating"))
                                        .build();
        ProjectionOperation project = Aggregation.project().and("average_rating").as("average_rating");
        Aggregation pipeline = Aggregation.newAggregation(match, lookupComment, addAverage, project);
        Document result = mongoTemplate.aggregate(pipeline, "game", Document.class).getUniqueMappedResult();
        float avgRating = ((Double) result.get("average_rating")).floatValue();
        System.out.println("Average rating is " + avgRating);

        return avgRating;
    }

    // db.comment.find({gid:999}, {_id: 0, c_id: 1})
    public List<String> getAllCommentIdsForGid(int gid) {
        MatchOperation match = Aggregation.match(Criteria.where("gid").is(gid));
        ProjectionOperation project = Aggregation.project().and("c_id").as("c_id");
        Aggregation pipeline = Aggregation.newAggregation(match, project);
        AggregationResults<Document> results = mongoTemplate.aggregate(pipeline, "comment", Document.class);
        List<Document> commentDocuments = results.getMappedResults();
        List<String> commentIds = new ArrayList<>();
        for (Document commentDocument : commentDocuments) {
            String commentId = commentDocument.getString("c_id");
            // System.out.println("commentId is "+ commentId);
            commentIds.add(commentId);
        }
        return commentIds;
    }
    

    


    //helper method
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
