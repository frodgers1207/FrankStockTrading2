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

public class getStockData
{
	public static void getStockData() throws ParseException, SQLException
	{
		ArrayList<String> stockTicker = new ArrayList<>();
		
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
		
		String Symbol = null; //start initializing variables
		double Change = 0;
		double LastAskPrice = 0;
		double LastBidPrice = 0;
		String Name = null;
		String StockExchange = null; // end initializing variables
		
		StockQuote StockQuoteObject = new StockQuote(); //connect to StockQuote.java
	
		  preparedStatement = connect
		          .prepareStatement("SELECT TickerID FROM stockquotes.stock");
		      resultSet = preparedStatement.executeQuery();
		      writeResultSet(resultSet, stockTicker);
		      
		String YahooURL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(";
			for(int x=0; x < stockTicker.size()-1; x++)
				{
			    	YahooURL += "\""+stockTicker.get(x)+"\"%2C";
				}
		YahooURL += "\""+stockTicker.get(stockTicker.size()-1)+"\"";
		YahooURL += ")&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
		System.out.println(YahooURL);
		
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
            				  
            				  if(di.getTagName().equals("ChangeRealtime"))
            				  {
            					  Change = Double.parseDouble(di.getTextContent());
            				  }
            				  if(di.getTagName().equals("AskRealtime")) // get ask and bid, replace
            				  {
            					  LastAskPrice = Double.parseDouble(di.getTextContent());
            				  }
            				  if(di.getTagName().equals("BidRealtime")) // get ask and bid, replace
            				  {
            					  LastBidPrice = Double.parseDouble(di.getTextContent());
            				  }
            				  if(di.getTagName().equals("Name"))
            				  {
            					  Name = di.getTextContent();
            				  }
            				  if(di.getTagName().equals("Symbol"))
            				  {
            					  Symbol = di.getTextContent();
            				  }
            				  if(di.getTagName().equals("StockExchange"))
            				  {
            					  StockExchange = di.getTextContent(); //end turning parsed data into variables
            				  }
            			  } //end of parsing sub nodes
            		  }
            		  
            		  connect.close();
            		  resultSet.close();
            		  preparedStatement.close();
            		  
            		  StockQuoteObject.StockQuote(Symbol, Change, LastAskPrice, LastBidPrice, 
            				  Name, StockExchange); //pass variables into StockQuote.java
            		  
            	  } //end of parsing each stock
              }
            }
          } catch (MalformedURLException e) {
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
	}
	
	 private static ArrayList<String> writeResultSet(ResultSet resultSet, ArrayList<String> stockTicker) throws SQLException 
	 {
		 //System.out.println("-----Trading Stocks-----");
		    while (resultSet.next()) {
		      String tID = resultSet.getString("TickerID");
		      //System.out.println(tID);
		      stockTicker.add(tID);
		    }
		    //System.out.println("------------------------");
		    return stockTicker;
	 }
	

}

