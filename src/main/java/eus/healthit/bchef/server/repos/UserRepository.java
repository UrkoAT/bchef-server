package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import eus.healthit.bchef.server.request.StatusCode;

public class UserRepository {

	public static JSONObject addUser(String name, String surname, String profilePicPath, String email, String username,
			String password) throws PSQLException, SQLException {
		String query = "INSERT INTO public.users (name, surname, profilepic, email, username, pass) VALUES"
				+ String.format("('%s', '%s', '%s', '%s', '%s', '%s')", name, surname, profilePicPath, email, username,
						QueryCon.md5(password));
		QueryCon.execute(query);
		return new JSONObject().put("success", true);
	}

	public static JSONObject auth(String username, String password) throws SQLException {
		String query = "SELECT * FROM public.users WHERE UPPER(username) = "
				+ String.format("UPPER('%s') AND pass = '%s') INNER JOIN", username, QueryCon.md5(password));
		ResultSet rSet = QueryCon.executeQuery(query);
		if (!rSet.next()) {
			return new JSONObject().put("valid", false);
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

	private static List<JSONObject> getShoplist(int id) throws SQLException {
		String query = "SELECT * FROM public.rel_shoplist INNER JOIN public.shoplist ON (rel_shoplist.id_shoplist_item = shoplist.id_shoplist)"
				+ "WHERE rel_shoplist.id_shoplist_item = " + id + "";
		ResultSet rSetShop = QueryCon.executeQuery(query);
		List<JSONObject> shoplist = new ArrayList<>();
		while (rSetShop.next()) {
			JSONObject innerJsonObject = new JSONObject();
			innerJsonObject.put("name", rSetShop.getString("name")).put("ticked", rSetShop.getBoolean("ticked"));
			shoplist.add(innerJsonObject);
		}
		return shoplist;
	}

	public static JSONObject getUserById(int id) throws SQLException {
		String query = "SELECT * FROM public.users WHERE users.id = " + id + "";
		ResultSet rSet = QueryCon.executeQuery(query);
		if (!rSet.next()) {
			return parseUser(rSet);
		}
		return null;
	}

	private static JSONObject parseUser(ResultSet rSet) throws SQLException {
		JSONObject user = new JSONObject();
		int id = rSet.getInt("id");
		user.put("id", rSet.getString("int")).put("name", rSet.getString("name"))
				.put("surname", rSet.getString("surname")).put("email", rSet.getString("email"))
				.put("profilepic", ImageRepository.encodeImage(rSet.getString("profilepic")))
				.put("shoplist", getShoplist(id)).put("folowed", getFollowed(id)).put("followers", getFollowers(id))
				.put("saved", RecipeRepository.getSaved(id)).put("history", RecipeRepository.getHistory(id));
		return user;
	}

	public static JSONObject shopAdd(String name, int id) throws SQLException {
		String query = "INSERT INTO public.shoplist (name, ticked) VALUES ('" + name + "', false) RETURNING id";
		Integer idSh = QueryCon.executeQuery(query).getInt("id");
		query = "INSERT INTO public.rel_shoplist VALUES (" + id + ", " + idSh + ")";
		JSONObject json = new JSONObject();
		json.put("item", idSh);
		json.put("status", StatusCode.SUCCESSFUL);
		return json;
	}

	public static void shopSet(int id, boolean ticked) throws SQLException {
		String query = "UPDATE public.shoplist SET shoplist.ticked = " + ticked + " WHERE shoplist.id = " + id;
		QueryCon.execute(query);
	}

	public static void userUpdate(Integer id, String name, String surname, String profilePicPath, String email,
			String username, String password) throws SQLException {
		String query = String.format("UPDATE public.users SET users.name = '%s', users.surname = '%s',"
				+ " users.profilepic = '%s', users.email = '%s', users.username = '%s', users.password = '%s' WHERE users.id = %d",
				name, surname, profilePicPath, email, username, QueryCon.md5(password));
		QueryCon.execute(query);
	}

	public static JSONObject checkUser(String username) throws SQLException {
		String query = "SELECT COUNT(*) FROM public.users WHERE users.username = " + username;
		Integer count = QueryCon.executeQuery(query).getInt("count");
		if (count == 1) {
			return QueryCon.statusMessage(StatusCode.USER_DUPLICATED);
		} else {
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		}
	}

	public static void shopRemove(int id) throws SQLException {
		String query = "DELETE FROM public.shoplist WHERE shoplist.id = " + id;
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

}