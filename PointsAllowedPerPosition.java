import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PointsAllowedPerPosition {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        /////////////////////////////////////////////////////////
        List<String> positions= new ArrayList<String>();
        positions.add("PG");
        positions.add("SG");
        positions.add("SF");
        positions.add("PF");
        positions.add("C");
        for(String position: positions)
        {
	        String query="SELECT * FROM team";
	        Statement st,st2,st3,st4,st5,st6;
			st =  conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			String team=null;
			//for each team
			while(rs.next())
			{
				team = rs.getString("abbriviation");
				String query2 = "select * from matchup where team1='"+team + "' or team2='"+ team+"'";
				st2 = conn.createStatement();
				ResultSet rs2 = st2.executeQuery(query2);
				List<Integer> matchups = new ArrayList<Integer>();
				List<Integer> games = new ArrayList<Integer>();
				List<Integer> players = new ArrayList<Integer>();
				while(rs2.next())
				{
					matchups.add(rs2.getInt("matchupID"));
					//System.out.println("here:"+ rs2.getInt("matchupID"));
				}	
				rs2.close();
				for(int i =0; i < matchups.size(); i++)
				{
					//System.out.println("matchupID:"+matchups.get(i) );
					query2 = "select * from game where matchupID='"+matchups.get(i) +"'";
					st3 = conn.createStatement();
					ResultSet rs3 = st3.executeQuery(query2);
					while(rs3.next())
					{
						//System.out.println(rs3.getInt("gameID"));
						games.add(rs3.getInt("gameID"));				
					}
				}
				float fantasy=0;
				for(int i=0; i < games.size(); i++)
				{
					players = new ArrayList<Integer>();
					query2="Select * from playerstats where gameID='"+games.get(i)+"'";
					st3 = conn.createStatement();
					ResultSet rs3 = st3.executeQuery(query2);
					while(rs3.next())
						players.add(rs3.getInt("playerID"));
					///////////////////////////////////////////////////////
					st3.close();
					for(int j =0; j < players.size(); j++)
					{
						query2="Select * from player where playerID='"+players.get(j)+"' and teamID !='"+ team +"' and position='"+
								position+"'";
						st4 = conn.createStatement();
						ResultSet rs4 = st4.executeQuery(query2);
						while(rs4.next())
						{
							//System.out.println( rs4.getString("lastName")+ "    "+ rs4.getString("teamID"));
							String query3="Select * from playerstats where playerID='"+players.get(j)+"' and gameID='"+games.get(i)+"'";
							st5 = conn.createStatement();
							ResultSet rs5 = st5.executeQuery(query3);
							while(rs5.next())
							{
								if((rs5.getInt("pts")+ 1.2*rs5.getInt("reb")+ 1.5*rs5.getInt("ast")+ 2*rs5.getInt("stl")+
										2*rs5.getInt("bs")-rs5.getInt("tov"))== 0)
									continue;
								fantasy += (float) (rs5.getInt("pts")+ 1.2*rs5.getInt("reb")+ 1.5*rs5.getInt("ast")+ 2*rs5.getInt("stl")+
										2*rs5.getInt("bs")-rs5.getInt("tov"));
								//System.out.println((rs5.getInt("pts")+ 1.2*rs5.getInt("reb")+ 1.5*rs5.getInt("ast")+ 2*rs5.getInt("stl")+
									//	2*rs5.getInt("bs")-rs5.getInt("tov")));
							}
							st5.close();
						}
						st4.close();
					}		
				}
				DecimalFormat df = new DecimalFormat("#.##");
				//System.out.println(fantasy/games.size());
				query2="update team set "+position + "A='"+ df.format(fantasy/games.size()) + "' where abbriviation='"+team+"'";
				st5 =  conn.createStatement();
				st5.executeUpdate(query2);
				//System.exit(0);
			}
			st.close();
		}
        
	}

}
