import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.simple.JSONObject;

/**
 * Class used to represent Games on the server
 * Handles all the game logic and keeping track of users
 * 
 * @author Samuel Ingram
 *
 */
public class Game {
	
	/**
	 * HashMap of Game id's to Game objects to keep track of all games
	 */
	private static HashMap<String, Game> games = new HashMap<>();
	
	/**
	 * An ArrayList that holds the different pieces of data sent from the clients
	 * corresponding to each step in the game.
	 * i.e. holds both prompt strings and drawing data
	 */
	private ArrayList<String> timeline;
	
	private ArrayList<User> users;
	private User owner;
	private int size;
	private String name;
	private String key;
	private String stage;
	private int promptCounter;
	
	public Game(User owner, String name, int size) {
		this.owner = owner;
		this.name = name;
		this.size = size;
		this.key = Utils.generateKey();
		this.stage = "filling";
		this.promptCounter = 0;
		
		games.put(this.key, this);
		
		users = new ArrayList<>();
		users.add(owner);
		owner.setGame(this);
		
		timeline = new ArrayList<>();
		
		System.out.println("Game generated by " + owner.getName() + " of size " + size + " with id " + this.key);
	}
	
	/**
	 * Starts the game
	 */
	public void start() {
		this.stage = "playing";
		System.out.println("Game " + this.getKey() + " started!");
		
		// Randomize Order
		Collections.shuffle(users);
		
		// Set initial pages to send
		users.get(0).setStage("prompting");
		users.get(0).setPageUpdated(true);
		for (int i = 1; i < users.size(); i++) {
			if (!users.get(i).getStage().equals("waiting")) {
				users.get(i).setStage("waiting");
				users.get(i).setPageUpdated(true);
			}
		}
		
		// Send initial pages
		pushPages();
	}
	
	/**
	 * Ends the game. Normally called by the next() function
	 */
	private void end() {
		for (int i = 0; i < users.size(); i++) {
			users.get(i).setStage("end");
			users.get(i).setPageUpdated(true);
		}
		
		pushPages();
	}
	
	/**
	 * Advances the game to the next stage
	 * i.e. prompts the next user
	 */
	private void next() {
		if (promptCounter + 1 == size) {
			end();
			return;
		}
		
		users.get(promptCounter).setStage("done");
		users.get(promptCounter).setPageUpdated(true);
		
		if ((++promptCounter) % 2 == 0)
			users.get(promptCounter).setStage("describing");
		else
			users.get(promptCounter).setStage("drawing");
		
		users.get(promptCounter).setPageUpdated(true);
		
		pushPages();
	}
	
	/**
	 * Handles game specific requests
	 * 
	 * @param request The request to be handled
	 * @param user The user to whom the request belongs
	 * @return 0 if success, 1 if otherwise
	 */
	public int handle(JSONObject request, User user) {
		switch ((String) request.get("type")) {
		case "getPage":
			
			return Utils.getPage(user);
		case "submitPrompt":
			timeline.add((String) request.get("prompt"));
			
			next();
			break;
		case "submitDrawing":
			timeline.add("*Drawing*");
			
			next();
			break;
		default:
			System.out.println("No Game case for request of type " + request.get("type"));
			return 1;
		}
		return 0;
	}
	
	/**
	 * Pushes updated pages to the users who are in a new stage
	 */
	private void pushPages() {
		for (User user : users) {
			if (user.pageUpdated()) {
				// Find page and send
				Utils.getPage(user);
				
				user.setPageUpdated(false);
			}
		}
	}
	
	/**
	 * Sends a message to all the users in the game
	 * 
	 * @param message the JSONObject representing the message to be sent
	 */
	public void sendToAll(JSONObject message) {
		for (User user : users) {
			user.send(message);
		}
	}
	
	/**
	 * Adds a user to the game, and sets the user's current game to this one
	 * Also starts game if the max amount of players join
	 * 
	 * @param u The user to be added
	 * @return 0 if success, 1 if otherwise
	 */
	public int addUser(User u) {
		if (users.size() == size || !stage.equals("filling"))
			return 1;
		
		users.add(u);
		u.setGame(this);
		
		if (users.size() == size)
			this.start();
		
		return 0;
	}

	public ArrayList<User> getUsers(){
		return users;
	}
	
	public ArrayList<String> getTimeline(){
		return timeline;
	}
	
	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getStage() {
		return stage;
	}
	
	public String getLastSubmission() {
		return timeline.get(promptCounter - 1);
	}
	
	public static Game getGame(String key) {
		return games.get(key);
	}
}
