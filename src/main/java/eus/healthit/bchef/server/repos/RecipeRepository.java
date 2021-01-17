package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecipeRepository {

	static List<JSONObject> getSaved(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_saved INNER JOIN public.recipes ON (rel_saved.uuid_recipe = recipes.uuid)"
				+ "WHERE rel_saved.id_user = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		return parseRecipeList(rSet);
	}

	public static List<JSONObject> getHistory(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_history INNER JOIN public.recipes ON (rel_history.uuid_recipe = recipes.uuid)"
				+ "WHERE rel_history.id_user = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		return parseRecipeList(rSet);
	}

	private static JSONObject parseRecipe(ResultSet rSet) throws SQLException {
		JSONObject recipe = new JSONObject();
		String uuid = rSet.getString("uuid");
		recipe.put("uuid", uuid).put("name", rSet.getString("name")).put("author", rSet.getInt("author"))
				.put("rating", RecipeRepository.getRating(uuid))
				.put("timestamp", rSet.getTimestamp("publish_date").toString())
				.put("duration", rSet.getTime("duration").toString())
				.put("img", ImageRepository.encodeImage(rSet.getString("img")))
				.put("ingredients", IngredientRepository.getByUuid(uuid))
				.put("instructions", InstructionRepository.getByUuid(uuid));
		return recipe;
	}

	private static List<JSONObject> parseRecipeList(ResultSet rSet) throws SQLException {
		List<JSONObject> list = new ArrayList<>();
		while (rSet.next()) {
			list.add(parseRecipe(rSet));
		}
		return list;
	}

	public static JSONObject searchLike(String like) throws JSONException, SQLException {
		String query = "SELECT * FROM public.recipes WHERE recipes.name LIKE '%" + like + "%'";
		System.out.println(query);
		ResultSet rSet = QueryCon.executeQuery(query);
		return new JSONObject().put("recipes", parseRecipeList(rSet));
	}

	public static JSONObject getPage(String pagenum) throws SQLException {
		Integer page = Integer.valueOf(pagenum);
		String query = "SELECT * FROM public.recipes LIMIT 7 OFFSET " + page * 7 + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		return new JSONObject().put("recipes", parseRecipeList(rSet));
	}

	public static void makePublishedRelation(int userID, String uuid) throws SQLException {
		String query = String.format("INSERT INTO public.rel_published VALUES (%d, '%s' )", userID, uuid);
		QueryCon.execute(query);
	}

	public static void makeHistoryRelation(int userID, String uuidRecipe) throws SQLException {
		Date date = new Date();
		String query = String.format("INSERT INTO public.rel_history VALUES (%d, '%s' '%s')", userID, uuidRecipe,
				date.toInstant());
		QueryCon.execute(query);
	}

	public static void makeVoteRelation(int userID, String uuidRecipe, int rating) throws SQLException {
		String query = "SELECT COUNT(*) FROM public.rel_rating WHERE id_user = " + userID + "AND uuid_recipe = '"
				+ uuidRecipe + "'";
		if (QueryCon.executeQuery(query).getInt("count") == 1) {
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
		Integer count = rSet.getInt("count");
		Integer sum = rSet.getInt("sum");
		return sum / count;
	}

	public static void insertRecipe(JSONObject jsonObject) throws JSONException, SQLException {
		String uuid = jsonObject.getString("uuid");
		String name = jsonObject.getString("name");
		Integer author = jsonObject.getInt("author");
		String publishDate = jsonObject.getString("publish_date");
		String duration = jsonObject.getString("duration");
		String imagePath = ImageRepository.saveImage(jsonObject.getString("img"));
		JSONArray ingredients = jsonObject.getJSONArray("ingredients");
		JSONArray instructions = jsonObject.getJSONArray("instructions");

		/*
		 * No puedo hacer esto por que java es tonto y no puede throwear una excepcion
		 * desde un lambda ingredients.forEach(((json) ->
		 * IngredientRepository.makeRelation(((JSONObject)json).getInt("id"), uuid,
		 * ((JSONObject)json).getString("amount"))));
		 */

		for (Object object : ingredients) {
			JSONObject json = (JSONObject) object;
			IngredientRepository.makeRelation(json.getInt("id"), uuid, json.getString("amount"));
		}
		for (Object object : instructions) {
			JSONObject json = (JSONObject) object;
			InstructionRepository.makeRelation(InstructionRepository.insertInstruction(json, uuid), uuid);
		}
		String query = "INSERT INTO public.recipes (uuid, name, author, publish_date, dutation, img) VALUES " + String
				.format("('%s', '%s', '%s', '%s', '%s', '%s')", uuid, name, author, publishDate, duration, imagePath);
		QueryCon.execute(query);
		makePublishedRelation(author, uuid);
	}

	public static void vote(Integer id, String uuid, Integer rating) throws SQLException {
		String query = "INSERT INTO public.rel_rating VALUES (" + id + ", '" + uuid + "', " + rating + ")";
		QueryCon.execute(query);
	}

}
