import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RequestHandler {

	/**
	 * Takes in a message from the client and handles the request
	 * 
	 * @param message a JSON Object in the form of a string
	 * @return 0 indicating a success and anything else meaning an error
	 */
	public static int handleRequest(String message, Session session) {
		// Parse string into JSONObject
		JSONObject request;
		try {
			request = (JSONObject) new JSONParser().parse(message);
		} catch (ParseException e) {
			System.out.println("Failed to parse message: " + message);
			e.printStackTrace();
			return 1;
		}
		
		User user;
		
		// Check if id was given yet
		if (request.get("id") == null || User.getUsers().get(request.get("id")) == null) {
			// TODO: Notification for unauthorized user
			
			// New user connected
			user = new User(session);
		} else {
			user = User.getUsers().get(request.get("id"));
			user.setSession(session);
		}
		
		// Check request type
		switch ((String) request.get("type")) {
		case "getPage":
			return getPage(request, user);
		case "createRoom":
			if (user.getStage() != "init")
				return 1;
			
			user.setStage("createRoom");
			return getPage(request, user);
		default:
			System.out.println("Unknown request type: " + request.get("type"));
			return 1;
		}
	}
	
	/**
	 * Sends a "card" to the client
	 * 
	 * @param request JSONObject representing the request made
	 * @param user The user requesting the page
	 * @return 0 if successful, 1 if not
	 */
	private static int getPage(JSONObject request, User user) {
		JSONObject response = new JSONObject();
		response.put("id", user.getId());
		response.put("type", "newPage");
		
		switch (user.getStage()) {
		case "init":
			response.put("body", Utils.getFileContents("start.html"));
			user.send(response);
			break;
		case "createRoom":
			response.put("body", Utils.getFileContents("createRoom.html"));
			user.send(response);
			break;
		default:
			System.out.println("Requested page for stage " + user.getStage() + " but no page was specified for the stage...");
		}
		
		return 0;
	}
	
}
