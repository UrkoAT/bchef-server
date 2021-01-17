package eus.healthit.bchef.server.request.handlers;

import org.json.JSONObject;

import eus.healthit.bchef.server.repos.IngredientRepository;
import eus.healthit.bchef.server.repos.QueryCon;
import eus.healthit.bchef.server.request.StatusCode;
import spark.Request;
import spark.Response;

public class IngredientAPI {

	public static JSONObject ingredientLike(Request req, Response res) {
		try {
			return IngredientRepository.ingredientLike(req.params("like")).put("status", StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

}
