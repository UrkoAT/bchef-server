package eus.healthit.bchef.server.request.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import eus.healthit.bchef.server.repos.RecipeRepository;
import spark.Request;
import spark.Response;

public class RecipeAPI {

	public static String addRecipe(Request req, Response res) {
		try {
			return RecipeRepository.insertRecipe(new JSONObject(req.body())).toString();
		} catch (JSONException e) {
			spark.Spark.halt(400, "500 BAD REQUEST");
		} catch (Exception e) {
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
		}
		return null;
	}


	public static String vote(Request req, Response res) {
		try {
			return RecipeRepository.vote(new JSONObject(req.body())).toString();
		} catch (JSONException e) {
			spark.Spark.halt(400, "500 BAD REQUEST");
		} catch (Exception e) {
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
		}
		return null;
	}

}
