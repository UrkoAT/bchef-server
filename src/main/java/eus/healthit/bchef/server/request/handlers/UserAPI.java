package eus.healthit.bchef.server.request.handlers;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import eus.healthit.bchef.server.repos.ImageRepository;
import eus.healthit.bchef.server.repos.UserRepository;
import spark.Request;
import spark.Response;

public class UserAPI {

	public static String addUser(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String name = (String) json.get("name");
			String surname = (String) json.get("surname");
			String profilePicPath = ImageRepository.saveImage((String) json.get("profilepic"));
			String email = (String) json.get("email");
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			return UserRepository.addUser(name, surname, profilePicPath, email, username, password).toString();
		} catch (JSONException e) {
			e.printStackTrace();
			spark.Spark.halt(400, "400 BAD REQUEST");
		} catch (PSQLException e) {
			e.printStackTrace();
			spark.Spark.halt(409, "409 CONFLICT");
		} catch (SQLException e) {
			e.printStackTrace();
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
		}
		return null;
	}

	public static String auth(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			return UserRepository.auth(username, password).toString();
		} catch (JSONException e) {
			e.printStackTrace();
			spark.Spark.halt(400, "400 BAD REQUEST");
		} catch (SQLException e) {
			e.printStackTrace();
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
		}
		return null;
	}

	public static String shoplistAdd(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String name = json.getString("name");
			Boolean ticked = json.getBoolean("ticked");
			return UserRepository.shopAdd(name, ticked);
		} catch (JSONException e) {
			e.printStackTrace();
			spark.Spark.halt(400, "400 BAD REQUEST");
		} catch (SQLException e) {
			e.printStackTrace();
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
		}
		return null;
	}

	public static Object userUpdate(Request req, Response res) {
		try {
			JSONObject json = new JSONObject(req.body());
			String name = (String) json.get("name");
			String surname = (String) json.get("surname");
			String profilePicPath = ImageRepository.saveImage((String) json.get("profilepic"));
			String email = (String) json.get("email");
			String username = (String) json.get("username");
			String password = (String) json.get("password");
			
			UserRepository.userUpdate(name, surname, profilePicPath, email, username, password);
			return 1;
		} catch (JSONException e) {
			e.printStackTrace();
			spark.Spark.halt(400, "400 BAD REQUEST");
		} catch (SQLException e) {
			e.printStackTrace();
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
		}
		return null;
	}


	public static Object checkUser(Request req, Response res) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object shoplistRem(Request req, Response res) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object shoplistTick(Request req, Response res) {
		// TODO Auto-generated method stub
		return null;
	}

}
