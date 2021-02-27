package open.jp;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.jsoup.nodes.Document;

public class JSoup
{
	public static Document getHttps(String url) throws Exception
	{
	/*
	在握手期间，如果URL的主机名和服务器的标识主机名不匹配，则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。
		        
       策略可以是基于证书的或依赖于其他验证方案。当验证URL主机名使用的默认规则失败时使用此回调。

		httpsConn.setHostnameVerifier(new HostnameVerifier() 
		{
			public boolean verify(String arg0, SSLSession arg1) 
			{
				return true;
			}
		});
		*/
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() 
		{
			@Override
			public boolean verify(String hostname, SSLSession session) 
			{
				return true;
			}
		});
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		
		sslContext.init(null, new DefaultX509TrustManager[]{new DefaultX509TrustManager()}, new SecureRandom());
		
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		
		Document document = org.jsoup.Jsoup.connect(url)
				.timeout(60*1000)
				.get();
		
		return document;
	}
		
		
	//SSL内部类
	private static class DefaultX509TrustManager implements X509TrustManager 
	{
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException 
		{
			
		}
	 
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException 
		{
			
		}
	 
		public X509Certificate[] getAcceptedIssuers() 
		{
			return null;
		}
	}
}