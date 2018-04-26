import java.util.ArrayList;
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
	private static HashMap<String, User> sessionIdsToUsers = new HashMap<String, User>();
	
	private static ArrayList<User> joiningUsers = new ArrayList<>();
	
	private String name;
	private String id;
	private Session session;
	private Game currentGame;
	private String stage;
	private boolean pageUpdated;
	private boolean isLoggedIn;
	private int gamesPlayed;
	private String nameColor;
	private String defaultColor;
	
	private Thread destroyer;
	
	public User(Session session) {
		this.id = Utils.generateKey();
		this.session = session;
		this.stage = "init";
		this.pageUpdated = false;
		this.isLoggedIn = false;
		this.gamesPlayed = 0;
		this.nameColor = "#000000";
		this.defaultColor = "0000ff";
		
		// Set buffer size so we can actually send files
		this.session.setMaxTextMessageBufferSize(524288);
		
		sessionIdsToUsers.put(session.getId(), this);
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
		message.put("nameColor", this.getNameColor());
		message.put("stage", this.getStage());
		message.put("isLoggedIn", this.isLoggedIn);
		if (this.getGame() != null) {
			message.put("roomName", this.getGame().getName());
			message.put("gameKey", this.getGame().getKey());
		}
		
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (Exception e) {
			User.remove(this);
			e.printStackTrace();
			
			return 1;
		}
		return 0;
	}
	
	/**
	 * Send a warning message to the user to be displayed
	 * 
	 * @param message The message to send
	 */
	public void warn(String message) {
		JSONObject warning = new JSONObject();
		warning.put("type", "warning");
		warning.put("message", message);
		
		this.send(warning);
	}
	
	/**
	 * Try to sign up the user
	 * 
	 * @param name The username to be used. Must be unique
	 * @param pass The password to be used
	 * @return true if successful, false if not
	 */
	public boolean signup(String name, String pass) {
		if (name.length() == 0){
			warn("Please specify a username");
			return false;
		}
		
		if (name.length() > 16){
			warn("Please keep usernames under 16 characters in length");
			return false;
		}
		
		if (pass.length() == 0){
			warn("Please specify a password");
			return false;
		}
		
		if (DatabaseConnector.addUser(this, name, pass) != 0) {
			return false;
		}
		
		this.setStage("init");
		this.setName(name);
		this.isLoggedIn = true;
		
		return true;
	}
	
	/**
	 * Try to login the user
	 * 
	 * @param name The username to be checked
	 * @param pass The password to be checked
	 * @return true if successful, false if not
	 */
	public boolean login(String name, String pass) {
		if (DatabaseConnector.verifyUser(this, name, pass) != 0) {
			return false;
		}
		
		this.setStage("init");
		this.setName(name);
		this.isLoggedIn = true;
		
		return true;
	}
	
	/**
	 * Logs out a user and sets all the parameters to the default
	 */
	public void logout() {
		if (this.getStage().equals("joinRoom"))
			User.removeFromJoining(this);
		
		this.setStage("init");
		this.setName(null);
		this.isLoggedIn = false;
		this.gamesPlayed = 0;
		this.nameColor = "#000000";
		this.defaultColor = "#0000ff";
	}
	
	/**
	 * Remove all references to this object so that it will be picked up by garbage collection
	 */
	public void destroy() {
		User.remove(this);
		
		if (this.currentGame != null)
			this.getGame().removeUser(this);
		
		if (joiningUsers.contains(this))
			joiningUsers.remove(this);
		
		System.out.println("Destroyed user " + this.getId());
	}
	
	/**
	 * Remove the user from the current game and go to main page
	 */
	public void leaveGame() {
		this.getGame().removeUser(this);
		this.setStage("init");
	}
	
	/**
	 * Save the user to the database. Essentially an abstraction to the DatabaseConnector method.
	 * 
	 * @param info A JSONObject representing the info to be saved
	 * @return true if successful
	 */
	public boolean save(JSONObject info) {
		return DatabaseConnector.editUser(this, info);
	}
	
	/**
	 * Get the user info for displaying on the edit user page
	 * 
	 * @return A string representing the info in a JSON format
	 */
	public String getInfo() {
		JSONObject info = new JSONObject();
		info.put("gamesPlayed", this.gamesPlayed);
		info.put("defaultColor", this.defaultColor);
		
		return info.toString(); 
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
	
	public String getSessionId() {
		return this.session.getId();
	}
	
	public void setSession(Session session) {
		sessionIdsToUsers.remove(this.session.getId());
		sessionIdsToUsers.put(session.getId(), this);
		
		this.session = session;
		this.session.setMaxTextMessageBufferSize(524288);
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
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	public void addGamesPlayed(int games) {
		this.gamesPlayed += games;
	}
	
	public int getGamesPlayed() {
		return gamesPlayed;
	}
	
	public void setNameColor(String color) {
		this.nameColor = color;
	}
	
	public String getNameColor() {
		return nameColor;
	}
	
	public void setDefaultColor(String color) {
		this.defaultColor = color;
	}
	
	public String getDefaultColor() {
		return defaultColor;
	}
	
	public void setDestroyer(Thread destroyer) {
		this.destroyer = destroyer;
	}
	
	public Thread getDestroyer() {
		return this.destroyer;
	}
	
	public static void add(User u) {
		users.put(u.getId(), u);
	}
	
	public static void remove(User u) {
		users.remove(u.getId());
		sessionIdsToUsers.remove(u.getSessionId());
	}
	
	public static ArrayList<User> getJoiningUsers() {
		return joiningUsers;
	}
	
	public static void addToJoining(User u) {
		joiningUsers.add(u);
	}
	
	public static void removeFromJoining(User u) {
		joiningUsers.remove(u);
	}
	
	public static HashMap<String, User> getUsers(){
		return users;
	}
	
	public static User getUserFromSessionId(String id) {
		return sessionIdsToUsers.get(id);
	}
}
