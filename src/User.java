import java.util.HashMap;

import javax.websocket.Session;

import org.json.simple.JSONObject;

public class User {
	
	/**
	 * HashMap of User id's to User object to keep track of all clients
	 */
	private static HashMap<String, User> users = new HashMap<String, User>();
	
	private String name;
	private String id;
	private Session session;
	private Game currentGame;
	private String stage;
	
	public User(Session session) {
		this.id = Utils.generateKey();
		this.session = session;
		this.stage = "init";
		
		User.add(this);
		
		System.out.println("User generated with id " + this.id);
	}
	
	/**
	 * Send a JSON message to the user
	 * @param message the JSONObject representing the message to be sent
	 * @return 0 if success, 1 if something went wrong.
	 */
	public int send(JSONObject message) {
		try {
			session.getAsyncRemote().sendText(message.toString());
		} catch (Exception e) {
			User.remove(this.getId());
			e.printStackTrace();
			
			return 1;
		}
		
		return 0;
	}
	
	public String getName() {
		return name;
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
	
	public String getStage() {
		return stage;
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
