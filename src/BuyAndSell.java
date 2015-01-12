import java.*;
import java.awt.List;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.InputStream;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

import java.util.Date;
import java.util.Calendar;
import java.sql.*;

public class BuyAndSell
{
	public static void BuyAndSell() throws SQLException
	{
	    DecimalFormat f = new DecimalFormat("##.00");
	     //System.out.println(f.format(d));
		ArrayList<String> BuyAndSellTicker = new ArrayList<>();
		java.sql.Connection connect = null;
		Statement statement = null;
		java.sql.PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		double currentposvalue = 0;
		
		//get pricing for buying and selling
		double currentpricebuy = 0;
		double currentpricesell = 0;
		
		//variables for retrieving data from BuyAndSellList
		String tIDBSL = null;
		int NumOfSharesBSL = 0;
		double BuyThresholdBSL = 0;
		String TradeFlag = null;
		
		//variables for retrieving data from Position
		int aIDP = 1;
		String tIDP = null;
		int Shares = 0;
		double PurchasePrice = 0;
		double AvPrPerShare = 0;
		double TotalPositionCost = 0;
		double SalePrice = 0;
		double Profit = 0;
		
		//variables for retrieving data from TradingAccount
		int aIDTA = 1;
		String nameTA = null;
		double CashBalance = 0;
		double TradeFeeBuy = 0;
		double TradeFeeSell = 0;
		double AssetsBalance = 0;
		
		try //connect to database
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			connect = DriverManager.getConnection("jdbc:mysql://mysql.frankrodgers.com", "wluadmissions", "g3n3r@ls");
			//System.out.println("Connected to the Database.");
		}
		catch(Exception ex)
		{
			System.out.println("Error: "+ex);
		} //finish connect to database

		 preparedStatement = connect
		          .prepareStatement("SELECT TickerID FROM frankstocktrading.buyandselllist");
		      resultSet = preparedStatement.executeQuery();
		      writeResultSet(resultSet, BuyAndSellTicker);
		      
