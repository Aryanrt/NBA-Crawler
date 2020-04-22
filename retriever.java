import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class retriever 
{
	
	private static WebDriver driver;
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException 
	{
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
        
		
		List<Integer> matchups = new ArrayList<Integer>();
		List<Integer> gameIDs = new ArrayList<Integer>();
		List<String> locations = new ArrayList<String>();
		List<String> firstName = new ArrayList<String>();
 		List<String> lastName = new ArrayList<String>();
 		List<String> playerIDs = new ArrayList<String>();
 		List<WebElement> playerLinks;
 		String date = null;
 		int playerCount = 0;
		int numberOfGames = 0;
		String home=null;
		String away = null;
		
		
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        ///////////////////////////////////////////////
        //driver = new ChromeDriver();
 		//driver.get("https://watch.nba.com/game/20161025/SASGSW");
		int month = 2;
		int day = 16;
		
		while(month < 3)
		{
			//while((day < 32 && month == 1)||(month == 2 && day < 7 ))
			while(day < 17)
			{
				numberOfGames = 0;
				matchups = new ArrayList<Integer>();
				gameIDs = new ArrayList<Integer>();
				locations = new ArrayList<String>();
				///////////////////////////
				System.out.println("2017/0"+month+"/"+day);
				if(month == 11 && day==31 )
					break;
				if(day <10)
					date="20170"+ month + "0"+day;
				else
					date ="20170"+ month + day;
  
        ////////////////////////////////////////////////////////////////////
			    String query = "SELECT gameID,matchupID,location from game where date='"+date+"'";
			    //String query = "SELECT * from game where date='"+date+"' and matchupID IN (select matchupID from matchup where team1='PHX'"
			    //		+ "	or team2='PHX')";
			     
			    Statement st = null;
				st = conn.createStatement();
				ResultSet rs = null;
				rs = st.executeQuery(query);
			
				while(rs.next())
				{
			         matchups.add(rs.getInt("matchupID"));
			         gameIDs.add(rs.getInt("gameID"));
			         locations.add(rs.getString("location"));
			         numberOfGames++;
				}
				
				//going through each game
				for(int i= 0;i < numberOfGames; i++)
				{
					firstName = new ArrayList<String>();
			 		lastName = new ArrayList<String>();
			 		playerIDs = new ArrayList<String>();
			 		playerCount = 0;
					
					
					
					//////////////////////////
					query = "SELECT team1,team2 from matchup where matchupID='"+ matchups.get(i)+"'";
					st = null;
					st = conn.createStatement();
					rs = null;
					rs = st.executeQuery(query);
					
					//determining home n away//
					while(rs.next())
					{
				         home = rs.getString("team1");
				         away = rs.getString("team2");
				         if(away.equals(locations.get(i)))
				         {
				        	 String swap = home;
				        	 home = away;
				        	 away = swap;
				         }
					}		
					
					driver = new ChromeDriver();
					//get game's boxscore
					if(home.equals("WSH"))
						driver.get("https://watch.nba.com/game/"+date+"/"+ away + "WAS");
					else if(away.equals("WSH"))
						driver.get("https://watch.nba.com/game/"+date+"/"+ "WAS"+home);
					else
						driver.get("https://watch.nba.com/game/"+date+"/"+ away + home);
					driver.manage().window().maximize();
					if( driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[3]/div[2]")) == null)
						continue;
					driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[3]/div[2]")).click();
					
					playerCount = 0;
					playerLinks = driver.findElements(By.className("player-profile"));
					for(WebElement e: playerLinks)
			 		{
						String first = e.findElement(By.className("first-name")).getText();
						System.out.println("first is:" + first);
						String last = e.findElement(By.className("last-name")).getText();
						if(first.equalsIgnoreCase("tj") && last.equalsIgnoreCase("warren"))
							first = "T.J.";
						if(first.indexOf("'") != -1 )
			 			{
			 				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
			 			}
			 			if(last.indexOf("'") != -1 )
			 			{
			 				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
			 			}
			 			firstName.add(first);
			 			lastName.add(last);
			 			playerCount++;
			 			
			 			query = "INSERT IGNORE INTO player(teamID, firstName, lastName) VALUES('"+ home + "','" + first + "','"+ last + "')";
			 			st.executeUpdate(query);
			 			query = "SELECT playerID FROM player where teamID='"+ home +"' AND firstName='"+ first +"' AND lastName='"+ last+"'";
			 			rs = st.executeQuery(query);
			 			String dummy = null;
			 			while(rs.next())
						{
			 				dummy = rs.getString("playerID");
			 				playerIDs.add(dummy);
			 			}
			 			query = "INSERT IGNORE INTO playerStats(playerID,gameID) VALUES('"+ dummy + "','"+ gameIDs.get(i)+ "')";
						st.executeUpdate(query);
						 			
			 		}	
			 		//reading boxscore numbers for home
					for(int column = 2; column < 22; column++)
					{
						for(int row = 0; row <= playerCount ; row++)
						{
							//extracting teamstats
							if(row == playerCount)
							{
								query = "INSERT IGNORE INTO teamstats (teamID, gameID) VALUES('"+ home + "','"+ gameIDs.get(i)+"')";
								st.executeUpdate(query);
								int stat = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]"
										+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td")).getText());
								System.out.println("stat is:"+ stat);
								
								switch(column)
								{
									case 3:
										query = "UPDATE teamstats SET pts='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 4:
										query = "UPDATE teamstats SET reb='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 5:
										query = "UPDATE teamstats SET ast='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 6:
										query = "UPDATE teamstats SET stl='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 7:
										query = "UPDATE teamstats SET bs='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 9:
										query = "UPDATE teamstats SET fgm='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 10:
										query = "UPDATE teamstats SET fga='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 12:
										query = "UPDATE teamstats SET 3pm='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 13:
										query = "UPDATE teamstats SET 3pa='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 15:
										query = "UPDATE teamstats SET ftm='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 16:
										query = "UPDATE teamstats SET fta='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 18:
										query = "UPDATE teamstats SET ORB='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 19:
										query = "UPDATE teamstats SET DRB='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 20:
										query = "UPDATE teamstats SET tov='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 21:
										query = "UPDATE teamstats SET pf='"+stat + "'where teamID='"+ home +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									
								}
								break;
							}					
							
							
							/////////////////////////////////////////////////////////////
							if(column == 8 || column == 14 || column == 11 || column == 17)
								break;
							System.out.println(driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]"
									+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td")).getText());
							System.out.println("column is:"+ column + " row is:" + row);
							int stat = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]"
									+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td")).getText());
							switch(column)
							{
								case 2:
									query = "UPDATE playerstats SET min='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 3:
									query = "UPDATE playerstats SET pts='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 4:
									query = "UPDATE playerstats SET reb='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 5:
									query = "UPDATE playerstats SET ast='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 6:
									query = "UPDATE playerstats SET stl='" +stat+ "'where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 7:
									query = "UPDATE playerstats SET bS='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 9:
									query = "UPDATE playerstats SET fgm='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 10:
									query = "UPDATE playerstats SET fga='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 12:
									query = "UPDATE playerstats SET 3pm='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 13:
									query = "UPDATE playerstats SET 3pa='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 15:
									query = "UPDATE playerstats SET ftm='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 16:
									query = "UPDATE playerstats SET fta='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 18:
									query = "UPDATE playerstats SET ORB='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 19:
									query = "UPDATE playerstats SET DRB='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 20:
									query = "UPDATE playerstats SET tov='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 21:
									query = "UPDATE playerstats SET pf='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
							}//switch
							st.executeUpdate(query);
						}//for
					}//for
					/////////////////////////////////////////////////////////////////////////////////////////////////////away:
					//away team click 
					driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[1]/div/div[1]")).click();
					playerCount = 0;
					playerIDs = new ArrayList<String>();
					firstName = new ArrayList<String>();
					lastName = new ArrayList<String>();
					firstName = new ArrayList<String>();
					lastName = new ArrayList<String>();
					playerLinks = new ArrayList<WebElement>(); 
					playerLinks =driver.findElements(By.className("player-profile"));
					for(WebElement e: playerLinks)
			 		{
						String first = e.findElement(By.className("first-name")).getText();
						System.out.println("first is:" + first);
						String last = e.findElement(By.className("last-name")).getText();
						/*
			 			String p = e.getAttribute("href").substring(27,e.getAttribute("href").lastIndexOf("/"));
			 			int index = p.indexOf("/");
			 			String first = p.substring( 0, index);
			 			String last = p.substring(index+1).replace("/", " ").replace("_", " ");
			 			*/
			 			if(last.indexOf("'") != -1 )
			 			{
			 				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
			 			}
			 			if(first.indexOf("'") != -1 )
			 			{
			 				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
			 			}
			 			firstName.add(first);
			 			lastName.add(last);
			 			System.out.println(last);
			 			playerCount++;
			 			
			 			query = "INSERT IGNORE INTO player(teamID, firstName, lastName) VALUES('"+ away + "','" + first + "','"+ last + "')";
			 			st.executeUpdate(query);
			 			query = "SELECT playerID FROM player where teamID='"+ away +"' AND firstName='"+ first +"' AND lastName='"+ last+"'";
			 			rs = st.executeQuery(query);
			 			String dummy = null;
			 			while(rs.next())
						{
			 				dummy = rs.getString("playerID");
			 				System.out.println("added "+dummy);
			 				playerIDs.add(dummy);
			 			}
			 			query = "INSERT IGNORE INTO playerStats(playerID,gameID) VALUES('"+ dummy + "','"+ gameIDs.get(i)+ "')";
						st.executeUpdate(query);
						 			
			 		}	
			 		//reading boxscore numbers for away
					for(int column = 2; column < 22; column++)
					{
						for(int row = 0; row <= playerCount ; row++)
						{
							//extracting teamstats
							if(row == playerCount)
							{
								query = "INSERT IGNORE INTO teamstats (teamID, gameID) VALUES('"+ away + "','"+ gameIDs.get(i)+"')";
								st.executeUpdate(query);
								int stat = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[1]"
										+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td")).getText());
								System.out.println("stat is:"+ stat);
								
								switch(column)
								{
									case 3:
										query = "UPDATE teamstats SET pts='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 4:
										query = "UPDATE teamstats SET reb='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 5:
										query = "UPDATE teamstats SET ast='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 6:
										query = "UPDATE teamstats SET stl='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 7:
										query = "UPDATE teamstats SET bs='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 9:
										query = "UPDATE teamstats SET fgm='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 10:
										query = "UPDATE teamstats SET fga='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 12:
										query = "UPDATE teamstats SET 3pm='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 13:
										query = "UPDATE teamstats SET 3pa='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 15:
										query = "UPDATE teamstats SET ftm='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 16:
										query = "UPDATE teamstats SET fta='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 18:
										query = "UPDATE teamstats SET ORB='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 19:
										query = "UPDATE teamstats SET DRB='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 20:
										query = "UPDATE teamstats SET tov='"+stat + "'where teamID='"+ away +"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									case 21:
										query = "UPDATE teamstats SET pf='"+stat + "'where teamID='"+ away+"' AND gameID='" + gameIDs.get(i)+"'";
										st.executeUpdate(query);
										break;
									
								}
								break;
							}					
							
							
							
							/////////////////
							if(column == 8 || column == 14 || column == 11 || column == 17)
								break;
							System.out.println(driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[1]"
									+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td")).getText());
							System.out.println("column is:"+ column + " row is:" + row);
							int stat = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[1]"
									+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td")).getText());
							switch(column)
							{
								case 2:
									query = "UPDATE playerstats SET min='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 3:
									query = "UPDATE playerstats SET pts='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 4:
									query = "UPDATE playerstats SET reb='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 5:
									query = "UPDATE playerstats SET ast='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 6:
									query = "UPDATE playerstats SET stl='" +stat+ "'where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 7:
									query = "UPDATE playerstats SET bS='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 9:
									query = "UPDATE playerstats SET fgm='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 10:
									query = "UPDATE playerstats SET fga='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 12:
									query = "UPDATE playerstats SET 3pm='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 13:
									query = "UPDATE playerstats SET 3pa='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 15:
									query = "UPDATE playerstats SET ftm='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 16:
									query = "UPDATE playerstats SET fta='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 18:
									query = "UPDATE playerstats SET ORB='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 19:
									query = "UPDATE playerstats SET DRB='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 20:
									query = "UPDATE playerstats SET tov='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
								case 21:
									query = "UPDATE playerstats SET pf='" +stat+ "' where playerID='"+ playerIDs.get(row) + "' AND gameID='"
											+gameIDs.get(i) + "'";
									break;
							}//switch
							st.executeUpdate(query);
						}//for
					
					}//for
					
					
					driver.close();
					
				}
								
				
				
			     /////////////////////////////////
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/+ "/div[2]/div/ul/li["+column+"]/table/tbody/tr["+(row+1)+"]/td"
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[2]/table/tbody/tr[14]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[3]/table/tbody/tr[14]/td
						 
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[1]/div[2]/div/ul/li[2]/table/tbody/tr[1]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[1]/table/tbody/tr[1]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[15]/table/tbody/tr[1]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[15]/table/tbody/tr[1]/td	
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[13]/table/tbody/tr[13]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[2]/table/tbody/tr[1]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[2]/table/tbody/tr[2]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[21]/table/tbody/tr[1]/td
				
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[3]/table/tbody/tr[1]/td
				// /html/body/div[7]/div[2]/div[6]/div[1]/div[2]/div[2]/div[2]/div/ul/li[3]/table/tbody/tr[2]/td
			    
						 //driver.close();
						 day++;
 				 }
 				 day = 1;
 				 month++;
		}//going through days
		
	}

}
