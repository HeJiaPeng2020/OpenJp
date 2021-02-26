package open.jp;

import java.util.Calendar;

public class Date
{
	public static void main(String[] args)
	{
		open.jp.Date.currentWeek();
	}
	
	
	public static void currentWeek()
	{
		Calendar calendar = Calendar.getInstance();
		
		System.out.println(calendar.get(Calendar.WEEK_OF_YEAR));
	}
}
