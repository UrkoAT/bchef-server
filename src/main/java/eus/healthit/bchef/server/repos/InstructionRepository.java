package eus.healthit.bchef.server.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class InstructionRepository {

	public static List<JSONObject> getByUuid(String uuid) throws SQLException {
		String query = "SELECT * FROM public.rel_instructions INNER JOIN public.instructions ON (rel_instructions.id_instruction = instructions.id)"
				+ "WHERE rel_instructions.uuid_recipe = '" + uuid + "'";
		ResultSet rSet = QueryCon.executeQuery(query);
		return parseInstructionList(rSet);
	}

	private static JSONObject parseInstruction(ResultSet rset) throws JSONException, SQLException {
		JSONObject instruction = new JSONObject();
		instruction.put("id", rset.getInt("id")).put("action", rset.getString("action"))
				.put("value", rset.getInt("value")).put("img", ImageRepository.encodeImage(rset.getString("img")))
				.put("num", rset.getInt("num")).put("duration", rset.getTime("duration").toString());

		return instruction;
	}

	private static List<JSONObject> parseInstructionList(ResultSet rSet) throws SQLException {
		List<JSONObject> list = new ArrayList<>();
		while (rSet.next()) {
			list.add(parseInstruction(rSet));
		}
		return list;
	}

	public static int insertInstruction(JSONObject instruction, String recipeUUID) throws JSONException, SQLException {
		String action = instruction.getString("action");
		int value = instruction.getInt("value");
		String image = ImageRepository.saveImage(instruction.getString("img"));
		String text = instruction.getString("text");
		int num = instruction.getInt("num");
		String duration = instruction.getString("duration");
		String query = "INSERT INTO public.instructions (action, value, img, txt, num, duration) "
				+ String.format("VALUES ('%s', %d, '%s', '%s', %d, '%s')", action, value, image, text, num, duration)
				+ " RETURNING instructions.id";
		return QueryCon.executeQuery(query).getInt("id");
	}

	public static void makeRelation(int idIns, String uuidRecipe) throws SQLException {
		String query = String.format("INSERT INTO public.rel_instructions VALUES ('%s', %d )", uuidRecipe, idIns);
		QueryCon.execute(query);
	}

}
