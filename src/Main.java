import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class Main
{
	public static void main(String[] args) throws ParseException, SQLException
	{
		Scanner input = new Scanner(System.in);
		getStockData getStockDataObject = new getStockData();
		BuyAndSell BuyAndSellObject = new BuyAndSell();
		SetBuyThreshold SetBuyThresholdObject = new SetBuyThreshold();
		String StartUp = "1";
		SetBuyThreshold.SetBuyThreshold();
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
