/**
 * Class used to destroy a user after a specific amount of time if not interrupted
 * Essentially frees up memory by freeing up users that the server doesn't care about anymore
 * 
 * @author Samuel Ingram
 */
public class UserDestroyer implements Runnable {
	
	private User user;
	
	public UserDestroyer(User user) {
		this.user = user;
	}
	
	/**
	 * Used by the Thread that this is passed in to
	 * Destroys the user 60 seconds after loosing connection unless interrupted by RequestHandler
	 */
	public void run() {
		try {
			Thread.sleep(60 * 1000);
			user.destroy();
		} catch (InterruptedException e) {
			return;
		}
	}
}
