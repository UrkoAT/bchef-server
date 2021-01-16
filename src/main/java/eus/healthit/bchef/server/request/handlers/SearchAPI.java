package eus.healthit.bchef.server.request.handlers;

import java.sql.SQLException;

import org.json.JSONException;

import eus.healthit.bchef.server.repos.RecipeRepository;
import spark.Request;
import spark.Response;

public class SearchAPI {

	public static String search(Request req, Response res) {
		try {
			return RecipeRepository.searchLike(req.params("like")).toString();
		} catch (JSONException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String page(Request req, Response res) {
		try {
			return RecipeRepository.getPage(req.params("num")).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
