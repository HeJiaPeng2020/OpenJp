package open.jp;

public class UUID
{
	public static String random()
	{
		String uuid = java.util.UUID.randomUUID().toString().toUpperCase();
		
		return uuid.replace("-", "");
	}
}