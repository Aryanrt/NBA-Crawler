import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;


public class FantasyTableFiller {

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
		 String csvFile = "src/FanDuel-NBA-2017-02-16-18021-players-list.csv";
		 int counter = -1;
	     BufferedReader br = null;
	     String line = "";
	     String cvsSplitBy = ",";	
	     br = new BufferedReader(new FileReader(csvFile));
		String playerID = null;
		DecimalFormat df = new DecimalFormat("#.##");
         while ((line = br.readLine()) != null) {
        	 
        	 counter++;
        	 if(counter < 1)
        		 continue;
             // use comma as separator
             String[] country = line.split(cvsSplitBy);
             String position = country[1].replaceAll("^\"|\"$", "");
             String first =  country[2].replaceAll("^\"|\"$", "");
             String last = country[4].replaceAll("^\"|\"$", "");
             String status = country[11].replaceAll("^\"|\"$", "");
             String Salary = country[7].replaceAll("^\"|\"$", "");
             String avg = country[5].replaceAll("^\"|\"$", "");
             String team= country[9].replaceAll("^\"|\"$", "");
             String against= country[10].replaceAll("^\"|\"$", "");
             
			if(first.indexOf("'") != -1 )
 			{
 				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
 			}
			if(last.indexOf("'") != -1 )
 			{
 				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
 			}
			System.out.println(df.format(Float.parseFloat(avg)) );
			String f = df.format(Float.parseFloat(avg));
			String query = "INSERT IGNORE INTO `fantasy`(`status`, `salary`, `position`, `firstName`, `lastName`,`fantasyAvg`,`team`,`against`) VALUES ('"+status+"','"
					+Salary+"','"+position+"','"+first+"','"+last+"','"+f +"','"+team+"','"+against+"')";
		    Statement st = null;
			st =  conn.createStatement();
			st.executeUpdate(query);
			
			}

	}

}
