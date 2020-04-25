import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class injuredPlayer {
	
	public double injuredPlayerEffect(String first, String last, String team,String injuredFirst, String injuredLast) throws SQLException, ClassNotFoundException
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
		List<Double> fps = new ArrayList<Double>();
		int playerID = 0, injuredPlayerID = 0;
		double total=0;
		
		String query = "Select * from player where firstName='"+ first+"' and lastName='"+last+"' and teamID like '%"+team+"%'";
		rs = st.executeQuery(query);
		while(rs.next())
			playerID = rs.getInt("playerID");
		
		query = "Select * from player where firstName='"+ injuredFirst+"' and lastName='"+injuredLast+"' and teamID like '%"+team+"%'";
		rs = st.executeQuery(query);
		while(rs.next())
			injuredPlayerID = rs.getInt("playerID");
		
		query="select * from playerStats p1,playerStats p2 where p1.playerID='"+ playerID +"' and p2.playerID='"+ injuredPlayerID
				+"' and p2.min ='0' and p1.min !='0'";
		rs = st.executeQuery(query);
		while(rs.next())
			if(rs.getInt("min") != 0)
				fps.add( rs.getDouble("FP"));
		
		for(double d: fps)
			total += d;
		
		return total/fps.size();
	}

}
