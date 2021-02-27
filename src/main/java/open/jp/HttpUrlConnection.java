package open.jp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUrlConnection 
{
	public static InputStream getInputStream(String url) throws Exception
	{
		URL urlURL = new URL(url);
		
		HttpURLConnection httpURLConnection = (HttpURLConnection)urlURL.openConnection();

		httpURLConnection.setRequestProperty("accept", "*/*"); /*能接受任何请求类型*/
		httpURLConnection.setRequestProperty("accept-language", "zh-CN,zh;q=0.9"); /*能接受中文*/
		httpURLConnection.setRequestProperty("accept-encoding", "gzip"); /*压缩方式*/
		httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3314.0 Safari/537.36 SE 2.X MetaSr 1.0");
		
		httpURLConnection.setConnectTimeout(10*1000); /*连接超时*/
		httpURLConnection.setReadTimeout(10*1000); /*读取超时*/
		httpURLConnection.connect(); /*连接*/
		
        if (httpURLConnection.getResponseCode()!=200) /*只有200才算成功，缓存都不算*/
		{	
        	System.out.print(httpURLConnection.getResponseCode());
        	System.out.println(httpURLConnection.getResponseMessage());
			throw new Exception("发生异常：响应码不是200");
		}
        
        return httpURLConnection.getInputStream();
	}	
	
	
	
	
	public static void post(String url, String param) throws Exception
	{
		URL urlURL = new URL(url);
		
		HttpURLConnection httpURLConnection = (HttpURLConnection)urlURL.openConnection();

		httpURLConnection.setRequestMethod("POST"); /*尽量使用POST方式传输数据*/
		httpURLConnection.setDoInput(true); /*允许读数据，默认是true*/
		httpURLConnection.setDoOutput(true); /*允许写数据，默认为false*/
		
		httpURLConnection.setRequestProperty("accept", "*/*"); /*能接受任何请求类型*/
		httpURLConnection.setRequestProperty("accept-language", "zh-CN,zh;q=0.9"); /*能接受中文*/
		httpURLConnection.setRequestProperty("accept-encoding", "gzip"); /*压缩方式*/
		httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3314.0 Safari/537.36 SE 2.X MetaSr 1.0");
		
		httpURLConnection.setConnectTimeout(10*1000); /*连接超时*/
		httpURLConnection.setReadTimeout(10*1000); /*读取超时*/
		httpURLConnection.connect(); /*连接*/
		
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));
		bufferedWriter.write(param);
		bufferedWriter.close();
		
        if (httpURLConnection.getResponseCode()!=200) /*只有200才算成功，缓存都不算*/
		{	
        	System.out.print(httpURLConnection.getResponseCode());
        	System.out.println(httpURLConnection.getResponseMessage());
			throw new Exception("发生异常：响应码不是200");
		}
        
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        
        String line = null;
        while((line=bufferedReader.readLine()) != null)
        {
        	System.out.println(line);
        }
	}
}