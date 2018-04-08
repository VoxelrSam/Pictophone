import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class that contains useful functions that didn't really fit in any one place
 * 
 * @author Samuel Ingram
 */
public class Utils {
	
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
	 * Puts the entire contents of a file into a string (for sending "cards" mainly)
	 * 
	 * @param fileName Name of the file to be sent
	 * @return 0 if successful, 1 if not
	 */
	public static String getFileContents(String fileName) {
		//System.out.println(System.getProperty("user.dir"));
		
		try {
			byte[] data = Files.readAllBytes(Paths.get("C:\\Users\\Sam\\Documents\\GitHub\\Pictophone\\" + fileName));
			return new String(data, Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("File not found: " + fileName);
			e.printStackTrace();
			
			return "";
		}
	}
}
