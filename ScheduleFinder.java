import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import com.gargoylesoftware.htmlunit.javascript.host.Element;

public class ScheduleFinder {
	private static WebDriver driver;
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException {
		// TODO Auto-generated method stub
		ScheduleFinder t = new ScheduleFinder();
		
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		//driver = new ChromeDriver();

		////////////////////////////////////////////////////////////
		//final String host = "localhost";
		final String user = "root";
		final String passwd = "";
		  
		
		 String myDriver = "com.mysql.cj.jdbc.Driver";
         String myUrl = "jdbc:mysql://localhost";
         Class.forName(myDriver);
         Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);

         ////////////////////////////////////
 
		// our SQL SELECT query. 
         // if you only need a few columns, specify them by name instead of using "*"
         String query = "SELECT abbriviation FROM team";

         // create the java statement
        Statement st = null;
		st = conn.createStatement();
		ResultSet rs = null;
		rs = st.executeQuery(query);
	
		List<String> teamSchadule = new ArrayList<String>();
		int i = 0 ;
		 while(rs.next()){
	         //Retrieve by column name
	         String s =rs.getString("abbriviation");
			 teamSchadule.add(s);
			 i++;
		 }
		// System.exit(1);
		 for(int j = 0; j < i;j++)
		 {
			 
		driver = new ChromeDriver();
		driver.get("http://www.espn.com/nba/team/schedule/_/name/"+teamSchadule.get(j));
		//driver.findElement(By.className("calendar-date")).click();
		//driver.findElement(By.cssSelector("a.ui-datepicker-prev.ui-corner-all")).click();
		//driver.findElement(By.cssSelector("a.ui-datepicker-prev.ui-corner-all")).click();
		
		List<WebElement> rows = driver.findElements(By.tagName("tr"));
		int counter = 0;
		 
		 
		////////////////////////////////////////////////////////////
		 boolean year = false;
		for(WebElement row: rows)
		{
			if(counter < 2)
			{
				counter++;
				continue;
			}
			
			Boolean home = true;
			String team1;
			String team2 = null;
			String date;
			List<WebElement> columns = row.findElements(By.tagName("td"));
			String month;
			String day;
			int counter2 = 0;
			String finalDate = null;
			
			for(WebElement column: columns)
			{

				if( column.getText().equals("JANUARY"))
					year = true;				
				if(column.getText().equals("NOVEMBER") || column.getText().equals("DECEMBER") || column.getText().equals("JANUARY") ||column.getText().equals("FEBRUARY")
						||column.getText().equals("MARCH")||column.getText().equals("APRIL"))
					break;

				if(counter2 == 2)
					break;
				if(counter2 == 0)
				{

					date = column.getText().substring(5, column.getText().length());
					System.out.println(date);
					Date date2 = new SimpleDateFormat("MMMM").parse(date.substring(0, 3));
					Calendar cal = Calendar.getInstance();
					cal.setTime(date2);
						
					month =Integer.toString(cal.get(Calendar.MONTH) + 1);
					day = date.substring(4);
					if(Integer.parseInt(date.substring(4)) < 10)
						day = "0".concat(day);
					if(Integer.parseInt(month) < 10)
						month = "0".concat(month);
					
					String sqlDate = month.concat(day);
					if(year == true)
						finalDate = "2017".concat(sqlDate);
					else
						finalDate = "2016".concat(sqlDate);
				
				}
				else					
				{
					if(column.getText().split("\n")[0].equals("@"))
						home =false;
					
					if(column.getText().split("\n")[1].contains("NY"))
						team2 ="NYK";
					else if(column.getText().split("\n")[1].contains("Los Angeles"))
						team2 ="LAL";
					else if(column.getText().split("\n")[1].contains("LA"))
						team2 = "LAC";
					else 
					{
						query = "SELECT abbriviation FROM team where teamName like'%"+
								column.getText().split("\n")[1]+"%'";
						st = null;
						st = conn.createStatement();
						rs = null;
						rs = st.executeQuery(query);
				
						while(rs.next()){
							//Retrieve by column name
							team2 = rs.getString("abbriviation");
							System.out.println(team2);
					}
				}
				 ////////
				 //team1 alphabetically proceeds
				 team1 = teamSchadule.get(j);
				 String otherTeam = team2;
				 String OriginalTeam = team1;
				 if(team1.compareTo(team2) > 0)
				 {
					 String swap = team1;
					 team1 = team2;
					 team2 = swap;
				 }
				 int matchupID = 0;
				 query = "SELECT matchupID FROM matchup where team1='"+ team1 + "' AND team2='"+team2 + "'";
			     st = null;
				 st = conn.createStatement();
			     rs = null;
			     rs = st.executeQuery(query);
				 while(rs.next()){
				    //Retrieve by column name
				    //System.out.println("matchup is: "+rs.getInt("matchupID"));
				    matchupID = rs.getInt("matchupID");
				 }
				 if(home == true)
					 query = "INSERT IGNORE INTO GAME(matchupID, date, location) VALUES('"+ matchupID + "','" + finalDate + "','"
							 + OriginalTeam+ "')";
				 else
					 query = "INSERT IGNORE INTO GAME(matchupID, date, location) VALUES('"+ matchupID + "','" + finalDate + "','"
							 + otherTeam+ "')";
				 st = null;
				 st = conn.createStatement();
			     rs = null;
			     int k = st.executeUpdate(query);
			
				}
				counter2++;
			}
			counter++;
		}
		
		//List<WebElement> e = driver.findElements(By.cssSelector("a.ui-state-default"));
		//driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
		

		
		//e.get(24).click();	
		
		driver.close();
		 }
		
	}

	
}
