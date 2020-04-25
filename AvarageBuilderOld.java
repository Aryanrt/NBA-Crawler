import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AvarageBuilderOld {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st = null;
		st = conn.createStatement();
	    Statement st2 = null;
		st2 = conn2.createStatement();
		ResultSet rs = null;
		ResultSet rs2 = null;
		List<String> stat = new ArrayList<String>();
		stat.add("pts");
		stat.add("reb");
		stat.add("ORB");
		stat.add("DRB");
		stat.add("ast");
		stat.add("bs");
		stat.add("stl");
		stat.add("min");
		stat.add("pf");
		stat.add("tov");
		stat.add("fga");
		stat.add("fgm");
		stat.add("3pa");
		stat.add("3pm");
		stat.add("fta");
		stat.add("ftm");
        /////////////////////////////////////////////////////////////////////////
        String query ="SELECT * FROM player";
        rs = st.executeQuery(query);

		String playerID = null;
		DecimalFormat df = new DecimalFormat("#.##");
		while(rs.next())
		{
			int realStat = 0;
			int games =0;			
			System.out.println("playerID: "+ rs.getString("playerID"));
			String first = rs.getString("firstName");
			String last = rs.getString("lastName");
			System.out.println("first: "+ rs.getString("firstName")+ " last"+rs.getString("lastName"));
			playerID = rs.getString("playerID");
			////////////////////////////////////////////////
			//String query2 ="SELECT * FROM playerstats where playerID='"+ playerID+"'";
			for(int i=0; i<stat.size(); i++)
			{
				realStat = 0;
				games =0;			
				String query2 ="SELECT "+stat.get(i)+" FROM playerstats where playerID='"+ playerID+"'";
				rs2 = st2.executeQuery(query2);				
		        while(rs2.next())
				{
		        	if(rs2.getString(stat.get(i)) != null)
		        	{
		        		if(i == 7)
		        			realStat = realStat + Integer.parseInt(rs2.getString(stat.get(i)).substring(0, 2));
		        		else
		        			realStat = realStat+ Integer.parseInt(rs2.getString(stat.get(i)));
		        		games++;
		        	}
				}			
		        if(first.indexOf("'") != -1  && i == 0)
	 			{
	 				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
	 			}
	 			if(last.indexOf("'") != -1 && i == 0)
	 			{
	 				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
	 			}
		        if(games != 0)
		        {
		        	System.out.println("average"+stat.get(i)+" is:"+ df.format((float)realStat/games)+ "   number of games is"+games);
		        	query2 ="UPDATE player set "+ stat.get(i)+"='"+ df.format((float)realStat/games)+ "' where firstName='"+ first+"' and lastName='"+ last+"'";
		        	System.out.println(query2);
		        	st2.executeUpdate(query2);	
		        }
			}
		}
		
        
        
	}

}
