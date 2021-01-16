package eus.healthit.bchef.server.request.handlers;

import eus.healthit.bchef.server.repos.IngredientRepository;
import spark.Request;
import spark.Response;

public class IngredientAPI {

	public static String ingredientLike(Request req, Response res) {
		try {
			return IngredientRepository.ingredientLike(req.params("like")).toString();
		} catch (Exception e) {
			return null;
		}
	}

}
