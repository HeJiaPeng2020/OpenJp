package open.jp;

import open.jp.m3u8.NotFoundException;

import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUrlConnection 
{
	public static InputStream getInputStream(String url) throws Exception
	{
		//初始化加密会话
		SSLContext sslContext = SSLContext.getInstance("TLS");
		//System.setProperty("https.protocols", "TLSv1.1");
		sslContext.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
		SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		
		//直接通过主机认证
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() 
        {
        	@Override
         	public boolean verify(String urlHostName, SSLSession session) 
        	{	return true;
         	}
        });
		
		//初始化连接
		URL urlURL = new URL(url);
        javax.net.ssl.HttpsURLConnection httpsURLConnection = (javax.net.ssl.HttpsURLConnection) urlURL.openConnection();
        httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
        httpsURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
        httpsURLConnection.setRequestProperty("Authorization","username");
        httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        
        //连接并等待响应结果
        httpsURLConnection.connect();
        if (httpsURLConnection.getResponseCode()!=200)/* 只有200才算成功，缓存都不算 */
		{
            if(httpsURLConnection.getResponseCode()==400)/*400异常的url要单独处理*/
            {   throw new NotFoundException(url);
            }
        	System.out.print(httpsURLConnection.getResponseCode());
        	System.out.println(httpsURLConnection.getResponseMessage());
			throw new Exception("发生异常：响应码不是200，也不是400");
		}
        
        return httpsURLConnection.getInputStream();
	}
	
	
	//SSL内部类
	private static class DefaultTrustManager implements X509TrustManager 
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