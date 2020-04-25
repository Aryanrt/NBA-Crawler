
public class PlayerPFHistory {

	private String season;
	private double fp;
	private int min;
	public PlayerPFHistory(String season, double fp, int min)
	{
		this.season = season;
		this.fp = fp;
		this.min = min;
	}
	public String getSeason() {
		return season;
	}
	public void setSeason(String season) {
		this.season = season;
	}
	public double getFp() {
		return fp;
	}
	public void setFp(double fp) {
		this.fp = fp;
	}
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	
}
