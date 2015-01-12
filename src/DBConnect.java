import java.sql.*;

public class DBConnect
{
	private Connection con;
	private Statement st;
	private ResultSet rs;
	
	public DBConnect()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			con = DriverManager.getConnection("jdbc:mysql://django.amazigh-inc.com", "root", "Nimby9!1");
			System.out.println("Connected to the Database.");
		}
		catch(Exception ex)
		{
			System.out.println("Error: "+ex);
		}
	}
}