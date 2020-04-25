import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mysql.cj.api.x.Collection;

public class FantasyFPtoSalary {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		final String user = "root";
		final String passwd = "";
		String myDriver = "com.mysql.cj.jdbc.Driver";
        System.setProperty("webdriver.chrome.driver", "C:/Users/dds/ChromeDriver/chromedriver.exe");
		Class.forName(myDriver);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/nba2?useSSL=false", user, passwd);
        Statement st;
        st = conn.createStatement();
	    ResultSet rs;
		DecimalFormat df = new DecimalFormat("#.##");
		List<Double> projectedFp = new ArrayList<Double>();
		List<Double> salaries = new ArrayList<Double>();
		List<Proportion> p = new ArrayList<Proportion>();
		
		String query = "Select * from fantasy where status!='GTD' and status!='O'";
		rs = st.executeQuery(query);
		while(rs.next())
		{
			projectedFp.add(rs.getDouble("Projected"));
			salaries.add(Double.parseDouble( rs.getString("salary")));
			//System.out.println(rs.getString("firstName")+" "+ rs.getString("lastName")+" :");
			//System.out.println(rs.getDouble("Projected") +" " +rs.getDouble("salary"));
			double x = 10000* (rs.getDouble("Projected")/ Double.parseDouble(rs.getString("salary")));
			String name = rs.getString("firstName").concat(" ".concat(rs.getString("lastName")));
			//System.out.println(x);
			//System.out.println("actual: "+ rs.getDouble("result"));
			p.add( new Proportion(name, x,rs.getDouble("result"), rs.getString("salary")));
		}
		Collections.sort(p, new Comparator<Proportion>() {
            @Override
            public int compare(Proportion lhs, Proportion rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getProp() > rhs.getProp() ? -1 : (lhs.getProp() < rhs.getProp() ) ? 1 : 0;
            }
        });
		for(Proportion p1: p)
		{
			System.out.println(p1);
		}
		

	}
	public static class  Proportion
	{
		private String name, salary;
		private double prop, actual;
		public Proportion(String name, double prop, double actual, String salary)
		{
			this.name = name;
			this.prop = prop;
			this.actual = actual;
			this.salary = salary;
		}
		public double getProp()
		{
			return prop;
		}
		public String toString()
		{
			return name + " "+ prop+ " actual is: "+ actual + " salary is: "+ salary;
		}
	
	}

}