		 for(int n=0; n < BuyAndSellTicker.size(); n++)
		 {
			 preparedStatement = connect
					 .prepareStatement("SELECT LastBidPrice FROM stockquotes.pricing"
					 		+ " WHERE TickerID=?");
			 		  preparedStatement.setString(1, BuyAndSellTicker.get(n));
			 	  resultSet = preparedStatement.executeQuery();
			 	  if(resultSet.next())
			 		 currentpricesell = resultSet.getDouble("LastBidPrice");
			 
			 preparedStatement = connect
					.prepareStatement("SELECT LastAskPrice FROM stockquotes.pricing"
						 	       + " WHERE TickerID=?");
				 	 preparedStatement.setString(1, BuyAndSellTicker.get(n));
				  resultSet = preparedStatement.executeQuery();
				  if(resultSet.next())
				 	 currentpricebuy = resultSet.getDouble("LastAskPrice");
			 	  
			 preparedStatement = connect
			          .prepareStatement("SELECT * FROM frankstocktrading.buyandselllist WHERE TickerID=?");
			 		   preparedStatement.setString(1, BuyAndSellTicker.get(n));
			      resultSet = preparedStatement.executeQuery();
			      
				  while(resultSet.next())
				  {
					  tIDBSL = resultSet.getString("TickerID");
				      NumOfSharesBSL = resultSet.getInt("NumOFShares");
				      BuyThresholdBSL = resultSet.getDouble("BuyThreshold");
				      TradeFlag = resultSet.getString("TradeFlag");
				  }
			      
			      
			 preparedStatement = connect
			          .prepareStatement("SELECT * FROM frankstocktrading.position WHERE TickerID=?");
			 		   preparedStatement.setString(1, BuyAndSellTicker.get(n));
			      resultSet = preparedStatement.executeQuery();
			      while(resultSet.next())
				  {
			    	  aIDP = resultSet.getInt("AccountID");
			    	  tIDP = resultSet.getString("TickerID");;
			    	  Shares = resultSet.getInt("Shares");
			    	  PurchasePrice = resultSet.getDouble("PurchasePrice");
			    	  AvPrPerShare = resultSet.getDouble("AveragePricePerShare");
			    	  TotalPositionCost = resultSet.getDouble("TotalPositionCost");
			    	  SalePrice = resultSet.getDouble("SalePrice");
			    	  Profit = resultSet.getDouble("Profit");
				  }
			 
			preparedStatement = connect
					.prepareStatement("SELECT * FROM frankstocktrading.tradingaccount "
							+ " WHERE AccountID=?");
				preparedStatement.setInt(1, 1);
				resultSet = preparedStatement.executeQuery();
				while(resultSet.next())
				{
					aIDTA = resultSet.getInt("AccountID");
					nameTA = resultSet.getString("Name");
					CashBalance = resultSet.getDouble("CashBalance");
					TradeFeeBuy = resultSet.getDouble("TradeFeeBuy");
					TradeFeeSell = resultSet.getDouble("TradeFeeSell");
					AssetsBalance = resultSet.getDouble("AssetsBalance");
				}

			 if(TradeFlag.equals("s"))
			 {//selling
			      if(currentpricesell >= 1.15*AvPrPerShare || currentpricesell <= .95*AvPrPerShare) //very basic version of the algorithm 
			      {																                   //need to add up and down ticks
			    	  CashBalance = CashBalance + Shares*currentpricesell - TradeFeeSell;
			    	  System.out.println("Cash: "+NumberFormat.getCurrencyInstance().format(CashBalance));
			    	  SalePrice = Shares*currentpricesell - TradeFeeSell;
			    	  
			    	  NumberFormat.getCurrencyInstance().format(CashBalance);
			    	  NumberFormat.getCurrencyInstance().format(currentpricesell);
			    	  NumberFormat.getCurrencyInstance().format(SalePrice);
			    	  NumberFormat.getCurrencyInstance().format(TotalPositionCost);
			    	  
				 
			    	  preparedStatement = connect
				          .prepareStatement("UPDATE frankstocktrading.tradingaccount "
				          		+ " SET CashBalance=ROUND(?,2)"
				          		+ " WHERE AccountID=?");
				 		   preparedStatement.setDouble(1, CashBalance);
				 		   preparedStatement.setInt(2, aIDTA);
				 		   preparedStatement.executeUpdate();
				 	  
				 	  preparedStatement = connect
				 		  .prepareStatement("UPDATE frankstocktrading.position "
				 		  		+ " SET Shares=?, PurchasePrice=ROUND(?,2), AveragePricePerShare=ROUND(?,2),"
				 		  		+ " PositionDate=?, TotalPositionCost=ROUND(?,2), TotalPositionValue=ROUND(?,2), SalePrice=ROUND(?,2), Profit=ROUND(?,2)"
				 		  		+ " WHERE TickerID=?");
				 	  	   preparedStatement.setInt(1, 0);
				 	  	   preparedStatement.setDouble(2, 0);
				 	  	   preparedStatement.setDouble(3, 0);
				 	  	   Calendar calendar1 = Calendar.getInstance(); //get time stamp for MySQL
						   java.util.Date now1 = calendar1.getTime();
						   java.sql.Timestamp currentTimestampSell = new java.sql.Timestamp(now1.getTime());
				 	  	   preparedStatement.setTimestamp(4, currentTimestampSell);
				 	  	   preparedStatement.setDouble(5, 0);
				 	  	   preparedStatement.setDouble(6, 0);
				 	  	   preparedStatement.setDouble(7, Shares*currentpricesell);
				 	  	   preparedStatement.setDouble(8, SalePrice - TotalPositionCost);
				 	  	   preparedStatement.setString(9, BuyAndSellTicker.get(n));
				 	  	   preparedStatement.executeUpdate();
				 		  
				 	  preparedStatement = connect
				 			  .prepareStatement("UPDATE frankstocktrading.buyandselllist"
				 			  		+ " SET BuyThreshold=ROUND(?,2), TradeFlag=? WHERE TickerID=?");
				 	  		   preparedStatement.setDouble(1, currentpricesell*.95);
				 	  		   preparedStatement.setString(2, "b");
				 	  		   preparedStatement.setString(3, BuyAndSellTicker.get(n));
				 	  		   preparedStatement.executeUpdate();
				 	  		   
				 	  	   Calendar calendar = Calendar.getInstance(); //get time stamp
						   java.util.Date now = calendar.getTime();
						   java.sql.Timestamp currentTimestampEventSell = new java.sql.Timestamp(now.getTime());
				 	  	   System.out.println(currentTimestampEventSell+" Executed Sale for: "+BuyAndSellTicker.get(n)+" at "+NumberFormat.getCurrencyInstance().format(currentpricesell));
			      }
			 } //end of selling
			 if(TradeFlag.equals("b") && CashBalance > (NumOfSharesBSL*currentpricebuy + 7.95) ) //buying
			 {
				 if(currentpricebuy <= BuyThresholdBSL)
				 {
					  CashBalance = CashBalance - NumOfSharesBSL*currentpricebuy - TradeFeeBuy;
					  System.out.println("Cash: "+NumberFormat.getCurrencyInstance().format(CashBalance));
			    	  AvPrPerShare = ((Shares)*AvPrPerShare + NumOfSharesBSL*currentpricebuy + 7.95)/(Shares+NumOfSharesBSL);
			    	  PurchasePrice = ((Shares)*PurchasePrice + NumOfSharesBSL*currentpricebuy + 15.90)/(Shares+NumOfSharesBSL);
			    	  Shares += NumOfSharesBSL;

			    	  preparedStatement = connect
							  .prepareStatement("UPDATE frankstocktrading.position "
							  		+ "SET AccountID=?, Shares=?, PurchasePrice=ROUND(?,2), AveragePricePerShare=ROUND(?,2),"
							  		+ "PositionDate=?, TotalPositionCost=ROUND(?,2), TotalPositionValue=ROUND(?,2), Profit=ROUND(?,2)"
							  		+ "WHERE TickerID=?");
					 		  //1: aID, 2: Shares, 3: PurchasePrice, 4: AvPrPerShare, 5: Position Date,
					 		  //6: PositionCost, 7: Profit
							  preparedStatement.setInt(1, 1);
							  preparedStatement.setInt(2, Shares);
							  preparedStatement.setDouble(3, currentpricebuy);
							  preparedStatement.setDouble(4, AvPrPerShare);
							  Calendar calendar2 = Calendar.getInstance(); //get time stamp for MySQL
							  java.util.Date now2 = calendar2.getTime();
							  java.sql.Timestamp currentTimestampBuy = new java.sql.Timestamp(now2.getTime());
							  preparedStatement.setTimestamp(5, currentTimestampBuy);
							  preparedStatement.setDouble(6, TotalPositionCost + NumOfSharesBSL*currentpricebuy);
							  preparedStatement.setDouble(7, currentpricesell*Shares);
							  preparedStatement.setDouble(8, Profit + Shares*AvPrPerShare - Shares*PurchasePrice);
							  preparedStatement.setString(9, BuyAndSellTicker.get(n));
							  preparedStatement.executeUpdate();
					
			    	  preparedStatement = connect
					          .prepareStatement("UPDATE frankstocktrading.tradingaccount "
					          		+ " SET CashBalance=ROUND(?,2)"
					          		+ " WHERE AccountID=?");
					 		   preparedStatement.setDouble(1, CashBalance);
					 		   preparedStatement.setInt(2, aIDP);
					 		   preparedStatement.executeUpdate();
					
					 preparedStatement = connect
							  .prepareStatement("UPDATE frankstocktrading.buyandselllist "
							        + "SET TradeFlag=? WHERE TickerID=?");
					 		  preparedStatement.setString(1, "s");
					 		  preparedStatement.setString(2, BuyAndSellTicker.get(n));
					 		  preparedStatement.executeUpdate();
					 		  
					 		  Calendar calendar = Calendar.getInstance(); //get time stamp
							  java.util.Date now = calendar.getTime();
							  java.sql.Timestamp currentTimestampEventBuy = new java.sql.Timestamp(now.getTime());
					 	  	  System.out.println(currentTimestampEventBuy+" Executed Buy for: "+BuyAndSellTicker.get(n)+" at "+currentpricebuy);
				 }
			 }
		 }
	  AssetsBalance = 0;
	  
