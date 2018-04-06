
public class User {
	
	private String name;
	private String id;
	private Game currentGame;
	
	public User(String name) {
		this.name = name;
		this.id = User.generateId();
	}
	
	public static String generateId() {
		return "asdf";
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public Game getGame() {
		return currentGame;
	}
}
