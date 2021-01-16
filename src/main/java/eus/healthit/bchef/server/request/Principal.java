package eus.healthit.bchef.server.request;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import eus.healthit.bchef.server.request.handlers.IngredientAPI;
import eus.healthit.bchef.server.request.handlers.RecipeAPI;
import eus.healthit.bchef.server.request.handlers.SearchAPI;
import eus.healthit.bchef.server.request.handlers.UserAPI;

public class Principal {
	public static void main(String[] args) {
		
		port(80);
		path("/api", () -> {

			before("/*", (req, res) -> {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				Date date = new Date();
				System.out.println("[" + dateFormat.format(date) + "] INFO Received API call");
			});

			/* ############### GET ############### */
			get("/search", (req, res) -> SearchAPI.search(req, res));
			get("/ingredient", (req, res) -> IngredientAPI.ingredientLike(req, res));
			get("/page", (req, res) -> SearchAPI.page(req, res));
			// get("/izan", (req, res) -> halt(418, "<h1>418 A jorge le gusta el Té"));

			/* ############### PUT ############### */
			before("/user/*", (request, response) -> {
				System.out.println(request.requestMethod());
				if (request.requestMethod() != "PUT")
					halt(405, "<h1>405 Method Not Allowed");
			});
			put("/auth", (req, res) -> UserAPI.auth(req, res));
			path("/user", () -> {
				put("/rate", (req, res) -> RecipeAPI.vote(req, res));
				put("/config", (req, res) -> UserAPI.userUpdate(req, res));
				path("/shoplist", () -> {
					put("/add", (req, res) -> UserAPI.shoplistAdd(req, res));
					put("/remove", (req, res) -> UserAPI.shoplistRem(req, res));
					put("/tick", (req, res) -> UserAPI.shoplistTick(req, res));
				});

			});

			/* ############### POST ############### */
			before("/register/*", (request, response) -> {
				if (request.requestMethod() != "POST")
					halt(405, "<h1>405 Method Not Allowed");
			});
			path("/register", () -> {
				post("/user", (req, res) -> UserAPI.addUser(req, res));
				post("/check", (req, res) -> UserAPI.checkUser(req, res));
				post("/recipe", (req, res) -> RecipeAPI.addRecipe(req, res));
			});
		});
	}
}