	  for(int j=0; j<BuyAndSellTicker.size(); j++)
   	  	{
		  preparedStatement = connect
			     .prepareStatement("SELECT TotalPositionValue FROM frankstocktrading.position WHERE "
			          		     + "TickerID=?");
				  preparedStatement.setString(1, BuyAndSellTicker.get(j));
		  resultSet = preparedStatement.executeQuery();
		  if(resultSet.next())
			 currentposvalue = resultSet.getInt("TotalPositionValue");
		  AssetsBalance += currentposvalue;
   	  	}
	  
   	  AssetsBalance += CashBalance;
   	  System.out.println("Assets: "+NumberFormat.getCurrencyInstance().format(AssetsBalance));
   	  preparedStatement = connect
	          .prepareStatement("UPDATE frankstocktrading.tradingaccount "
	          		+ " SET AssetsBalance=ROUND(?,2)"
	          		+ " WHERE AccountID=?");
	 		   preparedStatement.setDouble(1, AssetsBalance);
	 		   preparedStatement.setInt(2, aIDP);
	 		   preparedStatement.executeUpdate();
	 
	 	closeConnect(connect);
	 	closeResultSet(resultSet);
	 	closePreparedStatement(preparedStatement);
		 	
	}
	
	private static ArrayList<String> writeResultSet(ResultSet resultSet, ArrayList<String> 
	BuyAndSellTicker) throws SQLException 
	{
	    while (resultSet.next()) 
	    {
	      String tID = resultSet.getString("TickerID");
	      BuyAndSellTicker.add(tID);
	    }
	    return BuyAndSellTicker;
	 }

	  private static void closePreparedStatement(java.sql.PreparedStatement preparedStatement) {
		  try {
		      if (preparedStatement != null) 
		      {
		        preparedStatement.close();
		      }
		    } catch (Exception e) 
		    {
		    // don't throw now as it might leave following closables in undefined state
		    }
	}
	private static void closeResultSet(ResultSet c) {
		try {
		      if (c != null) 
		      {
		        c.close();
		      }
		    } catch (Exception e) 
		    {
		    // don't throw now as it might leave following closables in undefined state
		    }
		
	}
	private static void closeConnect(java.sql.Connection c) 
	  {
	    try {
	      if (c != null) 
	      {
	        c.close();
	      }
	    } catch (Exception e) 
	    {
	    // don't throw now as it might leave following closables in undefined state
	    }
	  }
}
