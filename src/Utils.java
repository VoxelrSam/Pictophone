import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.simple.JSONObject;

/**
 * A class that contains useful functions that didn't really fit in any one place
 * 
 * @author Samuel Ingram
 */
public class Utils {
	
	/**
	 * Sends a "card" to the client based on their current stage
	 * 
	 * @param user The user requesting the page
	 * @return 0 if successful, 1 if not
	 */
	public static int getPage(User user) {
		JSONObject response = new JSONObject();
		response.put("type", "newPage");
		
		String fileName;
		switch (user.getStage()) {
		case "init":
			fileName = "start.html";
			break;
		case "createRoomForm":
			fileName = "createRoom.html";
			break;
		case "ownerWait":
			fileName = "ownerWait.html";
			response.put("key", user.getGame().getKey());
			break;
		case "joinRoom":
			fileName = "joinRoom.html";
			break;
		case "waiting":
			fileName = "waiting.html";
			break;
		case "prompting":
			fileName = "prompt.html";
			break;
		case "describing":
			fileName = "describe.html";
			response.put("image", user.getGame().getLastSubmission());
			break;
		case "drawing":
			fileName = "draw.html";
			response.put("prompt", user.getGame().getLastSubmission());
			break;
		case "done":
			fileName = "waiting.html";
			break;
		case "end":
			fileName = "end.html";
			response.put("timeline", user.getGame().getTimelineString());
			response.put("users", user.getGame().getUsersString());
			break;
		default:
			System.out.println("Requested page for stage " + user.getStage() + " but no page was specified for the stage...");
			fileName = "start.html";
		}
		
		response.put("body", Utils.getFileContents(fileName));
		user.send(response);
		
		return 0;
	}
	
	/**
	 * Generate a key of 5 random numbers to use for authentication
	 * 
	 * @return the key generated
	 */
	public static String generateKey() {
		String key = "";
		
		for (int i = 0; i < 5; i++) {
			key += (int) (Math.random() * 10);
		}
		
		return key;
	}
	
	/**
	 * Generate a key consisting of an adjective and noun to use for authentication
	 * 
	 * @return the key generated
	 */
	public static String generateWordKey() {
		// TODO: Change to a better path
		String path = "F:\\GitHub\\Pictophone\\";
		ArrayList<String> nouns = new ArrayList<>();
		ArrayList<String> adjectives = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(path + "nouns.txt"))){
			String line;
			while ((line = br.readLine()) != null) {
				nouns.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader(path + "adjectives.txt"))){
			String line;
			while ((line = br.readLine()) != null) {
				adjectives.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return adjectives.get((int) (Math.random() * adjectives.size())) + " " + nouns.get((int) (Math.random() * nouns.size()));
	}
	
	/**
	 * Puts the entire contents of a file into a string (for sending "cards" mainly)
	 * 
	 * @param fileName Name of the file to be sent
	 * @return 0 if successful, 1 if not
	 */
	public static String getFileContents(String fileName) {
		try {
			// TODO: Change to a better path
			byte[] data = Files.readAllBytes(Paths.get("F:\\GitHub\\Pictophone\\" + fileName));
			return new String(data, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("File not found: " + fileName);
			e.printStackTrace();
			
			return "";
		}
	}
}
