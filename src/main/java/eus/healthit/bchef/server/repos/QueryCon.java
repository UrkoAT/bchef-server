package eus.healthit.bchef.server.repos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import eus.healthit.bchef.server.request.StatusCode;

public class QueryCon {
	public static final String DBURL = "jdbc:postgresql://localhost:5432/Data";

	static Connection connection;
	static Statement statement;
	
	
	
	/*
	 * Encima que pongo el github en publico...... A ver como accedeis ahora a credentials.cred
	 * 
	 */
	public static String leerCredenciales() {
		String password = null;
		try (BufferedReader lector = new BufferedReader(new FileReader("credentials.cred"))) {
			String linea;
			while ((linea = lector.readLine()) != null) {
				password = linea; 
			}
		}catch (Exception e) {
			e.printStackTrace();
			}
		return password;
	}

	private static Statement getStatement() throws SQLException {
		if (statement == null) {
			connection = DriverManager.getConnection(DBURL, "postgres", leerCredenciales());
			statement = connection.createStatement();
		}
		try {
			statement = connection.createStatement();
			statement.executeQuery("SELECT * FROM public.users WHERE 1 = 0");
		} catch (Exception e) {
			connection = DriverManager.getConnection(DBURL, "postgres", "mutriku123");
			statement = connection.createStatement();
		}
		return statement;
	}

	/*
	 * private static Connection getConnection() { if (connection == null) { try {
	 * connection = DriverManager.getConnection(DBURL, "postgres", "mutriku123"); }
	 * catch (Exception e) { e.printStackTrace(); connection = null; } } return
	 * connection; }
	 */
	public static String md5(String base) {
		try {
			MessageDigest digest = MessageDigest.getInstance("md5");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void closeConn() {
		try {
			statement.close();
			connection.close();
		} catch (Exception e) {
		}
	}

	public static ResultSet executeQuery(String query) throws SQLException {
		ResultSet rSet;
		Statement stmt = getStatement();
		rSet = stmt.executeQuery(query);
		return rSet;
	}

	public static void execute(String query) throws SQLException {
		Statement stmt = getStatement();
		stmt.execute(query);
	}

	public static StatusCode exceptionHandler(Exception e) {
		e.printStackTrace();
		if (e instanceof JSONException) {
			spark.Spark.halt(400, "400 BAD REQUEST");
			return StatusCode.BAD_REQUEST;
		} else if (e instanceof PSQLException) {
			spark.Spark.halt(409, "409 CONFLICT");
			return StatusCode.USER_DUPLICATED;
		} else {
			spark.Spark.halt(500, "500 INTERNAL SERVER ERROR");
			return StatusCode.SERVER_ERROR;
		}
	}

	public static JSONObject statusMessage(StatusCode s) {
		return new JSONObject().put("status", s);
	}

}
