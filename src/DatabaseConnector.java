import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
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
			String query = "SELECT * FROM users WHERE username = ?";
			try (PreparedStatement statement = conn.prepareStatement(query)){
				statement.setString(1, name);
				try(ResultSet resultSet = statement.executeQuery()){
					if (resultSet.next()) {
						// User already exists
						user.warn("User already exists...");
						resultSet.close();
						conn.close();
						return 2;
					}
					resultSet.close();
				}
				statement.close();
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
	
	/**
	 * TEST CODE, WILL REMOVE
	 */
	public static void createTable() {
		  Connection conn = null;
		  Statement setupStatement = null;
		  Statement readStatement = null;
		  ResultSet resultSet = null;
		  String results = "";
		  int numresults = 0;
		  String statement = null;

		  try {
		    // Create connection to RDS DB instance
		    conn = getRemoteConnection();
		    
		    // Create a table and write two rows
		    setupStatement = conn.createStatement();
		    String createTable = "CREATE TABLE Beanstalk (Resource char(50));";
		    String insertRow1 = "INSERT INTO Beanstalk (Resource) VALUES ('EC2 Instance');";
		    String insertRow2 = "INSERT INTO Beanstalk (Resource) VALUES ('RDS Instance');";
		    
		    setupStatement.addBatch(createTable);
		    setupStatement.addBatch(insertRow1);
		    setupStatement.addBatch(insertRow2);
		    setupStatement.executeBatch();
		    setupStatement.close();
		    
		  } catch (SQLException ex) {
		    // Handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		  } finally {
		    System.out.println("Closing the connection.");
		    if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		  }

		  try {
		    conn = getRemoteConnection();
		    
		    readStatement = conn.createStatement();
		    resultSet = readStatement.executeQuery("SELECT Resource FROM Beanstalk;");

		    resultSet.first();
		    results = resultSet.getString("Resource");
		    resultSet.next();
		    results += ", " + resultSet.getString("Resource");
		    
		    resultSet.close();
		    readStatement.close();
		    conn.close();

		  } catch (SQLException ex) {
		    // Handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		  } finally {
		       System.out.println("Closing the connection.");
		      if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		  }
	}
}
