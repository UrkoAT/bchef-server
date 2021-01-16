package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.postgresql.util.PSQLException;

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

	public static JSONObject shopList() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String shopAdd() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static String shopRemove() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static String shopSet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static String userUpdate(String name, String surname, String profilePicPath, String email, String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	

}