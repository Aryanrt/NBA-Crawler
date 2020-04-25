import java.io.NotActiveException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//import FantasyFPtoSalary.Proportion;
//the algorithm
public class FantasyProjection {

	final static String date="20170216";
	
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
        Connection conn3 = DriverManager.getConnection("jdbc:mysql://localhost/2015-16?useSSL=false", user, passwd);
        Connection conn4 = DriverManager.getConnection("jdbc:mysql://localhost/2014-15?useSSL=false", user, passwd);
        Statement st,st2,st3,st4 = null;
        st = conn.createStatement();
	    st2 = conn2.createStatement();
	    st3 = conn3.createStatement();
	    st4 = conn4.createStatement();
		ResultSet rs,rs2,rs3,rs4 = null;
		List<String> stat = new ArrayList<String>();
		
        /////////////////////////////////////////////////////////////////////////
        String query ="SELECT * FROM fantasy";
        rs = st.executeQuery(query);

		DecimalFormat df = new DecimalFormat("#.##");
		
		while(rs.next())
		{
			List<PlayerPFHistory> playerHistory = new ArrayList<PlayerPFHistory>(); 
			String first = rs.getString("firstName");
			String last = rs.getString("lastName");
			String team = rs.getString("team");
			String against = rs.getString("against");
			String position = rs.getString("position");
			int salary = rs.getInt("salary");
			String matchupID = null;
			String gameID = null;
			String playerID = null;
			String team1, team2;
			String location = null;
			double fpResult=0, allowed=0;
			double fp = 0, fpAvarage = 0;
			double min = 0;
			String usagePercentage, fpLocation = null, fpOpponent = null;
			int  matchedPlayerID= 0;
			String matchedPlayerFirst = null,matchedPlayerLast = null;
			
			if(first.indexOf("'") != -1 )
				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
			
			if(last.indexOf("'") != -1 )
				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));

		    if(team.equals("PHO"))
			    team = "PHX";
		    if(against.equals("PHO"))
			    against = "PHX";
		    if(team.equals("SA"))
			    team = "SAS";
		    if(against.equals("SA"))
			    against = "SAS";
		    if(against.equals("WAS"))
			    against = "WSH";
		    if(team.equals("WAS"))
			    team = "WSH";
		    
		    if(team.compareTo(against) < 0)
		    {
			    team1 = team;
			    team2 = against;
		    }
		    else
		    {
			    team2 = team;
			    team1 = against;
		    }
		    
			String query2 = "SELECT * from matchup where team1 like '%"+team1+"%' and team2 like '%"+team2+"%'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
				matchupID = rs2.getString("matchupID");
			
			query2 = "SELECT * from game where matchupID='"+ matchupID+"' and date='"+date+"'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				location = rs2.getString("location"); 	
				gameID = rs2.getString("gameID");
			}
			if(first.equals("Guillermo") && last.equals("Hernangomez"))
				first = "Willy";
			if(first.equals("T.J.") && last.equals("warren"))
				first = "TJ";
			if(first.equalsIgnoreCase("c.j.") && last.equals("McCollum"))
				first = "CJ";
			if(first.equalsIgnoreCase("C.J."))
				first = "CJ";
			
			query2 = "SELECT * from player where firstName='"+ first+"' and lastName='"+last+"' and teamID like '%"+ team+"%'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				//setting avarages for the past x game
				fpAvarage = rs2.getDouble("FP");
				LastXGames g = new LastXGames(0, 0);
				g = lastXGamesFinder(first, last, team);
				fp = g.getFp();
				System.out.println("fp is:"+ fp);
				min = g.getMin();
				System.out.println("min is:"+ min);
				playerID = rs2.getString("playerID");
				min = rs2.getFloat("min");
			}
				
			System.out.println(gameID+ " "+ first + " "+ last+ " "+ location+" "+position +" FP avarage last x games:"+ fp+ " min:"+ min);
			query2 = "select * from player where position='"+position+"' and teamID like'%"+team+"%' and gameCount> '10'";
			rs2 = st2.executeQuery(query2);
			double total = 0;
			float minTotal = 0;
			while(rs2.next())
			{
				//System.out.println(rs2.getString("firstName")+ "  "+rs2.getString("lastName")+" "+ rs2.getDouble("FP"));
				total += rs2.getDouble("FP");
				minTotal += rs2.getFloat("min");
			}
			
			//System.out.println("he gets "+ df.format(100* fp/total)+ "% of points");
			//System.out.println("he gets "+ df.format(100* min/48)+ "% of time");
			usagePercentage = df.format(100* fp/total);
			
			
			query2 = "select * from player where playerID='"+playerID+"'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				if(team.equals(location))
				{
					fpLocation = rs2.getString("FPHOME");
					System.out.println("at home he is: "+ rs2.getString("FPHOME"));
				}
				else
				{
					fpLocation = rs2.getString("FPAWAY");
					System.out.println("away he is: "+ rs2.getString("FPAWAY"));
				}
			}
			
			query2= "Select * from team where abbriviation like '%"+against+"%'";
			rs2 = st2.executeQuery(query2);			
			while(rs2.next())
			{
				if(against.equals(location))
					{
						fpOpponent = rs2.getString(position+"AHOME");
						allowed = Double.parseDouble(rs2.getString(position+"AHOME"));
						System.out.println("allowd ponits for "+ position +" by "+ against+" is "+ rs2.getString(position+"AHOME"));
					}
				else
					{
						fpOpponent = rs2.getString(position+"AAWAY");
						allowed = Double.parseDouble(rs2.getString(position+"AAWAY"));
						System.out.println("allowd ponits for "+ position +" by "+ against+" is "+ rs2.getString(position+"AAWAY"));
					}
			}			

			
			/////////////////////////////
			//////////History////////////
			/////////////////////////////
			query2 ="select * from game where matchupID='"+ matchupID+"'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				String oldGameID;
				String oldGameDate;
				String query3;
				
				oldGameID = rs2.getString("gameID");
				oldGameDate = rs2.getString("date");; 
				//query3 = "select * from playerstats where gameID='"+oldGameID+"' and team";
			}
			
			query2 ="select * from player where teamID like '%"+against+"%' and position='"+ position+"'";
			rs2 = st2.executeQuery(query2);
			int flag = 0;
			float highestMin = 0, secondHighestMin= 0;
			String matchedplayer = null;
			while(rs2.next())
			{
				if(flag == 1)
					break;
				if(rs2.getFloat("min") > secondHighestMin )
					secondHighestMin = highestMin;

				System.out.println(rs2.getString("firstName").concat(" ").concat(rs2.getString("lastName")));
				if(min < 20 && secondHighestMin == highestMin )
				{
					matchedPlayerID = rs2.getInt("playerID");
					matchedPlayerFirst = rs2.getString("firstName");
					matchedPlayerLast = rs2.getString("LastName");
					
					
				}

				if(rs2.getFloat("min") > highestMin )
					highestMin = rs2.getFloat("min");
				if(min > 20 && highestMin == rs2.getFloat("min") )
				{
					matchedPlayerID = rs2.getInt("playerID");
					matchedPlayerFirst = rs2.getString("firstName");
					matchedPlayerLast = rs2.getString("LastName");
					
				}
				if(rs2.getFloat("min") > min -5 && rs2.getFloat("min") < min + 5)
				{
					
					flag = 1;
					matchedPlayerID = rs2.getInt("playerID");
					matchedPlayerFirst = rs2.getString("firstName");
					matchedPlayerLast = rs2.getString("LastName");
				}
			}
			/*if(first.equalsIgnoreCase("Andre") && last.equalsIgnoreCase("Roberson"))
			{
				matchedPlayerFirst = "Maurice";
				matchedPlayerLast = "Harkless";
			}*/
			
			System.out.println("matched with first: " + matchedPlayerFirst + " last:"+ matchedPlayerLast);
			query2= "select * from playerstats p1, playerstats p2 where p1.gameID = p2.gameID and p1.playerID='"+ playerID + 
					"' and p2.playerID='"+matchedPlayerID+"' and p1.gameID NOT IN (select gameID from game where date='"+date+"')";
							
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				if(rs2.getString("playerID").equals(playerID))
				{
					System.out.println("found history this season:");
					System.out.println("he got :"+ rs2.getDouble("FP")+" FP while having "+ rs2.getDouble("min")+" min");
					if(rs2.getDouble("min")* 2 < min)
						continue;
					playerHistory.add(new PlayerPFHistory("16-17", rs2.getDouble("FP"), rs2.getInt("min")));
				}				
			}
			
			/////8th feb
			query2= "select * from playerstats where playerID='"+ playerID+"' and gameID IN (select gameID from game "
					+ "where date!='"+date+"' and matchupID IN (select matchupID from matchup where (team1 like '%"+ team1 +"%' and team2 like "
							+ "'%" + team2 +"%') or ( team1 like '%"+ team2 +"%' and team2 like '%"+ team1+"%' )))";
							
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				if(rs2.getString("playerID").equals(playerID))
				{
					int f = 0;
					
					for(PlayerPFHistory p: playerHistory)
						if(p.getFp() == rs2.getDouble("FP") && p.getMin() == rs2.getInt("min"))
							f =1;
						
					if(f == 0)
					{
						System.out.println("found history this season against this team:");
						System.out.println("he got :"+ rs2.getDouble("FP")+" FP while having "+ rs2.getDouble("min")+" min");
					}
					if(rs2.getDouble("min")* 2 < min)
						continue;
					
					if(f == 0)
						playerHistory.add( new PlayerPFHistory("16-17", rs2.getDouble("FP"), rs2.getInt("min")));
				}				
			}
			//8th feb
			
			//////////////////////////////////////////////////////////////////////////////////
			
			String playerID2015= null;
			String matchedPlayerID2015 = null;
			if(matchedPlayerFirst != null && matchedPlayerLast != null )
			{
				if(matchedPlayerFirst.indexOf("'") != -1 && matchedPlayerFirst!= null)
	 			{
					matchedPlayerFirst = matchedPlayerFirst.substring(0, matchedPlayerFirst.indexOf("'")) + "'"+matchedPlayerFirst.substring(matchedPlayerFirst.indexOf("'"));
	 			}
				if(matchedPlayerLast.indexOf("'") != -1 && matchedPlayerLast!= null)
	 			{
					matchedPlayerLast = matchedPlayerLast.substring(0, matchedPlayerLast.indexOf("'")) + "'"+matchedPlayerLast.substring(matchedPlayerLast .indexOf("'"));
	 			}
			}
			query2= "select * from player where firstName='"+ first + "' and lastName='"+ last+"'";
			rs3 = st3.executeQuery(query2);
			String t1 = null, t2 = null;
			while(rs3.next())
			{
				t1 = rs3.getString("teamID");
				playerID2015 = rs3.getString("playerID");
			}
			query2= "select * from player where firstName='"+matchedPlayerFirst + "' and lastName='"+ matchedPlayerLast+"'";
			rs3 = st3.executeQuery(query2);
			while(rs3.next())
			{
				t2 = rs3.getString("teamID");
				matchedPlayerID2015 = rs3.getString("playerID");
			}
			query2= "select * from playerstats p1, playerstats p2 where p1.gameID = p2.gameID and p1.playerID='"+ playerID2015 + 
					"' and p2.playerID='"+matchedPlayerID2015+"'";
			rs3 = st3.executeQuery(query2);
			while(rs3.next())
			{
				if(rs3.getString("playerID").equals(playerID2015))
					if(rs3.getString("min") != null )
					{
						if(t1.equals(t2))
							continue;
						System.out.println("found history 2015/16:");
						System.out.println("he got :"+ rs3.getDouble("FP")+" FP while having "+ rs3.getString("min")
							.substring(0, 2)+" min");
						double d = Double.parseDouble(rs3.getString("min").substring(0, 2));
						if( d* 2 < min)
						{
							System.out.println("ignored");
							continue;
						}
						playerHistory.add(new PlayerPFHistory("15-16", rs3.getDouble("FP"), Integer.parseInt(rs3.getString("min")
							.substring(0, 2))));
					}
								
			}
			
			
			//////////////////////////////////////////////////////////////////////////////////
			/*
			String playerID2014= null;
			String matchedPlayerID2014 = null;
			
			query2= "select * from player where firstName='"+ first + "' and lastName='"+ last+"'";
			rs4 = st4.executeQuery(query2);
			while(rs4.next())
			{
				t1 = rs4.getString("teamID");
				playerID2014 = rs4.getString("playerID");
			}
			query2= "select * from player where firstName='"+matchedPlayerFirst + "' and lastName='"+ matchedPlayerLast+"'";
			rs4 = st4.executeQuery(query2);
			while(rs4.next())
			{
				t2 = rs4.getString("teamID");
				matchedPlayerID2014 = rs4.getString("playerID");
			}
		
			query2= "select * from playerstats p1, playerstats p2 where p1.gameID = p2.gameID and p1.playerID='"+ playerID2014 + 
					"' and p2.playerID='"+matchedPlayerID2014+"'";
			rs4 = st4.executeQuery(query2);
			while(rs4.next())
			{
				if(t1.equals(t2))
					continue;
				if(rs4.getString("playerID").equals(playerID2014))
					if(rs4.getString("min") != null )
					{
						System.out.println("found history 2014/15:");
						System.out.println("he got :"+ rs4.getDouble("FP")+" FP while having "+ rs4.getString("min")
							.substring(0, 2)+" min");
						
						double d = Double.parseDouble(rs4.getString("min").substring(0, 2));
						if( d * 2 < min)
						{
							System.out.println("stat ignored");
							continue;
						}
						playerHistory.add(new PlayerPFHistory("14-15", rs4.getDouble("FP"), Integer.parseInt(rs4.getString("min")
								.substring(0, 2))));
					}
								
			}
			*/
			//regulate min and FP accordingly/
			/*
			for(PlayerPFHistory p: playerHistory)
			{
				if(p.getMin() < 10)
					continue;
				if(p.getMin() != 0)
					p.setFp((p.getFp()*min)/p.getMin());
				//if(p.getFp() != 0)
					//p.setFp((p.getFp()* fpAvarage)/p.);
			}
			*/
			/*
			//regulate FP by FP: 2014
			query2= "select * from player where firstName='"+first+"' and lastName='"+ last+"'";
			rs3 = st3.executeQuery(query2);
			while(rs3.next())
				for(PlayerPFHistory p: playerHistory)
					if(p.getSeason().equals("14-15") && rs3.getDouble("FP")!= 0 && Math.abs(rs3.getDouble("FP") -fp)< 15  && rs3.getDouble("FP") > 15)
					{
						System.out.println((p.getFp()*fp)/rs3.getDouble("FP"));
						p.setFp((p.getFp()*fp)/rs3.getDouble("FP"));
					}

			//regulate FP by FP: 2015
			
			for(PlayerPFHistory p: playerHistory)
			{
				//if(fp > 30)
					//break;
				if(!(p.getSeason().equals("15-16")))
					continue;
				query2= "select * from player where firstName='"+first+"' and lastName='"+ last+"'";
				rs4 = st4.executeQuery(query2);
				while(rs4.next())
					if(rs4.getDouble("FP")!= 0  && Math.abs(rs4.getDouble("FP") -fp)< 15 && rs4.getDouble("FP") > 15)
					{
						System.out.println(rs4.getDouble("FP"));
						System.out.println((p.getFp()+(fp-rs4.getDouble("FP"))));
						p.setFp(p.getFp()+(fp-rs4.getDouble("FP")));
					}
			}
			
			*/
			double fpTotal1 = 0, fpTotal2 = 0, fpTotal3 = 0;
			int fpGameCount1 = 0, fpGameCount2 = 0, fpGameCount3 = 0;
			
			for(PlayerPFHistory p: playerHistory)
			{
				if(p.getSeason().equals("16-17"))
				{
					fpTotal1 += p.getFp();
					fpGameCount1++;
				}
				else if(p.getSeason().equals("15-16"))
				{
					fpTotal2 += p.getFp();
					fpGameCount2++;
				}
				else if(p.getSeason().equals("14-15"))
				{
					fpGameCount3++;
					fpTotal3 += p.getFp();
				}
				else
					System.out.println("can't be");
			}
			
			if(fpTotal1 == 0)
			{
				double a = 0;
				if(fpTotal2 == 0)
					a = fpTotal3/fpGameCount3;
				else if(fpTotal3 == 0)
					a = fpTotal2/fpGameCount2;
				else if(fpTotal3 != 0 && fpTotal2 != 0)
					a = (((fpTotal2/fpGameCount2)*2)+fpTotal3/fpGameCount3)/3;
				else
					System.out.println(" Historic FP = NOT AVIALABLE");
				System.out.println(" Historic FP =" + a);
			}
			else
			{
				double a;
				if(fpTotal2 == 0 && fpTotal3 == 0)
					 a = fpTotal1/fpGameCount1;
				else if(fpTotal2 == 0 && fpTotal3 != 0)
					a = ((fpTotal1/fpGameCount1)*3 + (fpTotal3/fpGameCount3)) /4;
				else if(fpTotal2 != 0 && fpTotal3 ==0)
					a = ((fpTotal1/fpGameCount1)*2+(fpTotal2/fpGameCount2))/3;
				else
					a = (fpTotal1/fpGameCount1)/2+(fpTotal2/fpGameCount2)/3	+(fpTotal3/fpGameCount3)/6;
				System.out.println(" Historic FP =" + a);
				
			}
			total = 0;
			flag =0;
			for(PlayerPFHistory p: playerHistory)
			{
				if(p.getSeason().equals("14-15"))
				{
					flag++;
					continue;
				}
				//System.out.println(p.getFp());
				total += p.getFp();
			}
			
			//System.out.println(total);
			//System.out.println(playerHistory.size());
			System.out.println(" Historic FP2 =" + total/(playerHistory.size()-flag));
			
			Collections.sort(playerHistory, new Comparator<PlayerPFHistory>() {
	            public int compare(PlayerPFHistory lhs, PlayerPFHistory rhs) {
	                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
	                return lhs.getFp() > rhs.getFp() ? -1 : (lhs.getFp() < rhs.getFp() ) ? 1 : 0;
	            }
	        });
			double a = 0;
			if(playerHistory.size() != 0)
			{

				System.out.println("");
				if(playerHistory.size() % 2 == 1 )
					a =  playerHistory.get(playerHistory.size()/2).getFp() ;
				else if( playerHistory.size() > 2)
					a = (playerHistory.get(playerHistory.size()/2).getFp()+ playerHistory.get(playerHistory.size()/2 +1 ).getFp()) /2;
				else
					a = (playerHistory.get(0).getFp()+ playerHistory.get(1).getFp())/2;
				
				double sum =0;
				for(PlayerPFHistory p: playerHistory)
					sum += p.getFp();
				double avg = sum /playerHistory.size();
				if(avg > a )
					a = avg;
				System.out.println(" Historic FP3 =" + a);
			}
			else
				System.out.println("No hisory found");
			
			//Feb13
			double temp = 0;
			for(PlayerPFHistory p: playerHistory)
				if(p.getFp() > temp)
					temp = p.getFp();
			//if(temp - a <= 10)
				//a = temp;
			
			//Feb13
			
			/////////////////////////////
			////////////History//////////
			/////////////////////////////
			///////////effect of injured player///////
			
			query2 = "select * from fantasy where (status='GTD' or status='O') and team like '%"+team+"%'";
			rs2 = st2.executeQuery(query2);
			double injuredEffectPF = 0;
			int size = 0;
			double s =0;
			double max = 0;
			while(rs2.next())
			{
				//if(rs2.getDouble("fantasyAvg") < 25 && ! rs2.getString("position").equalsIgnoreCase(position))
					//continue;
				System.out.println(rs2.getString("lastName"));
				injuredEffectPF = injuredPlayerEffect(first, last,position, team, rs2.getString("firstName"), rs2.getString("lastName"));
				System.out.println("injured is:"+ rs2.getString("firstName")+"  "+ rs2.getString("lastName"));
				System.out.println(injuredEffectPF);
				if(rs2.getString("position").equals(position) &&injuredEffectPF > fp +10 )
				{
					s = injuredEffectPF;
					size = 1;
					break;
				}
				if(! rs2.getString("position").equals(position) && rs2.getDouble("fantasyAvg") < 15 )
				{
					System.out.println("ignored!!");
					continue;
				}
				
				if(injuredEffectPF != 0)
				{
					size++;
					s += injuredEffectPF;
				}
				else
					System.out.println("injury ignored");
				if(injuredEffectPF > max)
					max = injuredEffectPF;
			}		
			if(team.equalsIgnoreCase("MIL"))
			{
				s = injuredPlayerEffect(first, last,position, team, "Jabari", "Parker");
				System.out.println("without Jabari:"+ s);
			}
			if(s != 0)
				injuredEffectPF = s/size;
			
			// this is to ignore the injury list
			injuredEffectPF = max;
			////////////////////////////
			//////////FP Projections/////
			/////////////////////////////
			//double yesterday = dayBaefore(playerID, team, "20170201");
			double yesterday =0;
			//if(injuredEffectPF - fp > 7 )
				//query2 = "update fantasy set projected='"+(injuredEffectPF+a)/2 +"' where firstName='"+ first
				//+"' and lastName='"	+last+"'";
			if(playerHistory.size() != 0)
			{
				String d, reason;
				if(injuredEffectPF == 0)
				{
					reason = "HST";
					d = df.format(a);
				}
					//d = df.format((fp+ a)/2);
				//d= df.format((total+ fp)/(playerHistory.size()+1));
				/*
				else if(injuredEffectPF  > fp)
				{
					reason = "INJ";
					d = df.format(injuredEffectPF);
				}*/
				else if(injuredEffectPF > a)
				{
					reason = "INJ";
					//d = df.format((a+ injuredEffectPF)/2);
					d = df.format(injuredEffectPF);
				}
				else
				{
					reason = "I/H";
					//d = df.format((a+ injuredEffectPF)/2);
					d = df.format((a+injuredEffectPF)/2);
				}
					//d = df.format((a+fp+ injuredEffectPF)/3);
					//d= df.format((total+fp+ injuredEffectPF)/(playerHistory.size()+2));
				
				if( first.equalsIgnoreCase("CJ"))
					first = "C.J.";
					
				query2 = "update fantasy set projected='"+d +"', reason='"+reason+"' where firstName='"+ first
					+"' and lastName='"	+last+"'";
				/*
				if(yesterday != 0 )
				{
					if(yesterday < fp  - 10)
						query2 = "update fantasy set projected='"+(Double.parseDouble(d)+fp-yesterday)+"' where firstName='"+ first
							+"' and lastName='"	+last+"'";
					else if(yesterday > fp)
						query2 = "update fantasy set projected='"+(Double.parseDouble(d)-yesterday+fp)+"' where firstName='"+ first
							+"' and lastName='"	+last+"'";
					else
						query2 = "update fantasy set projected='"+(Double.parseDouble(d)- 5 )+"' where firstName='"+ first
							+"' and lastName='"	+last+"'";
				}*/
			}
			else
			{
				String reason;
				if(injuredEffectPF == 0)
				{
					query2 = "update fantasy set projected='"+ df.format(fp)+"' where firstName='"+ first
						+"' and lastName='"	+last+"'";
					/*
					if(yesterday != 0 )
					{
						if(yesterday < fp  - 10)
							query2 = "update fantasy set projected='"+(fp+fp-yesterday)+"' where firstName='"+ first
								+"' and lastName='"	+last+"'";
						else if(yesterday > fp)
							query2 = "update fantasy set projected='"+(fp-yesterday+fp)+"' where firstName='"+ first
								+"' and lastName='"	+last+"'";
						else
							query2 = "update fantasy set projected='"+(fp- 5 )+"' where firstName='"+ first
								+"' and lastName='"	+last+"'";
					}
					*/
				}
				else
				{
					query2 = "update fantasy set projected='"+ df.format((injuredEffectPF+ fp)/2)+"' where firstName='"+ first
						+"' and lastName='"	+last+"'";
					if(injuredEffectPF > fp )
						query2 = "update fantasy set projected='"+ df.format(injuredEffectPF)+"', reason='INJ' where firstName='"+ first
								+"' and lastName='"	+last+"'";
					/*
					if(yesterday != 0 )
					{
						if(yesterday < fp  - 10)
							query2 = "update fantasy set projected='"+(((injuredEffectPF+ fp)/2)+fp-yesterday)+"' where firstName='"+ first
								+"' and lastName='"	+last+"'";
						else if(yesterday > fp)
							query2 = "update fantasy set projected='"+(((injuredEffectPF+ fp)/2)-yesterday+fp)+"' where firstName='"+ first
								+"' and lastName='"	+last+"'";
						else
							query2 = "update fantasy set projected='"+(((injuredEffectPF+ fp)/2)- 5 )+"' where firstName='"+ first
								+"' and lastName='"	+last+"'";
					}*/
					
				}
			}
			st2.executeUpdate(query2);
			query2 = "update fantasy set allowed='"+ allowed+"' where firstName='"+ first+"' and lastName='"+last+"'";
			st2.executeUpdate(query2);
			query2="select * from playerstats where playerID='"+playerID+"' and min > '0' and gameID IN(select gameID from game where date > '20161101')";
			rs2 = st2.executeQuery(query2);
			double totalGames = 0, fuckedUpGames = 0, realFuckup = 0;
			while(rs2.next())
			{
				totalGames++;
				
				/*if(salary < 4000 && rs2.getDouble("FP") < fp- 8 )
				{
					fuckedUpGames++;
					continue;
				}*/
				if( rs2.getDouble("FP") < (fp * 0.9) )
				{
					fuckedUpGames++;
				}	
				if( rs2.getDouble("FP") < fp - 10 )
				{
					realFuckup++;
				}	
				
			}
			if(totalGames != 0)
			{
				double d = 100 * fuckedUpGames/totalGames;
				query2 = "update fantasy set fuckUps='"+d+"' where firstName='"+ first
						+"' and lastName='"	+last+"'";
				st2.executeUpdate(query2);
				d = 100* realFuckup/totalGames;
				query2 = "update fantasy set realFuckUp='"+d+"' where firstName='"+ first
						+"' and lastName='"	+last+"'";
				st2.executeUpdate(query2);
			}
				/////////////////////////////
			//////////FP Projections/////
			/////////////////////////////

			query2 = "select * from playerstats where playerID='"+playerID+"' and gameID='"+ gameID+"'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				fpResult = Double.parseDouble(rs2.getString("FP"));
				System.out.println("actual result is:" + rs2.getString("FP"));
				System.out.println("My Projection 1 is: "+ df.format((2* Double.parseDouble(fpLocation) + (Double.parseDouble(usagePercentage)* 
						Double.parseDouble(fpOpponent)/100))/3));
				System.out.println("My Projection 2 is: "+ df.format((2* Double.parseDouble(fpLocation) + (Double.parseDouble(df.format(100* min/48))* 
					Double.parseDouble(fpOpponent)/100))/3));
				if(playerHistory.size() != 0)
				{
					if(injuredEffectPF - fp > 7 )
					{
						System.out.println("projection 4 because of injury: "+ df.format(injuredEffectPF));
					}
					else if(injuredEffectPF != 0)
						System.out.println("projection 4: "+ df.format((total/(playerHistory.size()-flag)+fp+ injuredEffectPF)/3));
					else
						System.out.println("projection 4: "+ df.format((total/(playerHistory.size()-flag)+fp)/2));
				}
				else
				{
					if(injuredEffectPF != 0)
						System.out.println("Projection 4: + "+df.format((injuredEffectPF+ fp)/2));
					else
						System.out.println("projection 4: "+ df.format(fp));
				}
				System.out.println("-------------------------------------------");				
			}
			//////////Actual Result//////
			////////////////////////////
			query2 = "update fantasy set result='"+fpResult+"' where firstName='"+first+"' and lastName='"+ last+"'";
			st2.executeUpdate(query2);
			/////////////////////////////
			////////Actual Result////////
		}
	}
	
	public static LastXGames lastXGamesFinder(String firstName, String lastName, String team) throws ClassNotFoundException, SQLException
	{
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st;
        st = conn.createStatement();
	    ResultSet rs;
		DecimalFormat df = new DecimalFormat("#.##");
		List<Integer> matchupIDs = new ArrayList<Integer>();
		List<Integer> gameIDs = new ArrayList<Integer>();
		List<Double> fps = new ArrayList<Double>();
		List<String> mins = new ArrayList<String>();
		int playerID = 0;
		int x = 5;
		
		
        
        String query ="select * from matchup where team1 like '%"+team+"%' or team2 like '%"+team+"%'";
        rs = st.executeQuery(query);
        while(rs.next())
        	matchupIDs.add(rs.getInt("matchupID"));
        
        for(int i: matchupIDs)
        {
		    query ="select * from game where matchupID='"+i+"' and date<'"+date+"' and date>'20170101'";
		    rs = st.executeQuery(query);
		    while(rs.next())
		    	gameIDs.add(rs.getInt("gameID"));
        }
    	query = "select * from player where firstName='"+ firstName+"' and lastName='"+ lastName+"' and teamID like '%"+team+"%'";
    	rs = st.executeQuery(query);
    	while(rs.next())
	    	playerID = rs.getInt("playerID");
    
    	System.out.println("p:"+ playerID + "g:"+ gameIDs);
        for(int i: gameIDs)
        {
        	query = "select * from playerstats where playerID='"+ playerID+ "' and gameID='"+ i+"'";
        	rs = st.executeQuery(query);
        	while(rs.next())
        	{
        		mins.add(rs.getString("min"));
        		fps.add(rs.getDouble("FP"));
        		System.out.println("here is: "+ rs.getDouble("FP"));
           	}
        }
        double sum = 0, sum1= 0;
        if(fps.size() == 0 || mins.size() == 0)
        	return new LastXGames(0, 0);
        for(double d: fps)
        	sum +=d;
        double mean = sum/fps.size();
        
        for(String s: mins)
        	sum1 += Double.parseDouble(s);
        double avarageMin = sum1/mins.size();
        
        double temp = 0;
        for(double d: fps)
        	temp += (d-mean)*(d-mean);
        double variance = temp/fps.size();
        
        
        conn.close();
		return new LastXGames(mean, avarageMin);
	}
	
	public static double injuredPlayerEffect(String first, String last,String position ,String team,String injuredFirst, String injuredLast) throws SQLException, ClassNotFoundException
	{
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection connn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st;
        st = connn.createStatement();
	    ResultSet rs;
		DecimalFormat df = new DecimalFormat("#.##");
		List<Double> fps = new ArrayList<Double>();
		int playerID = 0, injuredPlayerID = 0;
		double total=0;
		
		String query = "Select * from player where firstName='"+ first+"' and lastName='"+last+"' and teamID like '%"+team+"%'";
		rs = st.executeQuery(query);
		while(rs.next())
			playerID = rs.getInt("playerID");
		
		if(first.indexOf("'") != -1 )
			{
				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
			}
		if(last.indexOf("'") != -1 )
			{
				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
			}
		if(injuredFirst.indexOf("'") != -1 )
 			{
			injuredFirst = injuredFirst.substring(0, injuredFirst.indexOf("'")) + "'"+injuredFirst.substring(injuredFirst.indexOf("'"));
 			}
 		if(injuredLast.indexOf("'") != -1 )
 			{
 			injuredFirst = injuredFirst.substring(0, injuredFirst.indexOf("'")) + "'"+injuredFirst.substring(injuredFirst.indexOf("'"));
 			}
		
		
		query = "Select * from player where firstName='"+ injuredFirst+"' and lastName='"+injuredLast+"' and teamID like '%"+team+"%'";
		rs = st.executeQuery(query);
		System.out.println();
		while(rs.next())
		{
			injuredPlayerID = rs.getInt("playerID");
			System.out.println();
			
		//System.out.println("Injured playerID:"+ injuredPlayerID);
		
			if(rs.getString("position") == null || rs.getString("min") == null)
				continue;
			if(!(rs.getString("position").equals(position)) && rs.getFloat("min") < 10)
				return 0;
		
		}
		
		int j = 0;
		query="select * from playerStats p1,playerStats p2 where p1.playerID='"+ playerID +"' and p2.playerID='"+ injuredPlayerID
				+"' and p2.min ='0' and p1.min !='0' and p1.gameID=p2.gameID and p1.gameID NOT IN (select gameID from game where date='"+date+"')";
		rs = st.executeQuery(query);
		while(rs.next())
			if(rs.getInt("playerID") == playerID)
				fps.add( rs.getDouble("FP"));
		
		//10 feb
		List<Integer> games = new ArrayList<Integer>();
		query="select * from playerStats where playerID='"+ injuredPlayerID+"' ";
		rs = st.executeQuery(query);
		while(rs.next())
			games.add( rs.getInt("gameID"));
		
		query="select * from playerStats where playerID='"+ playerID+"' and gameID NOT IN (select gameID from game where date='"+date+"')";
		rs = st.executeQuery(query);
		while(rs.next())
			if(! games.contains(rs.getInt("gameID") ))
				fps.add( rs.getDouble("FP"));
		/* wtf is this?
		if(fps.size() == 0)
		{
			List<Integer> gameIDs = new ArrayList<Integer>();
			query = "select * from playerstats where playerID='"+ playerID+"'";
			rs = st.executeQuery(query);
			while(rs.next())
				gameIDs.add(rs.getInt("gameID"));
			
			for(int i: gameIDs)
			{
				query ="select * from playerstats where playerID='"+injuredPlayerID+"' and gameID='"+ i+"'";
				rs = st.executeQuery(query);
				if( rs.next() == false)
				{
					query = "select * from playerstats where playerID='"+ playerID+"' and gameID='"+i+"'";
					rs = st.executeQuery(query);
					while(rs.next())
					{
						j++;
						fps.add(rs.getDouble("FP"));
					}
				}
			}			
		}*/
		System.out.println("injury size is: "+ j);
		connn.close();
		for(double d: fps)
			total += d;
		if( fps.size() == 0)
			return 0;
		return total/fps.size();
	}

	public static double dayBaefore(String  playerID, String team, String date) throws ClassNotFoundException, SQLException
	{
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection connn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st,st2,st3;
        st = connn.createStatement();
        st2 = connn.createStatement();
        st3 = connn.createStatement();
	    ResultSet rs,rs2,rs3;
		DecimalFormat df = new DecimalFormat("#.##");
		List<Double> fps = new ArrayList<Double>();
		int gameID = 0;
		
		String query = "SELECT * from matchup where team1 like '%"+team+"%' or team2 like '%"+team+"%'";
		rs = st.executeQuery(query);
		while(rs.next())
		{
			String matchupID = rs.getString("matchupID");
			String query2 = "select * from game where matchupID='"+matchupID+"' and date='"+ date+"'";
			rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				System.out.println("here" + rs2.getString("gameID"));
				gameID = Integer.parseInt(rs2.getString("gameID"));
				break;
			}
			if(gameID != 0)
				break;
		}
		if(gameID == 0)
		{ 
			connn.close();
			conn1.close();
			System.out.println("no match yesterday");
			return 0;
		}
		query = "SELECT * from playerstats where gameID='"+ gameID+"' and playerID='"+ playerID+"'";
		rs = st.executeQuery(query);
		while(rs.next())
		{
			System.out.println("yesterday was :"+ rs.getDouble("FP"));
			//connn.close();
			conn1.close();
			return rs.getDouble("FP");
		}
		conn1.close();
		connn.close();
		return 0;
				
	}

}
