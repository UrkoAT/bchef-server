package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import eus.healthit.bchef.server.request.StatusCode;

public class UserRepository {

	public static void addUser(String name, String surname, String profilePicPath, String email, String username,
			String password) throws PSQLException, SQLException {
		String query = "INSERT INTO public.users (name, surname, profilepic, email, username, pass) VALUES"
				+ String.format("('%s', '%s', '%s', '%s', '%s', '%s')", name, surname, profilePicPath, email, username,
						QueryCon.md5(password));
		QueryCon.execute(query);
	}

	public static JSONObject auth(String username, String password) throws SQLException {
		String query = "SELECT * FROM public.users WHERE UPPER(users.username) = "
				+ String.format("UPPER('%s') AND pass = '%s'", username, QueryCon.md5(password));
		ResultSet rSet = QueryCon.executeQuery(query);
		if (!rSet.next()) {
			return new JSONObject().put("status", StatusCode.LOGIN_ERROR);
		}
		return parseUser(rSet);
	}

	private static int getFollowers(int id) throws SQLException {
		String query = "SELECT COUNT(*) FROM public.rel_followed INNER JOIN public.users ON (rel_followed.id_user = users.id)"
				+ "WHERE rel_followed.id_followed = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		return rSet.getInt("count");
	}

	private static List<Integer> getFollowed(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_followed WHERE rel_followed.id_user = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		List<Integer> jsonList = new ArrayList<>();
		while (rSet.next()) {
			jsonList.add(rSet.getInt("id_followed"));
		}
		return jsonList;
	}

	private static JSONArray getShoplist(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_shoplist INNER JOIN public.shoplist ON (rel_shoplist.id_shoplist_item = shoplist.id_shoplist)"
				+ "WHERE rel_shoplist.id_user = " + id + "";
		ResultSet rSetShop = QueryCon.executeQuery(query);
		
		JSONArray shoplist = new JSONArray();
		while (rSetShop.next()) {
			JSONObject innerJsonObject = new JSONObject();
			innerJsonObject.put("name", rSetShop.getString("name")).put("ticked", rSetShop.getBoolean("ticked"));
			shoplist.put(innerJsonObject);
			System.out.println(innerJsonObject.toString());
		}
		return shoplist;
	}

