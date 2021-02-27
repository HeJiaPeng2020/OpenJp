package open.jp;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;

public class Redis 
{
	private static Jedis jedis = new Jedis("localhost");
	
	
	public static void close()
	{
		jedis.close();
	}
	
	
	public static void set(String key, String value) throws Exception
	{
		String s = jedis.set(key, value);
		
		if (s==null || !s.equalsIgnoreCase("OK"))/* 若保存失败，必须终止继续，防止数据丢失 */
		{	
			throw new Exception("Redis保存失败！！！key=" + key);
		}
	}
	
	
	public static String get(String key)
	{
		return jedis.get(key);
	}
	
	
	public static void del(String key)
	{
		@SuppressWarnings("unused")
		Long l = jedis.del(key); /* 返回删除的个数 */
	}
	
	
	public static void set(String key, Object object) throws Exception
	{
		String value = JSON.toJSONString(object);
		
		String s = jedis.set(key, value);
		
		if (s==null || !s.equalsIgnoreCase("OK"))/* 若保存失败，必须终止继续，防止数据丢失 */
		{	
			throw new Exception("Redis保存失败！！！key=" + key);
		}
	}
	
	
	
	public static <T> T get(String key, Class<T> objectClass)
	{
		String value = jedis.get(key);
		
		T object = JSON.parseObject(value, objectClass);
		
		return object;
	}
	
	
	public static void save() throws Exception
	{
		String s = jedis.save();
		
		if (s==null || !s.equalsIgnoreCase("OK"))/* 若保存失败，必须终止继续，防止数据丢失 */
		{	
			throw new Exception("save保存失败！！！请检查原因！！！");
		}
	}
}
