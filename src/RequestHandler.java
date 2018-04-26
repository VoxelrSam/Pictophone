import java.io.IOException;

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
		
		if (request.get("type").equals("ping")) {
			try {
				session.getBasicRemote().sendText("{\"type\": \"pong\"}");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return 0;
		}
		
		User user;
		
		// Check if id was given yet
		if (request.get("id") == null || User.getUsers().get(request.get("id")) == null) {
			// New user connected
			user = new User(session);
		} else {
			user = User.getUsers().get(request.get("id"));
			user.setSession(session);
		}
		
		// Stop the UserDestroyer if necessary
		if (user.getDestroyer() != null) {
			user.getDestroyer().interrupt();
			user.setDestroyer(null);
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
		case "exit":
			if (user.getStage().equals("joinRoom"))
				User.removeFromJoining(user);
			
			user.setStage("init");
			break;
		case "rules":
			user.setStage("rules");
			break;
		case "loginForm":
			user.setStage("login");
			break;
		case "signup":
			if (!user.signup((String) request.get("username"), (String) request.get("password"))) {
				return 1;
			}
			break;
		case "login":
			if (!user.login((String) request.get("username"), (String) request.get("password"))) {
				return 1;
			}
			break;
		case "logout":
			user.logout();
			break;
		case "editUser":
			user.setStage("editUser");
			break;
		case "saveUser":
			if (!user.save(request))
				return 1;
			
			user.setStage("init");
			break;
		case "createRoomForm":
			if (user.getStage() != "init")
				return 1;
			
			user.setStage("createRoomForm");
			break;
		case "createRoom":
			if (user.getStage() != "createRoomForm")
				return 1;
			
			if (((String) request.get("username")).length() == 0) {
				user.warn("Please specify a username");
			} else if (((String) request.get("username")).length() > 16) {
				user.warn("Please keep usernames under 16 characters in length");
			}
			
			user.setName((String) request.get("username"));
			boolean isPublic = ((String) request.get("roomtype")).equals("Public");
			new Game(user, (String) request.get("roomname"), Integer.parseInt((String) request.get("roomsize")), isPublic);
			
			user.setStage("ownerWait");
			break;
		case "joinRoomForm":
			if (user.getStage() != "init")
				return 1;
			
			user.setStage("joinRoom");
			
			User.addToJoining(user);
			break;
		case "joinRoom":
			if (user.getStage() != "joinRoom")
				return 1;
			
			g = Game.getGame((String) request.get("roomkey"));
			if (g == null) {
				// Game does not exist
				user.warn("Room not found with that key. Try another.");
				return 1;
			}
			
			if (((String) request.get("username")).length() == 0) {
				user.warn("Please specify a username");
			} else if (((String) request.get("username")).length() > 16) {
				user.warn("Please keep usernames under 16 characters in length");
			}
			
			user.setName((String) request.get("username"));
			
			if (g.addUser(user) != 0) {
				// Could not add user
				user.warn("This room is not accepting users at the moment. Try another.");
				return 1;
			}
			
			if (user.getStage().equals("joinRoom"))
				User.removeFromJoining(user);
			
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
