import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.management.Query;

public class BestLineUp {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn3 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn4 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn5 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn6 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn7 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn8 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn9 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st1,st2,st3,st4,st5,st6,st7,st8,st9 = null;
        st1 = conn1.createStatement();
	    st2 = conn2.createStatement();
	    st3 = conn3.createStatement();
	    st4 = conn4.createStatement();
	    st5 = conn5.createStatement();
	    st6 = conn6.createStatement();
	    st7 = conn7.createStatement();
	    st8 = conn8.createStatement();
	    st9 = conn9.createStatement();
		ResultSet rs1,rs2,rs3,rs4,rs5,rs6,rs7,rs8,rs9 = null;
		List<String> stat = new ArrayList<String>();
		DecimalFormat df = new DecimalFormat("#.##");
		double maxTotal=0, secondMaxTotal = 0, thirdMaxTotal = 0, forthMaxTotal = 0, fifthMaxTotal = 0;
		Stack<String> names = new Stack<String>();
		Stack<String> names1 = new Stack<String>();
		Stack<String> names2 = new Stack<String>();
		Stack<String> names3 = new Stack<String>();
		Stack<String> names4 = new Stack<String>();
		Stack<String> names5 = new Stack<String>();

		int numberOfGames = 0;
		String sql = "SELECT count(DISTINCT team)  as Count FROM fantasy";
		rs1 = st1.executeQuery(sql);
		while(rs1.next())
			numberOfGames = rs1.getInt("Count");
		
		sql = "SELECT * FROM fantasy where fantasyAVG > 15 order by projected/salary desc";
		rs1 = st1.executeQuery(sql);
		int i =0;
		while(rs1.next())
		{
			if(rs1.getString("status").equals("GTD") || rs1.getString("status").equals("O"))
				continue;
			if(i == 50 )
				break;
			//if(numberOfGames >= 4 && i == numberOfGames * 2)
				//break;
			String f = rs1.getString("firstName");
			String l = rs1.getString("lastName");
			if(f.indexOf("'") != -1 )
 			{
 				f = f.substring(0, f.indexOf("'")) + "'"+f.substring(f.indexOf("'"));
 			}
 			if(l.indexOf("'") != -1 )
 			{
 				l = l.substring(0, l.indexOf("'")) + "'"+l.substring(l.indexOf("'"));
 			}
			
			
			String sql2="INSERT IGNORE INTO fantasy2(status,salary,position, firstName, lastName, team, against, projected,result) VALUES('" 
					+rs1.getString("status")+ "','"+rs1.getInt("salary")+ "','"+ rs1.getString("position")+"','"+f+ "','"
					+l+"','" +rs1.getString("team")+"','"+rs1.getString("against")+"','" +rs1.getDouble("projected")+"','"+ rs1.getDouble("result")+"')";
			st2.executeUpdate(sql2);
			i++;
			
		}
		
		
		
