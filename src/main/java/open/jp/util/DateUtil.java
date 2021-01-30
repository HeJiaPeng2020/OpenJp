package open.jp.util;

import java.util.Calendar;

public class DateUtil
{
	public static void main(String[] args) 
	{
		DateUtil.currentWeek();
	}
	
	public static void currentWeek()
	{
		Calendar calendar = Calendar.getInstance();
		
		System.out.println(calendar.get(Calendar.WEEK_OF_YEAR));
	}
}
