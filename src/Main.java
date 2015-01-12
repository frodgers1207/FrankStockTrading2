import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Scanner;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class Main
{
	public static void main(String[] args) throws ParseException, SQLException
	{
		java.sql.Connection connect = null;
		Statement statement = null;
		java.sql.PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		Scanner input = new Scanner(System.in);
		getStockData getStockDataObject = new getStockData();
		BuyAndSell BuyAndSellObject = new BuyAndSell();
		SetBuyThreshold SetBuyThresholdObject = new SetBuyThreshold();
		String StartUp = "1";
		SetBuyThreshold.SetBuyThreshold();
		
		double CashBalance = 0;
		double AssetsBalance = 0;
		
		try //connect to database
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://mysql.frankrodgers.com", "wluadmissions", "g3n3r@ls");
		}
		catch(Exception ex)
		{
			System.out.println("Error: "+ex);
		}
		
		preparedStatement = connect
		          .prepareStatement("SELECT CashBalance, AssetsBalance FROM frankstocktrading.tradingaccount "
		          		+ " WHERE AccountID=?");
		 		   preparedStatement.setInt(1, 1);
		 		   resultSet = preparedStatement.executeQuery();
		 		   while(resultSet.next())
		 		   {
		 			   CashBalance = resultSet.getDouble("CashBalance");
		 			   AssetsBalance = resultSet.getDouble("AssetsBalance");
		 		   }
		 
		System.out.println("Starting the day with a Cash Balance of: "
				+ "["+NumberFormat.getCurrencyInstance().format(CashBalance)+"]"
					 +" and an Assets Balance of: ["+NumberFormat.getCurrencyInstance().format(AssetsBalance)+"]");
		
		connect.close();
	 	resultSet.close();
	 	preparedStatement.close();
 		   
		System.out.print("Enter 0 at 9:00AM or after to start the program properly: ");
		StartUp = input.nextLine();
		while(StartUp.equals("0")) //start running at 9:00AM on trading days, 
		{                          //end process between 3:50PM and 4:00PM
		  getStockData.getStockData();
		  BuyAndSell.BuyAndSell();
		  
		  try {
			    Thread.sleep(10000);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
}
