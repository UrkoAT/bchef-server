package eus.healthit.bchef.server.request.handlers;

import java.sql.Timestamp;
import java.time.Instant;

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
			String profilePicPath = (json.getString("profilepic").equals("default"))?"default":ImageRepository.saveImage(json.getString("profilepic"));
			String email = (String) json.get("email");
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			UserRepository.addUser(name, surname, profilePicPath, email, username, password);
			return new JSONObject().put("status",StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject auth(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			return UserRepository.auth(username, password);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject userUpdate(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id_user");
			String name = json.getString("name");
			String surname = json.getString("surname");
			String img = (String) json.get("profilepic");
			String profilePicPath = (img.equals("nochange"))?"nochange":ImageRepository.saveImage(img);
			String email = json.getString("email");
			String username = json.getString("username");
			String password = json.getString("password");
			UserRepository.userUpdate(id, name, surname, profilePicPath, email, username, password);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			e.printStackTrace();
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject checkUser(Request req, Response res) {
		try {
			String username = req.queryParams("username");
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
			UserRepository.shopSet(id, (json.getBoolean("ticked")));
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject getById(Request req, Response res) {
		try {
			int id = Integer.valueOf(req.queryParams("id"));
			return UserRepository.getUserById(id);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject addHistory(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id");
			String uuid = json.getString("uuid");
			RecipeRepository.makeHistoryRelation(id, uuid);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject makeSavedRelation(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id");
			String uuid = json.getString("uuid");
			UserRepository.makeSavedRelation(id, uuid);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject removeSavedRelation(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id");
			String uuid = json.getString("uuid");
			UserRepository.removeSavedRelation(id, uuid);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject follow(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id");
			Integer id_followed = json.getInt("id_followed");
			UserRepository.follow(id, id_followed);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject unfollow(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			Integer id = json.getInt("id");
			Integer id_followed = json.getInt("id_followed");
			UserRepository.unfollow(id, id_followed);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject getName(Request req, Response res) {
		try {
			Integer id = Integer.valueOf(req.queryParams("id"));
			String name = UserRepository.getName(id);
			return QueryCon.statusMessage(StatusCode.SUCCESSFUL).put("name", name);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}
	
	public static JSONObject reauth(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			return UserRepository.reauth(username, QueryCon.md5(password));
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

	public static JSONObject getAllUsers(Request req, Response res) {
		try {
			return UserRepository.getAllUsers().put("status", StatusCode.SUCCESSFUL);
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}
	
	public static JSONObject getHistoryBetween(Request req, Response res) {
		int userId = Integer.valueOf(req.queryParams("userid"));
		String from = req.queryParams("from");
		Instant tFrom = Instant.parse(from);
		
		String until = req.queryParams("until");
		Instant tUntil = Instant.parse(until);
		try {
			return UserRepository.getHistoryBetween(userId, Timestamp.from(tFrom), Timestamp.from(tUntil));
		} catch (Exception e) {
			return QueryCon.statusMessage(QueryCon.exceptionHandler(e));
		}
	}

}
