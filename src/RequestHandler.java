import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class used to handle all of the request logic
 * Designed to be used statically; no RequestHandler objects are created
 * 
 * @author Samuel Ingram
 */
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
		
		// If game id is specified, send request to game request handler
		Game g;
		if (request.get("gameKey") != null 
				&& (g = Game.getGame((String) request.get("gameKey"))) != null
				&& g.getUsers().contains(user)) {
			return g.handle(request, user);
		}
		
		// Check request type
		switch ((String) request.get("type")) {
		case "getPage":
			break;
		case "createRoomForm":
			if (user.getStage() != "init")
				return 1;
			
			user.setStage("createRoomForm");
			break;
		case "createRoom":
			if (user.getStage() != "createRoomForm")
				return 1;
			
			User.getUsers().get(request.get("id")).setName((String) request.get("username"));
			new Game(User.getUsers().get(request.get("id")), (String) request.get("roomname"), Integer.parseInt((String) request.get("roomsize")));
			
			user.setStage("ownerWait");
			break;
		case "joinRoomForm":
			if (user.getStage() != "init")
				return 1;
			
			user.setStage("joinRoom");
			break;
		case "joinRoom":
			if (user.getStage() != "joinRoom")
				return 1;
			
			g = Game.getGame((String) request.get("roomkey"));
			if (g == null) {
				// Game does not exist
				JSONObject response = new JSONObject();
				response.put("type", "roomNotFound");
				user.send(response);
				return 1;
			}
			
			if (g.addUser(user) != 0) {
				// Could not add user
				JSONObject response = new JSONObject();
				response.put("type", "roomNotOpen");
				user.send(response);
				return 1;
			}
			
			user.setName((String) request.get("username"));
			
			// Avoid double page send
			if (g.getStage().equals("playing"))
				return 0;
			
			user.setStage("waiting");
			
			break;
		default:
			System.out.println("Unknown request type: " + request.get("type"));
			return 1;
		}
		
		return Utils.getPage(user);
	}
	
}
