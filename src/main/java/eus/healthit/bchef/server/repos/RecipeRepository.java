package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eus.healthit.bchef.server.request.StatusCode;

public class RecipeRepository {

	static JSONArray getSaved(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_saved INNER JOIN public.recipes ON (rel_saved.uuid_recipe = recipes.uuid)"
				+ "WHERE rel_saved.id_user = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		return parseRecipeList(rSet);
	}

	public static JSONArray getHistory(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_history INNER JOIN public.recipes ON (rel_history.uuid_recipe = recipes.uuid)"
				+ "WHERE rel_history.id_user = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		return parseRecipeList(rSet);
	}

	private static JSONObject parseRecipe(ResultSet rSet) throws SQLException {
		JSONObject recipe = new JSONObject();
		String uuid = rSet.getString("uuid");
		System.out.println(uuid);
		recipe.put("uuid", uuid).put("name", rSet.getString("name")).put("author", rSet.getInt("author"))
				.put("rating", RecipeRepository.getRating(uuid))
				.put("publish_date", rSet.getTimestamp("publish_date").toString())
				.put("img", ImageRepository.encodeImage(rSet.getString("img")))
				.put("ingredients", IngredientRepository.getByUuid(uuid))
				.put("description", rSet.getString("description"))
				.put("instructions", InstructionRepository.getByUuid(uuid));
		return recipe;
	}

	public static JSONArray parseRecipeList(ResultSet rSet) throws SQLException {
		JSONArray array = new JSONArray();
		while (rSet.next()) {
			array.put(parseRecipe(rSet));
		}
		return array;
	}

	public static JSONObject searchLike(String like, int page) throws JSONException, SQLException {
		String query = "SELECT * FROM public.recipes WHERE UPPER(recipes.name) LIKE UPPER('%" +
				like + "%') ORDER BY publish_date LIMIT 7 OFFSET " +page;
		ResultSet rSet = QueryCon.executeQuery(query);
		return new JSONObject().put("recipes", parseRecipeList(rSet));
	}

	public static JSONObject getPage(String pagenum) throws SQLException {
		Integer page = Integer.valueOf(pagenum);
		String query = "SELECT * FROM public.recipes ORDER BY publish_date LIMIT 7 OFFSET " + page * 7;
		ResultSet rSet = QueryCon.executeQuery(query);
		return new JSONObject().put("recipes", parseRecipeList(rSet));
	}

	public static void makePublishedRelation(int userID, String uuid) throws SQLException {
		String query = String.format("INSERT INTO public.rel_published VALUES (%d, '%s' )", userID, uuid);
		QueryCon.execute(query);
	}

	public static void makeHistoryRelation(int userID, String uuidRecipe) throws SQLException {
		LocalDate date = LocalDate.now();
		String query = String.format("INSERT INTO public.rel_history VALUES (%d, '%s' '%s')", userID, uuidRecipe,
				date.toString());
		QueryCon.execute(query);
	}

	public static void makeVoteRelation(int userID, String uuidRecipe, int rating) throws SQLException {
		String query = "SELECT COUNT(*) FROM public.rel_rating WHERE id_user = " + userID + "AND uuid_recipe = '"
				+ uuidRecipe + "'";
		
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		if (rSet.getInt("count") == 1) {
			query = "UPDATE public.rel_rating SET rel_rating.rating = " + rating + " WHERE rel_rating.id_user = "
					+ userID + " AND rel_rating.uuid_recipe = " + uuidRecipe;
		} else {
			query = "INSERT INTO public.rel_rating VALUES (" + userID + ", '" + uuidRecipe + "', " + rating + ")";
		}
		QueryCon.execute(query);
	}

	private static Integer getRating(String uuid) throws SQLException {
		String query = "SELECT COUNT(*), SUM(rating) FROM rel_rating WHERE rel_rating.uuid_recipe = '" + uuid + "'";
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		Integer count = rSet.getInt("count");
		Integer sum = rSet.getInt("sum");
		return (count != 0)?(sum / count):5;
	}

	public static void insertRecipe(JSONObject jsonObject) throws JSONException, SQLException {
		String uuid = jsonObject.getString("uuid");
		String name = jsonObject.getString("name");
		Integer author = jsonObject.getInt("author");
		String publishDate = jsonObject.getString("publish_date");
		String description = jsonObject.getString("description");
		String imagePath = ImageRepository.saveImage(jsonObject.getString("img"));
		JSONArray ingredients = jsonObject.getJSONArray("ingredients");
		JSONArray instructions = jsonObject.getJSONArray("instructions");

		/*
		 * No puedo hacer esto por que java es tonto y no puede throwear una excepcion
		 * desde un lambda ingredients.forEach(((json) ->
		 * IngredientRepository.makeRelation(((JSONObject)json).getInt("id"), uuid,
		 * ((JSONObject)json).getString("amount"))));
		 */

		String query = "INSERT INTO public.recipes (uuid, name, author, publish_date, img, description) VALUES " + String
				.format("('%s', '%s', '%s', '%s', '%s', '%s')", uuid, name, author, publishDate, imagePath, description);
		QueryCon.execute(query);
		for (Object object : ingredients) {
			JSONObject json = (JSONObject) object;
			IngredientRepository.makeRelation(json.getInt("id"), uuid, json.getString("amount"));
		}
		for (Object object : instructions) {
			JSONObject json = (JSONObject) object;
			InstructionRepository.makeRelation(InstructionRepository.insertInstruction(json, uuid), uuid);
		}
		vote(author, uuid, 0);
		makePublishedRelation(author, uuid);
	}

	public static void vote(Integer id, String uuid, Integer rating) throws SQLException {
		String query = "INSERT INTO public.rel_rating VALUES (" + id + ", '" + uuid + "', " + rating + ")";
		QueryCon.execute(query);
	}

	public static JSONObject byIngredients(Set<String> set) throws SQLException {
		StringBuilder builder = new StringBuilder("SELECT COUNT(uuid_recipe), uuid_recipe FROM public.rel_ingredients INNER JOIN public.ingredients"
				+ " ON (ingredients.id = rel_ingredients.id_ingredient) WHERE ");
		List<String> comparations = new ArrayList<>();
		int i = 0;
		for (String string : set) {
			comparations.add("UPPER(ingredients.name) LIKE UPPER('%"+string+"%')");
			i++;
		}
		builder.append(String.join(" OR ", comparations));
		builder.append(" GROUP BY uuid_recipe");
		System.out.println(builder.toString());
		ResultSet rSet = QueryCon.executeQuery(builder.toString());
		List<String> searchList = new ArrayList<>();
		while (rSet.next()) {
			if (rSet.getInt("count") == i) {
				searchList.add("'"+rSet.getString("uuid_recipe")+"'");
			}
		}
		if (searchList.size() == 0) {
			return new JSONObject().put("recipes", new JSONArray());
		}
		String query = "SELECT * FROM public.recipes WHERE recipes.uuid IN (" + String.join(", ", searchList) + ")";
		ResultSet returnSet = QueryCon.executeQuery(query);
		
		return new JSONObject().put("recipes", parseRecipeList(returnSet)).put("status", StatusCode.SUCCESSFUL);
	}

}
