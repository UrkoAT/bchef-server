package eus.healthit.bchef.server.request.handlers;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import eus.healthit.bchef.server.repos.QueryCon;
import eus.healthit.bchef.server.repos.RecipeRepository;
import eus.healthit.bchef.server.request.StatusCode;
import spark.Request;
import spark.Response;

public class SearchAPI {

	public static JSONObject search(Request req, Response res) {

		String like = req.queryParams("like");
		int page = Integer.valueOf(req.queryParams("page"));
		System.out.println(like);
		try {
			return RecipeRepository.searchLike(like, page).put("status", StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject page(Request req, Response res) {
		try {
			return RecipeRepository.getPage(req.queryParams("num")).put("status", StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject byIngredient(Request req, Response res) {
		Set<String> sech =  req.queryParams();
		Set<String> set = new HashSet<>();
		for (String string : sech) {
			set.add(req.queryParams(string));
		}
		try {
			return RecipeRepository.byIngredients(set);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

}