        /////////////////////////////////////////////////////////////////////////
		//String query = "select * from fantasy f1, fantasy f2 where f1.position = f2.position and f1.lastName!= f2.lastName and ";
        String query ="SELECT * FROM fantasy2 where position='PG'";
        rs1 = st1.executeQuery(query);
		while(rs1.next())
		{
			//if( (Integer.parseInt( rs1.getString("salary"))* 4)/1000 > rs1.getDouble("projected"))
				//	continue;
			int salary = 0;
			double total = 0;
			salary += Integer.parseInt( rs1.getString("salary"));
			total += rs1.getDouble("projected");
			String first = rs1.getString("firstName");
			String last = rs1.getString("lastName");
			names.push(new String(rs1.getString("firstName")+ " "+rs1.getString("lastName")));
	        
			String query2 ="SELECT * FROM fantasy2 where position='PG' and (firstName !='"+ first+"' or lastName!='"+ last+"')";
	        rs2 = st2.executeQuery(query2);
			while(rs2.next())
			{
				//if( (Integer.parseInt( rs2.getString("salary"))* 4)/1000 > rs2.getDouble("projected"))
					//continue;
				salary += Integer.parseInt( rs2.getString("salary"));
				total += rs2.getDouble("projected");
				names.push(new String(rs2.getString("firstName")+ " "+rs2.getString("lastName")));
				
				String query3 ="SELECT * FROM fantasy2 where position='SG'";
		        rs3 = st3.executeQuery(query3);
				while(rs3.next())
				{
					if(salary + Integer.parseInt( rs3.getString("salary")) > 60000)
						continue;
					//if(( Integer.parseInt( rs3.getString("salary"))* 4)/1000 > rs3.getDouble("projected"))
						//continue;
					String first1 = rs3.getString("firstName");
					String last1 = rs3.getString("lastName");
					
					salary += Integer.parseInt( rs3.getString("salary"));
					total += rs3.getDouble("projected");
					names.push(new String(rs3.getString("firstName")+ " "+rs3.getString("lastName")));
					
					String query4 ="SELECT * FROM fantasy2 where position='SG' and (firstName !='"+ first1+"' or lastName!='"+ last1+"')";
			        rs4 = st4.executeQuery(query4);
					while(rs4.next())
					{
						if(salary + Integer.parseInt( rs4.getString("salary")) > 60000)
							continue;
						//if(( Integer.parseInt( rs4.getString("salary"))* 4)/1000 > rs4.getDouble("projected"))
							//continue;
						salary += Integer.parseInt( rs4.getString("salary"));
						total += rs4.getDouble("Projected");
						names.push(new String(rs4.getString("firstName")+ " "+rs4.getString("lastName")));
						
						String query5 ="SELECT * FROM fantasy2 where position='SF'";
				        rs5 = st5.executeQuery(query5);
						while(rs5.next())
						{
							if(salary + Integer.parseInt( rs5.getString("salary")) > 60000)
								continue;
							//if(( Integer.parseInt( rs5.getString("salary"))* 4)/1000 > rs5.getDouble("projected"))
								//continue;
							
							String first2 = rs5.getString("firstName");
							String last2 = rs5.getString("lastName");
							salary += Integer.parseInt( rs5.getString("salary"));
							total += rs5.getDouble("Projected");
							names.push(new String(rs5.getString("firstName")+ " "+rs5.getString("lastName")));
							
							String query6 ="SELECT * FROM fantasy2 where position='SF' and (firstName !='"+ first2+"' or lastName!='"+ last2+"')";
					        rs6 = st6.executeQuery(query6);
							while(rs6.next())
							{
								if(salary + Integer.parseInt( rs6.getString("salary")) > 60000)
									continue;
								//if(( Integer.parseInt( rs6.getString("salary"))* 4)/1000 > rs6.getDouble("projected"))
									//continue;
								salary += Integer.parseInt( rs6.getString("salary"));
								total += rs6.getDouble("Projected");
								names.push(new String(rs6.getString("firstName")+ " "+rs6.getString("lastName")));
							
								String query7 ="SELECT * FROM fantasy2 where position='PF'";
						        rs7 = st7.executeQuery(query7);
								while(rs7.next())
								{
									if(salary + Integer.parseInt( rs7.getString("salary")) > 60000)
										continue;
									//if(( Integer.parseInt( rs7.getString("salary"))* 4)/1000 > rs7.getDouble("projected"))
										//continue;
									String first3 = rs7.getString("firstName");
									String last3 = rs7.getString("lastName");
									salary += Integer.parseInt( rs7.getString("salary"));
									total += rs7.getDouble("Projected");
									names.push(new String(rs7.getString("firstName")+ " "+rs7.getString("lastName")));
								
									String query8 ="SELECT * FROM fantasy2 where position='PF' and (firstName !='"+ first3+"' or lastName!='"+ last3+"')";
							        rs8 = st8.executeQuery(query8);
									while(rs8.next())
									{
										if(salary + Integer.parseInt( rs8.getString("salary")) > 60000)
											continue;
										//if(( Integer.parseInt( rs8.getString("salary"))* 4)/1000 > rs8.getDouble("projected"))
											//continue;
										salary += Integer.parseInt( rs8.getString("salary"));
										total += rs8.getDouble("Projected");
										names.push(new String(rs8.getString("firstName")+ " "+rs8.getString("lastName")));
										String query9 ="SELECT * FROM `fantasy2` where position='C'";
								        rs9 = st9.executeQuery(query9);
										while(rs9.next())
										{
											if(salary + Integer.parseInt( rs9.getString("salary")) > 60000)
												continue;
											//if(( Integer.parseInt( rs9.getString("salary"))* 4)/1000 > rs9.getDouble("projected"))
												//continue;
											salary += Integer.parseInt( rs9.getString("salary"));
											total += rs9.getDouble("Projected");
											String s = new String(rs9.getString("firstName")+ " "+rs9.getString("lastName"));
											names.push(s);
											
											if(total >= maxTotal)
											{
												//System.out.println("found new max");
												//System.out.println(names);
												names5 = (Stack<String>) names4.clone();
												names4 = (Stack<String>) names3.clone();
												names3 = (Stack<String>) names2.clone();
												names2 = (Stack<String>) names1.clone();
												names1 = (Stack<String>) names.clone();
											}
											else if(total >= secondMaxTotal)
											{
												names5 = (Stack<String>) names4.clone();
												names4 = (Stack<String>) names3.clone();
												names3 = (Stack<String>) names2.clone();
												names2 = (Stack<String>) names.clone();
											}
											else if(total >= thirdMaxTotal)
											{
												names5 = (Stack<String>) names4.clone();
												names4 = (Stack<String>) names3.clone();
												names3 = (Stack<String>) names.clone();												
											}
											else if(total >= forthMaxTotal)
											{
												names5 = (Stack<String>) names4.clone();
												names4 = (Stack<String>) names.clone();																						
											}
											else if(total >= fifthMaxTotal)
											{
												names5 = (Stack<String>) names.clone();
											}
											
											names.pop();
											total = total - rs9.getDouble("result");
											salary = salary - Integer.parseInt( rs9.getString("salary"));											
										}//9th while
										names.pop();
										total = total - rs8.getDouble("result");
										salary = salary - Integer.parseInt( rs8.getString("salary"));
									}//8th while
									names.pop();
									total = total - rs7.getDouble("result");
									salary = salary - Integer.parseInt( rs7.getString("salary"));
								}//7th while
								names.pop();
								total = total - rs6.getDouble("result");
								salary = salary - Integer.parseInt( rs6.getString("salary"));
							}//6th while	
							names.pop();
							total = total - rs5.getDouble("result");
							salary = salary - Integer.parseInt( rs5.getString("salary"));
						}//5th while	
						names.pop();
						total = total - rs4.getDouble("result");
						salary = salary - Integer.parseInt( rs4.getString("salary"));
					}//4th while
					names.pop();
					total = total - rs3.getDouble("result");
					salary = salary - Integer.parseInt( rs3.getString("salary"));
				}//3rd while	
				names.pop();
				total = total - rs2.getDouble("result");
				salary = salary - Integer.parseInt( rs2.getString("salary"));
			}//2nd while
			names.pop();
			total = total - rs1.getDouble("result");
			salary = salary - Integer.parseInt( rs1.getString("salary"));
		}//1st while
		