	public static JSONObject getUserById(int id) throws SQLException {
		String query = "SELECT * FROM public.users WHERE users.id = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		if (rSet.next()) {
			return parseUser(rSet);
		}
		return QueryCon.statusMessage(StatusCode.BAD_REQUEST);
	}

	private static JSONObject parseUser(ResultSet rSet) throws SQLException {
		JSONObject user = new JSONObject();
		int id = rSet.getInt("id");
		String pString = rSet.getString("profilepic");
		user.put("id", rSet.getInt("id")).put("name", rSet.getString("name")).put("surname", rSet.getString("surname"))
				.put("email", rSet.getString("email"))
				.put("profilepic", (pString.equals("default")) ? "default" : ImageRepository.encodeImage(pString))
				.put("shoplist", getShoplist(id)).put("followed", getFollowed(id)).put("followers", getFollowers(id))
				.put("saved", RecipeRepository.getSaved(id)).put("username", rSet.getString("username"))
				.put("history", RecipeRepository.getHistory(id)).put("published", getPublished(id));
		return user.put("status", StatusCode.SUCCESSFUL);
	}

	private static JSONArray getPublished(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_published INNER JOIN public.recipes ON (rel_published.uuid_recipe = recipes.uuid) WHERE rel_published.id_user = "
				+ id;
		ResultSet rSet = QueryCon.executeQuery(query);
		JSONArray array = RecipeRepository.parseRecipeList(rSet);
		return array;
	}

	public static JSONObject shopAdd(String name, int id) throws SQLException {
		String query = "INSERT INTO public.shoplist (name, ticked) VALUES ('" + name + "', false) RETURNING id_shoplist";
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		Integer idSh = rSet.getInt("id_shoplist");
		query = "INSERT INTO public.rel_shoplist VALUES (" + id + ", " + idSh + ")";
		QueryCon.execute(query);
		JSONObject json = new JSONObject();
		json.put("id", idSh);
		json.put("status", StatusCode.SUCCESSFUL);
		return json;
	}

	public static void shopSet(int id, boolean ticked) throws SQLException {
		String query = "UPDATE public.shoplist SET shoplist.ticked = " + ticked + " WHERE shoplist.id_shoplist = " + id;
		QueryCon.execute(query);
	}

	public static void userUpdate(Integer id, String name, String surname, String profilePicPath, String email,
			String username, String password) throws SQLException {
		String query;
		if (profilePicPath.equals("nochange")) {
			query = String.format(
					"UPDATE public.users SET name = '%s', surname = '%s',"
							+ " email = '%s', username = '%s', pass = '%s' WHERE id = %d",
					name, surname, email, username, QueryCon.md5(password), id);

		}
		query = String.format(
				"UPDATE public.users SET name = '%s', surname = '%s',"
						+ " profilepic = '%s', email = '%s', username = '%s', pass = '%s' WHERE id = %d",
				name, surname, profilePicPath, email, username, QueryCon.md5(password), id);
		System.out.println(query);
		QueryCon.execute(query);
	}

	public static JSONObject checkUser(String username) throws SQLException {
		String query = "SELECT COUNT(*) FROM public.users WHERE users.username = '" + username + "'";
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		Integer count = rSet.getInt("count");
		if (count == 1) {
			return QueryCon.statusMessage(StatusCode.USER_DUPLICATED);
		} else {
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		}
	}

	public static void shopRemove(int id) throws SQLException {
		String query = "DELETE FROM public.shoplist WHERE shoplist.id_shoplist = " + id;
		QueryCon.execute(query);
	}

	public static void makeSavedRelation(int userID, String uuid) throws SQLException {
		String query = String.format("INSERT INTO public.rel_saved VALUES (%d, '%s' )", userID, uuid);
		QueryCon.execute(query);
	}

	public static void removeSavedRelation(int userID, String uuid) throws SQLException {
		String query = String.format(
				"DELETE FROM public.rel_saved WHERE rel_saved.id_user = %d AND rel_saved.uuid_recipe ='%s' )", userID,
				uuid);
		QueryCon.execute(query);
	}

	public static void follow(Integer id, Integer id_followed) throws SQLException {
		String query = String.format("INSERT INTO  public.rel_followed VALUES ('%s', '%s')", id, id_followed);
		QueryCon.execute(query);
	}

	public static void unfollow(Integer id, Integer id_followed) throws SQLException {
		String query = String.format(
				"DELETE FROM public.rel_followed WHERE rel_followed.id_user = %d AND rel_followed.id_followed = %d )",
				id, id_followed);
		QueryCon.execute(query);
	}

	public static String getName(Integer id) throws SQLException {
		String query = "SELECT username FROM public.users WHERE users.id = " + id;
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		return rSet.getString("username");
	}

	public static JSONObject reauth(String username, String password) throws SQLException {
		String query = "SELECT COUNT(*) FROM public.users WHERE users.username = '" + username + "' AND users.pass = '"
				+ password + "'";
		ResultSet rSet = QueryCon.executeQuery(query);
		rSet.next();
		if (rSet.getInt("count") == 1) {
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} else {
			return QueryCon.statusMessage(StatusCode.LOGIN_ERROR);
		}
	}

	public static JSONObject getAllUsers() throws SQLException {
		String query = "SELECT id, name, surname, email FROM public.users";
		ResultSet rSet = QueryCon.executeQuery(query);
		JSONArray array = new JSONArray();
		while(rSet.next()) {
			array.put(new JSONObject().put("id", rSet.getInt("id")).put("name", rSet.getString("name"))
					.put("surname", rSet.getString("surname")).put("email", rSet.getString("email")));
		}
		return new JSONObject().put("users", array).put("status", StatusCode.SUCCESSFUL);
	}

	public static JSONObject getHistoryBetween(int userId, Timestamp from, Timestamp until) throws SQLException {
		String query = "SELECT * FROM public.rel_history INNER JOIN public.recipes ON (rel_history.uuid_recipe = recipes.uuid) WHERE rel_history.id_user = "+userId 
				+"AND recipes.publish_date BETWEEN '"+from.toString()+"' AND '"+until.toString()+"'";
		ResultSet rSet = QueryCon.executeQuery(query);
		JSONArray array = RecipeRepository.parseRecipeList(rSet);
		return new JSONObject().put("history", array).put("status", StatusCode.SUCCESSFUL);
	}

}