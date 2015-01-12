import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mysql.jdbc.Statement;

public class SetBuyThreshold
{
	public static void SetBuyThreshold() throws SQLException
	{
		DecimalFormat f = new DecimalFormat("##.00");
		ArrayList<String> stockTickerBT = new ArrayList<>();
		double DaysStart = 0;
		ResultSet resultSet = null;
		Statement statement = null;
		java.sql.Connection connect = null;
		java.sql.PreparedStatement preparedStatement = null;
		
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
		          .prepareStatement("SELECT TickerID FROM stockquotes.stock");
		      resultSet = preparedStatement.executeQuery();
		      writeResultSet(resultSet, stockTickerBT);
		      
		String YahooURL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(";
			for(int x=0; x < stockTickerBT.size()-1; x++)
				{
			    	YahooURL += "\""+stockTickerBT.get(x)+"\"%2C";
				}
		YahooURL += "\""+stockTickerBT.get(stockTickerBT.size()-1)+"\"";
		YahooURL += ")&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
		//System.out.println(YahooURL);
		
		try { //connect to the URL
			
	           URL url = new URL(YahooURL); //begin connecting
	           URLConnection connection;
	           connection = url.openConnection();
	           HttpURLConnection httpConnection = (HttpURLConnection)connection;
	           int responseCode = httpConnection.getResponseCode(); //done connecting
	           
	           if (responseCode == HttpURLConnection.HTTP_OK) { //if connecting works
	        	  InputStream in = (InputStream) httpConnection.getInputStream();
	              DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	              DocumentBuilder db = dbf.newDocumentBuilder();
	              org.w3c.dom.Document dom = db.parse(in);
	              org.w3c.dom.Element docEle = dom.getDocumentElement(); //End of Connecting
	              
	              NodeList quoteList = docEle.getElementsByTagName("quote");
	              for (int i = 0 ; i < quoteList.getLength(); i++)
	              {
	            	  Node p = (Node) quoteList.item(i);
	            	  if(p.getNodeType()==Node.ELEMENT_NODE)
	            	  {
	            		  Element quote = (Element) p;
	            		  String tickerID = quote.getAttribute("symbol");
	            		  //System.out.println("tickerID: "+quote.getAttribute("symbol"));
	            		  NodeList infoList = quote.getChildNodes();
	            		  for(int j = 0; j < infoList.getLength(); j++)
	            		  {
	            			  Node details = (Node) infoList.item(j);
	            			  if(details.getNodeType()==Node.ELEMENT_NODE)
	            			  {
	            				  Element di = (Element) details;
	            				  
	            				  if(di.getTagName().equals("PreviousClose"))
	            				  {
	            					  DaysStart = Double.parseDouble(di.getTextContent());
	            					  NumberFormat.getCurrencyInstance().format(DaysStart);
	            					  
	            					  preparedStatement = connect
	            							  .prepareStatement("UPDATE frankstocktrading.buyandselllist "
	            							  		+ "SET BuyThreshold=? WHERE TickerID=?");
	            					  preparedStatement.setDouble(1, 1.01*DaysStart);
	            					  preparedStatement.setString(2, tickerID);
	            					  preparedStatement.executeUpdate();
	            				  }
	            			  }
	            		  
	            		  
	            	  } //end of parsing each stock
	              }
	            }
	          } 
			  }catch (MalformedURLException e) {
	            e.printStackTrace();
	          } catch (IOException e) {
	        	e.printStackTrace();
	          } catch (ParserConfigurationException e) {
	        	e.printStackTrace();
	          } catch (SAXException e) {
	        	e.printStackTrace();
	          }
	          finally {
	          }	
		
		connect.close();
	 	resultSet.close();
	 	preparedStatement.close();
	}
	 
	private static ArrayList<String> writeResultSet(ResultSet resultSet, ArrayList<String> stockTicker) throws SQLException {
		    while (resultSet.next()) 
		    {
		      String tID = resultSet.getString("TickerID");
		      stockTicker.add(tID);
		    }
		    return stockTicker;
		  }
}