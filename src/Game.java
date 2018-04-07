import java.util.ArrayList;

public class Game {
	
	private ArrayList<User> users;
	private User owner;
	private int size;
	private String key;
	
	public Game(User owner, int size) {
		this.owner = owner;
		this.size = size;
		this.key = Utils.generateKey();
		
		System.out.println("Game generated by " + owner.getName() + " of size " + size + " with id " + this.key);
	}

	public ArrayList<User> getUsers(){
		return users;
	}
	
	public String getKey() {
		return key;
	}
}
