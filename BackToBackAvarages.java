import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.api.mysqla.result.Resultset;

public class BackToBackAvarages {

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
        Connection conn3 = DriverManager.getConnection("jdbc:mysql://localhost/2015-16?useSSL=false", user, passwd);
        Connection conn4 = DriverManager.getConnection("jdbc:mysql://localhost/2014-15?useSSL=false", user, passwd);
        Statement st,st2,st3,st4 = null;
        st = conn.createStatement();
        st2 = conn.createStatement();
        String query ="select * from player";
        ResultSet rs = st.executeQuery(query);
        while(rs.next())
        {
        	String playerID = rs.getString("playerID");
        	String query2="select * from playerstats where playerID='"+ playerID+"'";
        	ResultSet rs2 = st2.executeQuery(query2);
        	while(rs2.next())
        	{
        		String date = rs2.getString("date");
        		
        	}
        }
        
		
	}

}
