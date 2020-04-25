import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class FPforPlayerstatRow {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        ResultSet rs,rs2 = null;   
        Statement st,st2 = null;
		st = conn.createStatement();
		String query ="SELECT * FROM playerstats where gameID IN (select gameID from game where date='20170216')";
		query ="SELECT * FROM playerstats";
        rs = st.executeQuery(query);
        DecimalFormat df = new DecimalFormat("#.##");
        while(rs.next())
		{
			System.out.println("playerID: "+ rs.getString("playerID"));
			String playerID = rs.getString("playerID");
			String gameID = rs.getString("gameID");
			if(rs.getString("pts") != null)
			{
			double fp = Integer.parseInt(rs.getString("pts"))+Double.parseDouble(rs.getString("ast"))*1.5+ Double.parseDouble(rs.getString("reb"))*1.2
					+ 2*(Integer.parseInt(rs.getString("bs"))+Integer.parseInt(rs.getString("stl")))-Integer.parseInt(rs.getString("tov"));
			String query2 ="update playerstats set fp='"+df.format((float)fp)+"' where playerID='"+playerID+"' and gameID='"+ gameID+"'";
			st2 = conn2.createStatement();
			st2.executeUpdate(query2);
			}
		}
	}

}
