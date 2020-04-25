import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NumberOfGames {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
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
	
        /////////////////////////////////////////////////////////////////////////
        String query ="SELECT * FROM player";
        rs = st.executeQuery(query);

		String playerID = null;
		DecimalFormat df = new DecimalFormat("#.##");
		while(rs.next())
		{
			playerID = rs.getString("playerID");
			String query2 ="SELECT count(*) AS count FROM playerstats where playerID='"+ playerID+"'";
	        rs2 = st2.executeQuery(query2);
	        rs2.next();
	        int total = rs2.getInt("count");
	        query2="update player set gameCount='"+total+"' where playerID='"+playerID+"'";
	        st2.executeUpdate(query2);
		}
	}

}
