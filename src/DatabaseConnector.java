import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class used to interface with the database
 * 
 * @author Samuel Ingram
 */
public class DatabaseConnector {
	
	/**
	 * Gets the connection to the database specified by the credentials
	 * 
	 * @return The connection to the database
	 */
	private static Connection getRemoteConnection() {
		
		// Load the driver
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load the jdbc driver");
			e.printStackTrace();
		}
		
		// Check if we are on AWS
		if (System.getProperty("RDS_HOSTNAME") != null) {
			// AWS
			
			try {
				String dbName = System.getProperty("RDS_DB_NAME");
				String userName = System.getProperty("RDS_USERNAME");
				String password = System.getProperty("RDS_PASSWORD");
				String hostname = System.getProperty("RDS_HOSTNAME");
				String port = System.getProperty("RDS_PORT");
				
				String jdbcUrl = "jdbc:mysql://" 
								+ hostname 
								+ ":" 
								+ port 
								+ "/" 
								+ dbName 
								+ "?user=" 
								+ userName 
								+ "&password=" 
								+ password;
				
				Connection con = DriverManager.getConnection(jdbcUrl);
				
				return con;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			// Not AWS
			
			try {
				
				// I'd rather avoid revealing all the login details of the database
				// To used another database, put an auth.json file in the WEB-INF/lib directory with the correct info
				JSONObject auth;
				try {
					auth = (JSONObject) (new JSONParser()).parse(Utils.getFileContents("auth.json"));
				} catch (ParseException e) {
					System.err.println("Could not parse the auth file!");
					e.printStackTrace();
					return null;
				}
				
				String jdbcUrl = "jdbc:mysql://" 
									+ auth.get("hostname") 
									+ ":" 
									+ auth.get("port") 
									+ "/" 
									+ auth.get("dbName") 
									+ "?user=" 
									+ auth.get("userName") 
									+ "&password=" 
									+ auth.get("password");
				
				Connection con = DriverManager.getConnection(jdbcUrl);
				
				return con;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * Add a user to the database
	 * 
	 * @param user The User object used for sending warning messages
	 * @param name The name of the user to be created
	 * @param pass The password of the user to be created
	 * @return 0 if successful, anything else if not
	 */
	public static int addUser(User user, String name, String pass) {
		// hash password
		pass = BCrypt.hashpw(pass, BCrypt.gensalt());
		
		Connection conn = null;
		try {
			conn = getRemoteConnection();
			
			// Check if user exists
			if (userExists(name)) {
				user.warn("User already exists...");
				conn.close();
				return 2;
			}
			
			// TODO: Prevent SQL Injection
			// Insert the new user
			Statement setup = conn.createStatement();
			setup.addBatch("INSERT INTO users (username, password) VALUES ('" + name + "', '" + pass + "')");
			setup.executeBatch();
			setup.close();
			
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			
			System.out.println("Closing the connection.");
		    if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		    
		    return 1;
		}
		
		System.out.println("Closing the connection.");
		if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		
		return 0;
	}
	
	/**
	 * Verify the specified username and pass in the database
	 * 
	 * @param user The User object used for sending warning messages
	 * @param name The name to verify
	 * @param pass The password to verify
	 * @return 0 if successful, anything else if not
	 */
	public static int verifyUser(User user, String name, String pass) {
		
		Connection conn = null;
		try {
			conn = getRemoteConnection();
			
			// Build query to get user
			String query = "SELECT * FROM users WHERE username = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			
			statement.setString(1, name);
			try(ResultSet resultSet = statement.executeQuery()){
				
				// if user does not exist
				if (!resultSet.first()) {
					user.warn("User does not exist...");
					resultSet.close();
					statement.close();
					conn.close();
					return 2;
				}
				
				// if the password is incorrect
				if (!BCrypt.checkpw(pass, resultSet.getString("password"))) {
					user.warn("Password is incorrect...");
					resultSet.close();
					statement.close();
					conn.close();
					return 3;
				}
				
				user.addGamesPlayed(resultSet.getInt("games_played"));
				user.setNameColor("#" + resultSet.getString("name_color"));
				user.setDefaultColor("#" + resultSet.getString("default_color"));
				
				resultSet.close();
			}
			
			statement.close();
			conn.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			
			System.out.println("Closing the connection.");
		    if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		    
		    return 1;
		}
		
		return 0;
	}
	
	public static boolean editUser(User user, JSONObject info) {
		String prevName = user.getName();
		if (info.get("username") != null) {
			
			// Check to be sure it's a new name
			if (userExists((String) info.get("username"))) {
				user.warn("A user by this name already exists!");
				return false;
			}
			
			user.setName((String) info.get("username"));
		}
		
		String password = null;
		if (info.get("password") != null) {
			password = (String) info.get("password");
			
			// hash password
			password = BCrypt.hashpw(password, BCrypt.gensalt());
		}
		
		if (info.get("nameColor") != null) {
			user.setNameColor((String) info.get("nameColor"));
		}
		
		if (info.get("defaultColor") != null) {
			user.setDefaultColor((String) info.get("defaultColor"));
		}
		
		// Save user info to the database
		Connection conn = null;
		PreparedStatement setup = null;
		try {
			conn = getRemoteConnection();
			
			String optional = "";
			if (password != null) {
				optional = "password = ?, ";
			}
			
			String command = "UPDATE users SET username = ?, " 
								+ optional
								+ "name_color = ?, default_color = ? WHERE username = ?";
			setup = conn.prepareStatement(command);
			setup.setString(1, user.getName());
			
			if (password == null) {
				setup.setString(2, user.getNameColor().substring(1, user.getNameColor().length()));
				setup.setString(3, user.getDefaultColor().substring(1, user.getDefaultColor().length()));
				setup.setString(4, prevName);
			} else {
				setup.setString(2, password);
				setup.setString(3, user.getNameColor().substring(1, user.getNameColor().length()));
				setup.setString(4, user.getDefaultColor().substring(1, user.getDefaultColor().length()));
				setup.setString(5, prevName);
			}
			
			
			setup.executeUpdate();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			System.out.println("Closing the connection.");
			if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
			if (setup != null) try { setup.close(); } catch (SQLException ignore) {}
		}
		
		return true;
	}
	
	private static boolean userExists(String username) {
		Connection conn = getRemoteConnection();
		String query = "SELECT * FROM users WHERE username = ?";
		try (PreparedStatement statement = conn.prepareStatement(query)){
			statement.setString(1, username);
			try(ResultSet resultSet = statement.executeQuery()){
				if (resultSet.next()) {
					// User already exists
					statement.close();
					resultSet.close();
					conn.close();
					return true;
				}
				resultSet.close();
			}
			statement.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			
			System.out.println("Closing the connection.");
		    if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		    
		    return true;
		}
		
		return false;
	}
}
