import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PositionParser {

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException 
	{
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = "jdbc:mysql://localhost";
		System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/2015-16?useSSL=false", user, passwd);
		////////////
		 String csvFile = "src/FanDuel-NBA-2017-01-12-17633-players-list.csv";
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
             String status;
			if(first.indexOf("'") != -1 )
 			{
 				first = first.substring(0, first.indexOf("'")) + "'"+first.substring(first.indexOf("'"));
 			}
 			if(last.indexOf("'") != -1 )
 			{
 				last = last.substring(0, last.indexOf("'")) + "'"+last.substring(last.indexOf("'"));
 			}
 			if(country[11].replaceAll("^\"|\"$", "").equals("GTD") || country[11].replaceAll("^\"|\"$", "").equals("O"))
 				System.out.print(country[11].replaceAll("^\"|\"$", "") + "  ");
             System.out.print(position + "  "+first+ "   "+ last);
             
             
             String query = "UPDATE `player` SET position='"+position+"' where firstName='"+first+"' AND LastName='" + last + "'";
 			
		     
			    Statement st = null;
				st = conn.createStatement();
				ResultSet rs = null;
				st.executeUpdate(query);

             
             
             
             ////////////////////////////////////////////////////////////
             System.out.print("   "+ Float.valueOf(country[5].substring(1, country[5].length()-1)).intValue());
             System.out.println("    "+ country[7].replaceAll("^\"|\"$", ""));
             
         }
	}

}
