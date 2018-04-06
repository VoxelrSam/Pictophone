import java.util.ArrayList;

public class Game {
	
	private ArrayList<User> users;
	private User owner;
	private int size;
	
	public Game(User owner, int size) {
		this.owner = owner;
		this.size = size;
	}

	public ArrayList<User> getUsers(){
		return users;
	}
}
