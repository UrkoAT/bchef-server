package eus.healthit.bchef.server.request.handlers;

import org.json.JSONObject;

import eus.healthit.bchef.server.repos.QueryCon;
import eus.healthit.bchef.server.repos.RecipeRepository;
import eus.healthit.bchef.server.request.StatusCode;
import spark.Request;
import spark.Response;

public class RecipeAPI {

	public static JSONObject addRecipe(Request req, Response res) {
		try {
			RecipeRepository.insertRecipe(new JSONObject(req.body()));
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject vote(Request req, Response res) {
		JSONObject json = new JSONObject(req.body());
		Integer id = json.getInt("id_user");
		String uuid = json.getString("uuid_recipe");
		Integer rating = json.getInt("rating");

		try {
			RecipeRepository.vote(id, uuid, rating);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

}
