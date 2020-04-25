import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerAnalyzer {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
		////////////
		 String csvFile = "src/FanDuel-NBA-2017-01-25-17776-players-list.csv";
		 int counter = -1;
	     BufferedReader br = null;
	     String line = "";
	     String cvsSplitBy = ",";	
	     br = new BufferedReader(new FileReader(csvFile));
         while ((line = br.readLine()) != null) {
        	 
        	 counter++;
        	 if(counter < 1)
        		 continue;
             // use comma as separator
             String[] country = line.split(cvsSplitBy);
             String position = country[1].replaceAll("^\"|\"$", "");
             String first =  country[2].replaceAll("^\"|\"$", "");
             String last = country[4].replaceAll("^\"|\"$", "");
			if(first.indexOf("'") != -1 )
 			{
 				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
 			}
			if(last.indexOf("'") != -1 )
 			{
 				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
 			}
         
             System.out.print("  "+first+ "   "+ last);
            
             String query = "SELECT * FROM player where firstName='"+first+"' AND LastName='" + last + "'";
             Statement st =conn.createStatement();
             ResultSet rs = st.executeQuery(query);
             int playerID = 0;
			while(rs.next())
			{
				playerID = rs.getInt("playerID");
				System.out.println("   found "+ playerID);
			}

         }
	}

}
