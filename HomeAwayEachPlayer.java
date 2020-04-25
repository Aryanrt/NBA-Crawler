import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeAwayEachPlayer {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn3 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st = null;
		st = conn.createStatement();
	    Statement st2 = null;
		st2 = conn2.createStatement();
		ResultSet rs = null;
		ResultSet rs3 = null;
		ResultSet rs2 = null;
		Statement st3 = null;
		st3 = conn.createStatement();
		DecimalFormat df = new DecimalFormat("#.##");
		
		String teamID = null;
		List<Integer> homeGameIDs= new ArrayList<Integer>();
		List<Integer> awayGameIDs= new ArrayList<Integer>();

        String query ="SELECT * FROM player";
        rs = st.executeQuery(query);

		String playerID = null;
		while(rs.next())
		{
			homeGameIDs= new ArrayList<Integer>();
			awayGameIDs= new ArrayList<Integer>();
			playerID= rs.getString("playerID");
			teamID = rs.getString("teamID");
			String name = rs.getString("firstName").concat(" ").concat(rs.getString("lastName"));
			String query2 = "select * from matchup where team1='"+teamID+"' or team2='"+teamID+"'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				String matchup= rs2.getString("matchupID");	
				String query3 = "select * from game where matchupID='"+matchup+"' and location='"+teamID+"' and date<20170113";
				rs3 = st3.executeQuery(query3);
				while(rs3.next())
				{
					homeGameIDs.add(rs3.getInt("gameID"));			 
				}
				query3 = "select * from game where matchupID='"+matchup+"' and location !='"+teamID+"'";
				rs3=st3.executeQuery(query3);
				while(rs3.next())
				{
					awayGameIDs.add(rs3.getInt("gameID"));			 
				}
			}//while
			double total = 0;
			int numberOfGame = 0;
			for(int home: homeGameIDs)
			{
				query2 = "select * from playerstats where playerID='"+playerID+"' and gameID='"+home+"'";
				rs2=st2.executeQuery(query2);
				//System.out.println("here"+ " plyer:"+ name+ playerID);
				while(rs2.next())
				{
					System.out.println("FP:" + rs2.getDouble("FP") + " plyer:"+ name+ playerID);
					total += rs2.getDouble("FP");
					numberOfGame++;
				}
			}
			if(numberOfGame != 0)
			{
				System.out.println(df.format((double)total/numberOfGame));
				query2="Update player set FPHOME='"+df.format((double)total/numberOfGame)+"' where playerID='"+ playerID+"'";
				st2.executeUpdate(query2);
			}	
			total = 0;
			numberOfGame = 0;
			for(int home: awayGameIDs)
			{
				query2 = "select * from playerstats where playerID='"+playerID+"' and gameID!='"+home+"'";
				rs2=st2.executeQuery(query2);
				while(rs2.next())
				{
					total += rs2.getDouble("FP");
					numberOfGame++;
				}
			}
			if(numberOfGame != 0)
			{
				query2="Update player set FPAWAY='"+df.format((double)total/numberOfGame)+"' where playerID='"+ playerID+"'";
				st2.executeUpdate(query2);
			}
			
		}
	}

}
