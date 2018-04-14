import java.util.HashMap;

import javax.websocket.Session;

import org.json.simple.JSONObject;

/**
 * Class used to represent users on the server
 * In the code, Users are most often distinguished by their id
 * 
 * @author Sam
 */
public class User {
	
	/**
	 * HashMap of User id's to User objects to keep track of all clients
	 */
	private static HashMap<String, User> users = new HashMap<String, User>();
	
	private String name;
	private String id;
	private Session session;
	private Game currentGame;
	private String stage;
	private boolean pageUpdated;
	
	public User(Session session) {
		this.id = Utils.generateKey();
		this.session = session;
		this.stage = "init";
		this.pageUpdated = false;
		
		User.add(this);
		
		System.out.println("User generated with id " + this.id);
	}
	
	/**
	 * Send a JSON message to the user
	 * 
	 * @param message the JSONObject representing the message to be sent
	 * @return 0 if success, 1 if something went wrong.
	 */
	public int send(JSONObject message) {
		message.put("id", this.getId());
		message.put("name", this.getName());
		if (this.getGame() != null) {
			message.put("roomName", this.getGame().getName());
			message.put("gameKey", this.getGame().getKey());
		}
		
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (Exception e) {
			User.remove(this.getId());
			e.printStackTrace();
			
			return 1;
		}
		return 0;
	}
	
	/**
	 * Remove the user from the current game and go to main page
	 */
	public void leaveGame() {
		this.getGame().removeUser(this);
		this.setStage("init");
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Game getGame() {
		return currentGame;
	}
	
	public void setGame(Game g) {
		this.currentGame = g;
	}
	
	public String getStage() {
		return stage;
	}
	
	public void setStage(String stage) {
		this.stage = stage;
	}
	
	public void setPageUpdated(boolean b) {
		this.pageUpdated = b;
	}
	
	public boolean pageUpdated() {
		return pageUpdated;
	}
	
	public static void add(User u) {
		users.put(u.getId(), u);
	}
	
	public static HashMap<String, User> getUsers(){
		return users;
	}
	
	public static void remove(String id) {
		users.remove(id);
	}
}
