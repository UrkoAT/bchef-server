package eus.healthit.bchef.server.request.handlers;

import org.json.JSONObject;

import eus.healthit.bchef.server.repos.ImageRepository;
import eus.healthit.bchef.server.repos.QueryCon;
import eus.healthit.bchef.server.repos.RecipeRepository;
import eus.healthit.bchef.server.repos.UserRepository;
import eus.healthit.bchef.server.request.StatusCode;
import spark.Request;
import spark.Response;

public class UserAPI {

	public static JSONObject addUser(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String name = (String) json.get("name");
			String surname = (String) json.get("surname");
			String profilePicPath = ImageRepository.saveImage((String) json.get("profilepic"));
			String email = (String) json.get("email");
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			return UserRepository.addUser(name, surname, profilePicPath, email, username, password).put("status",
					StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject auth(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			return UserRepository.auth(username, password).put("status", StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject userUpdate(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id");
			String name = json.getString("name");
			String surname = json.getString("surname");
			String profilePicPath = ImageRepository.saveImage((String) json.get("profilepic"));
			String email = json.getString("email");
			String username = json.getString("username");
			String password = json.getString("password");
			UserRepository.userUpdate(id, name, surname, profilePicPath, email, username, password);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject checkUser(Request req, Response res) {
		try {
			String username = req.params("username");
			return UserRepository.checkUser(username);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject shoplistAdd(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String name = json.getString("name");
			Integer id_user = json.getInt("id_user");
			Boolean ticked = json.getBoolean("ticked");

			return UserRepository.shopAdd(name, id_user);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject shoplistRem(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			int id = json.getInt("id");
			UserRepository.shopRemove(id);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject shoplistTick(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			int id = json.getInt("id");
			UserRepository.shopSet(id, (json.getBoolean("ticked"))?true:false);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject getById(Request req, Response res) {
		try {
			int id = Integer.valueOf(req.params("id"));
			return UserRepository.getUserById(id).put("status", StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject addHistory(Request req, Response res) {
		JSONObject json = new JSONObject(req.body());
		Integer id = json.getInt("id");
		String uuid = json.getString("uuid");
		try {
			RecipeRepository.makeHistoryRelation(id, uuid);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

}
