package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eus.healthit.bchef.server.request.StatusCode;

public class IngredientRepository {

	public static List<JSONObject> getByUuid(String uuid) throws SQLException {
		String query = "SELECT * FROM public.rel_ingredients INNER JOIN public.ingredients ON (rel_ingredients.id_ingredient = ingredients.id)"
				+ "WHERE rel_ingredients.uuid_recipe = '" + uuid + "'";
		ResultSet rSet = QueryCon.executeQuery(query);
		return parseIngredientList(rSet);
	}

	private static JSONObject parseIngredient(ResultSet rset) throws JSONException, SQLException {
		JSONObject ingredient = new JSONObject();
		ingredient.put("id", rset.getInt("id")).put("name", rset.getString("name")).put("type", rset.getString("type"))
				.put("amount", rset.getString("amount"));
		return ingredient;
	}

	private static List<JSONObject> parseIngredientList(ResultSet rSet) throws SQLException {
		List<JSONObject> list = new ArrayList<>();
		while (rSet.next()) {
			list.add(parseIngredient(rSet));
		}
		return list;
	}

	public static JSONObject ingredientLike(String like) throws JSONException, SQLException {
		String query = "SELECT * FROM public.ingredients WHERE ingredients.name LIKE '%" + like + "%' LIMIT 10";
		ResultSet rSet = QueryCon.executeQuery(query);
		JSONArray array = new JSONArray();
		while(rSet.next()) {
			JSONObject ingr = new JSONObject().put("id", rSet.getInt("id"))
					.put("name", rSet.getString("name")).put("type", rSet.getString("type"));
			array.put(ingr);
		}
		return new JSONObject().put("ingredients", array).put("status", StatusCode.SUCCESSFUL);
	}

	public static void makeRelation(int idIng, String uuidRecipe, String amount) throws SQLException {
		String query = String.format("INSERT INTO public.rel_ingredients VALUES ('%s', %d, '%s' )", uuidRecipe, idIng,
				amount);
		QueryCon.execute(query);
	}

}
