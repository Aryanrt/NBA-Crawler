import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mysql.cj.api.mysqla.result.Resultset;

public class PointsAllowed 
{
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
        
        Statement st,st2,st3,st4,st5,st6;
		ResultSet rs, rs2, rs3, rs4, rs5;
		st =  conn.createStatement();
		String sql = "select * from team";
		rs = st.executeQuery(sql);
		while(rs.next())
		{
			String teamID=rs.getString("abbriviation");
			
		}
        
	}

}
