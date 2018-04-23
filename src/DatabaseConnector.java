import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DatabaseConnector {
	
	private static Connection getRemoteConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load the jdbc driver");
			e.printStackTrace();
		}
		
		if (System.getProperty("RDS_HOSTNAME") != null) {
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
			try {
				
				// I'd rather avoid revealing all the login details of the database
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
	
	public static int addUser(String user, String pass) {
		// TODO: hash password
		
		Connection conn = null;
		try {
			conn = getRemoteConnection();
			
			// TODO: Prevent SQL Injection and check if user exists first
			Statement setup = conn.createStatement();
			setup.addBatch("INSERT INTO users (username, password) VALUES ('" + user + "', '" + pass + "')");
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
