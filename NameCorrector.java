import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class NameCorrector {

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
		ResultSet rs = null;
		
		
		String query ="SELECT * FROM player";
        rs = st.executeQuery(query);
		while(rs.next())
		{
			String first = rs.getString("firstName");
			String last = rs.getString("lastName");
			if( first.indexOf("'") != -1  || last.indexOf("'") != -1)
				System.out.println(first+ " "+ last+ " id: "+ rs.getString("playerID"));
		}
	}

}