		double realresult = 0;
		for(String s: names1)		{
			query ="SELECT * FROM fantasy2 where firstName='"+ s.split(" ")[0]+"' and lastName='"+ s.split(" ")[1]+"'";
		     rs1 = st1.executeQuery(query);
			 while(rs1.next())
			{
				 System.out.println(rs1.getString("firstName")+ " "+rs1.getString("lastName") + " "+ rs1.getString("Projected") );
				 realresult += rs1.getDouble("result");
				 System.out.println("that player that day was: "+ rs1.getDouble("result"));
			}
		}
		System.out.println("real sum is:"+ realresult);
		
		realresult = 0;
		for(String s: names2)
		{
			query ="SELECT * FROM fantasy2 where firstName='"+ s.split(" ")[0]+"' and lastName='"+ s.split(" ")[1]+"'";
		     rs1 = st1.executeQuery(query);
			 while(rs1.next())
			{
				 System.out.println(rs1.getString("firstName")+ " "+rs1.getString("lastName") + " "+ rs1.getString("Projected") );
				 realresult += rs1.getDouble("result");
				 System.out.println("that player that day was: "+ rs1.getDouble("result"));
			}
		}
		System.out.println("real sum is:"+ realresult);
		
		realresult = 0;
		for(String s: names3)
		{
			query ="SELECT * FROM fantasy2 where firstName='"+ s.split(" ")[0]+"' and lastName='"+ s.split(" ")[1]+"'";
		     rs1 = st1.executeQuery(query);
			 while(rs1.next())
			{
				 System.out.println(rs1.getString("firstName")+ " "+rs1.getString("lastName") + " "+ rs1.getString("Projected") );
				 realresult += rs1.getDouble("result");
				 System.out.println("that player that day was: "+ rs1.getDouble("result"));
			}
		}
		System.out.println("real sum is:"+ realresult);
		
		realresult = 0;
		for(String s: names4)
		{
			query ="SELECT * FROM fantasy2 where firstName='"+ s.split(" ")[0]+"' and lastName='"+ s.split(" ")[1]+"'";
		     rs1 = st1.executeQuery(query);
			 while(rs1.next())
			{
				 System.out.println(rs1.getString("firstName")+ " "+rs1.getString("lastName") + " "+ rs1.getString("Projected") );
				 realresult += rs1.getDouble("result");
				 System.out.println("that player that day was: "+ rs1.getDouble("result"));
			}
		}
		System.out.println("real sum is:"+ realresult);
		
		realresult = 0;
		for(String s: names5)
		{
			query ="SELECT * FROM fantasy2 where firstName='"+ s.split(" ")[0]+"' and lastName='"+ s.split(" ")[1]+"'";
		     rs1 = st1.executeQuery(query);
			 while(rs1.next())
			{
				 System.out.println(rs1.getString("firstName")+ " "+rs1.getString("lastName") + " "+ rs1.getString("Projected") );
				 realresult += rs1.getDouble("result");
				 System.out.println("that player that day was: "+ rs1.getDouble("result"));
			}
		}
		System.out.println("real sum is:"+ realresult);

	}

}
