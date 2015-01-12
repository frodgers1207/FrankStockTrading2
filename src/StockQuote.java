import java.io.Closeable;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

public class StockQuote
{
	  DecimalFormat f = new DecimalFormat("##.00");
	  Connection connect = null;
	  Statement statement = null;
	  PreparedStatement preparedStatement = null;
	  ResultSet resultSet = null;
	  
	public void StockQuote(String symbol, double change,
			double LastAskPrice, double LastBidPrice, String name, String stockExchange) 
			throws SQLException 
	{
		
		  
		  
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			connect = DriverManager.getConnection("jdbc:mysql://mysql.frankrodgers.com", "wluadmissions", "g3n3r@ls");
			
			int NumOfSharesUpdate = 0;
			double positioncostupdate = 0;
			
	    	NumberFormat.getCurrencyInstance().format(LastAskPrice);
	    	NumberFormat.getCurrencyInstance().format(LastBidPrice);
	    	NumberFormat.getCurrencyInstance().format(change);
			
			preparedStatement = connect
					.prepareStatement("SELECT Shares, TotalPositionCost FROM frankstocktrading.position"
						 	       + " WHERE TickerID=?");
				 	 preparedStatement.setString(1, symbol);
				  resultSet = preparedStatement.executeQuery();
				  while(resultSet.next())
				  {
				 	 NumOfSharesUpdate = resultSet.getInt("Shares");
				 	 positioncostupdate = resultSet.getDouble("TotalPositionCost");
				  }
				  
				  f.format(positioncostupdate);
				  
			preparedStatement = connect 
			          .prepareStatement("UPDATE stockquotes.stock SET Name=?, Exchange=?, TradeReason=? "
			          		          + "WHERE TickerID=?;");
			      // (TickerID, Name, Beta, Exchange, TradeReason);
			      // parameters start with 1
			      preparedStatement.setString(1, name);
			      preparedStatement.setString(2, stockExchange);
			      preparedStatement.setString(3, "algorithm");
			      preparedStatement.setString(4, symbol);
			      preparedStatement.executeUpdate();
			
			preparedStatement = connect
				          .prepareStatement("UPDATE stockquotes.pricing SET QuoteTimeStamp=?, PriceChange=?, "
				          		+ "LastAskPrice=?, LastBidPrice=?"
			          		    + "WHERE TickerID=?;");
				      // (TickerID, QuoteTimeStamp, PriceChange, LastPrice");
					  Calendar calendar = Calendar.getInstance(); //get time stamp for MySQL
					  java.util.Date now = calendar.getTime();
					  java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
				      preparedStatement.setTimestamp(1, currentTimestamp);
				      preparedStatement.setDouble(2, change);
				      preparedStatement.setDouble(3, LastAskPrice);
				      preparedStatement.setDouble(4, LastBidPrice);
				      preparedStatement.setString(5, symbol);
				      preparedStatement.executeUpdate();
				      
		    preparedStatement = connect
				          .prepareStatement("UPDATE frankstocktrading.position SET TotalPositionValue=?, Profit=? "
				          		          + "WHERE TickerID=?;");
					      // (TickerID, QuoteTimeStamp, PriceChange, LastPrice");
					preparedStatement.setDouble(1, LastBidPrice*NumOfSharesUpdate);
					preparedStatement.setDouble(2, LastBidPrice*NumOfSharesUpdate - positioncostupdate);
					preparedStatement.setString(3, symbol);
					preparedStatement.executeUpdate();
		}
		catch(Exception ex)
		{
			System.out.println("Error: "+ex);
		}
		finally
		{
			close();
		}	
	}
	private void close() 
	  {
	    closeConnect(connect);
	    closeResultSet(resultSet);
	    closePreparedStatement(preparedStatement);
	  }
	  private void closePreparedStatement(PreparedStatement c) {
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
	private void closeResultSet(ResultSet c) {
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
	private void closeConnect(Connection c) 
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