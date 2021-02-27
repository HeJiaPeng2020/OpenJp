package open.jp.m3u8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import open.jp.HttpUrlConnection;
import open.jp.HttpsUrlConnection;
import open.jp.JSoup;
import open.jp.Redis;

public class _test96 
{
	
	
	//爬取当前网站
	public static void main(String[] args) throws Exception
	{
		//Redis只能再程序刚启动恢复的时候使用，程序跑的期间都只存储（存储也只为了程序重新恢复时用一下）
		WebSite webSite = Redis.get("webSite", WebSite.class);
		
		WebPage webPage = null;
		
		if (webSite!=null) /*必须保证从Redis中恢复时，webSite有数据*/
		{
			if (webSite.getState()==0) /*当前网站还未开始过，需要先初始化*/
			{
				webPage = initWebSite(webSite);
			}
			
			if (webSite.getState()==1) /*继续当前webPage*/
			{
				continueWebPage(webPage);
			}
			
			if (webSite.getState()==9) /*已全部爬取完毕*/
			{
				System.out.println("恭喜，当前网站已被你全部爬完！！！");
			}
		}
	}
	
	
	//继续当前页面
	private static void continueWebPage(WebPage webPage) throws Exception
	{
		WebItem webItem = null;
		
		if (webPage==null) /*表示需要先从Redis中恢复数据*/
		{
			webPage = Redis.get("webPage", WebPage.class);
		}
		
		if (webPage.getState()==0) /*当前页还未开始过，需要先初始化一个数据，默认都是取当前页的第一个*/
		{
			webItem = webPage.getWebItemList().get(0);
			
			webItem.setState(1); /*表示当前项已被取出，为了多线程而预留的*/
			
			Redis.set("webItem", webItem); /*数据有更新，都必须重新保存到Redis*/
			
			webPage.setState(1); /*表示初始数据已具备*/
			
			Redis.set("webPage", webPage); /*数据有更新，都必须重新保存到Redis*/
		}
		
		if (webPage.getState()==1) /*继续当前项*/
		{
			continueWebItem(webItem);
		}
		
		if (webPage.getState()==9) /*当前页已爬取完毕，需要取一下页*/
		{
			
		}
	}
	
	
	//继续当前项
	private static void continueWebItem(WebItem webItem) throws Exception
	{
		M3U8 m3u8 = null;
		
		if (webItem==null) /*表示需要先从Redis中恢复数据*/
		{
			webItem = Redis.get("webItem", WebItem.class);
		}
		
		if (webItem.getState()==1) /*表示当前项已被取出，但是还未开始过，需要先初始化*/
		{
			m3u8 = initWebItem(webItem);
		}
		
		if (webItem.getState()==2) /*继续当前m3u8*/
		{
			continueM3U8(m3u8);
		}
		
		if (webItem.getState()==9) /*表示当前webItem已爬取完，取下一个*/
		{
			getNextWebItem(webItem);
		}
	}
	
	
	//继续当前m3u8
	private static void continueM3U8(M3U8 m3u8) throws Exception
	{
		if (m3u8==null) /*表示需要先从Redis中恢复数据*/
		{
			m3u8 = Redis.get("m3u8", M3U8.class);
		}
		
		if (m3u8.getState()==0) /*当前m3u8还未开始过，需要先初始化ts*/
		{
			initTs(m3u8);
		}
		
		if (m3u8.getState()==1) /*继续ts下载*/
		{
			continueTs(m3u8);
		}
		
		if (m3u8.getState()==9) /*当前m3u8已成功下载，取下一个*/
		{
			getNextM3U8(m3u8);
		}
	}
	
	
	//继续ts下载
	private static void continueTs(M3U8 m3u8) throws Exception
	{
		List<Ts> tsList = m3u8.getTsList();
		
		if (tsList!=null && tsList.size()>0) /*必须保证列表中有数据*/
		{
			for(Ts ts : tsList) /*遍历所有ts，依次下载*/
			{
				if (ts.getState()==0) /**/
				{
					ts.setState(1); /*表示当前ts已被取出，为多线程而预留*/
					
					continueTsdownload(ts); /*此处采取了无限循环，直到所有此ts下载成功*/
					
					ts.setState(9);
					
					Redis.set("m3u8", m3u8); /*父容器的集合中有状态更新，必须重新保存到Redis*/
				}
			}
			
			int size = 0;
			for(int i=0; i<tsList.size(); i++)
			{
				if (tsList.get(i).getState()==9) /*统计下载成功ts的个数*/
				{
					size++;
				}
			}
			
			if (size==tsList.size()) /*只有全部ts下载完，才算成功*/
			{
				m3u8.setState(9);
				
				Redis.set("m3u8", m3u8); /*有更新，必须重新保存到Redis*/
				
				System.out.println("========================当前m3u8已成功下载完毕==========================");
			}
		}
		else 
		{	throw new Exception("m3u8没有ts数据，请先检查！" + m3u8.getUrl());
		}
	}
	
	//正式下载ts
	private static void continueTsdownload(Ts ts)
	{
		try
		{
			String parentPath = ts.getRootFilePath() + ts.getParentFileName() ;
			
			File parentFile = new File(parentPath);
			
			if (!parentFile.exists()) /*下载前，必须保证父目录已创建好*/
			{	
				parentFile.mkdirs();
			}
			
			InputStream inputStream = HttpUrlConnection.getInputStream(ts.getUrl());
			
			FileOutputStream fileOutputStream = new FileOutputStream(new File(parentFile, ts.getFileName()));
			
			open.jp.File.write(inputStream, fileOutputStream);
			
			inputStream.close();
			
			fileOutputStream.close();
			
			System.out.println(ts.getUrl());
		}
		catch(Exception e)
		{
			ts.setState(8);
			
			e.printStackTrace();
			
			continueTsdownload(ts);
		}
	}
	
	
	
	
	////////////////////////////////////////////初始化部分////////////////////////////////////////////
	//初始化ts
	private static void initTs(M3U8 m3u8) throws Exception
	{
		String m3u8Url = m3u8.getUrl();
		
		InputStream inputStream = HttpsUrlConnection.getInputStream(m3u8Url);
		
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
		
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		String line = null;
		
		while( (line=bufferedReader.readLine()) !=null ) /*遍历m3u8文件所有内容*/
		{
			if (line.startsWith("#EXT-X-STREAM-INF")) /*表明此m3u8是主播放列表*/
			{
				String mediaUrl = bufferedReader.readLine();
				
				String mediaUrlAbsolute = null;
				
				if (mediaUrl!=null && !mediaUrl.equals("")) /*必须保证媒体播放列表地址存在*/
				{
					if (mediaUrl.startsWith("http")) /*是绝对地址*/
					{
						mediaUrlAbsolute = mediaUrl;
					}
					else if (mediaUrl.startsWith("/")) /*是根目录*/
					{
						URL urlURL = new URL(m3u8Url);
						
						mediaUrlAbsolute = urlURL.getProtocol() + "://" + urlURL.getHost() + ":" + urlURL.getPort() + mediaUrl;
					}
					else /*其它都认为是相对路径*/ 
					{	
						int index = m3u8Url.lastIndexOf("/");
						
						mediaUrlAbsolute = m3u8Url.substring(0, index+1) + mediaUrl;
					}
					
					initTsParseMaster(m3u8, mediaUrlAbsolute); return; /*重新主播放列表，然后直接结束*/
				}
				else
				{	throw new Exception("主播放列表里面的媒体播放列表为空，请先检查！" + m3u8Url);
				}
			}
			else if (line.startsWith("#EXTINF")) /*表明此m3u8是媒体播放列表*/
			{
				//initTsParseMedia(); return;
			}
			else /*其它则略过，并继续寻找*/
			{	continue;
			}
		}
		
		//寻找到结尾，都没有可用信息，则直接报错
		throw new Exception("当前m3u8文件有问题，请先检查！" + m3u8Url);
	}
	
	//解析主播放列表
	private static void initTsParseMaster(M3U8 m3u8, String mediaUrl) throws Exception
	{
		InputStream inputStream = HttpsUrlConnection.getInputStream(mediaUrl);
		
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
		
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		String line = null;
		
		while( (line=bufferedReader.readLine()) != null ) /*遍历媒体播放文件所有内容*/
		{
			if (!line.startsWith("#")) /*只要不是#开头的，都认为是ts地址*/
			{
				String tsAbsoluteUrl = null;
				
				if (line.startsWith("http")) /*ts地址是绝对路径*/
				{
					tsAbsoluteUrl = line;
				}
				else if (line.startsWith("/")) /*ts路径是根路径*/
				{
					URL urlURL = new URL(mediaUrl);
					
					tsAbsoluteUrl = urlURL.getProtocol() + "://" + urlURL.getHost() + ":" + urlURL.getPort() + line;
				}
				else /*其它都被认为是ts相对路径*/
				{
					int index = mediaUrl.lastIndexOf("/");
					
					tsAbsoluteUrl = mediaUrl.substring(0, index+1) + line;
				}
				
				Ts ts = new Ts();
				ts.setUrl(tsAbsoluteUrl);
				ts.setFileName(tsAbsoluteUrl.substring(tsAbsoluteUrl.lastIndexOf("/")+1));
				ts.setParentFileName(URLEncoder.encode(mediaUrl, "UTF-8"));
				m3u8.getTsList().add(ts);
			}
		}
		
		if (m3u8.getTsList().size()>0) /*必须保证m3u8成功解析出ts列表*/
		{
			m3u8.setState(1);
			
			Redis.set("m3u8", m3u8);
		}
		else
		{	throw new Exception("当前m3u8文件没有ts，请先检查" + mediaUrl);
		}
	}

	
	//初始化项数据
	private static M3U8 initWebItem(WebItem webItem) throws Exception
	{
		String webItemUrl = webItem.getWebItemUrl();
		
		Document document = JSoup.getHttps(webItemUrl);
		
		Elements elements = document.select("div.layout-box script:not([src])");
		
		if (elements!=null && elements.size()>0) /*必须保证有数据*/
		{
			String script = elements.toString();
			
			//找video:开头，找index.m3u8结尾，然后取中间
			int indexHead = script.indexOf("video:'");
			int indexTail = script.indexOf("index.m3u8");
			String m3u8Url = script.substring(indexHead+7, indexTail+10);
			
			if (m3u8Url!=null && !m3u8Url.equals("")) /*必须保证截取到了m3u8网址，否则报错*/
			{
				M3U8 m3u8 = new M3U8();
				
				m3u8.setUrl(m3u8Url);
				
				m3u8.setWebItem(webItem);
				
				Redis.set("m3u8", m3u8);
				
				webItem.setState(2); /*表示当前项已成功解析出m3u8网址*/
				
				Redis.set("webItem", webItem); /*更新了数据，必须重新存入Redis*/
				
				return m3u8;
			}
			else
			{	throw new Exception("没有截取到m3u8网址，请先检查！" + document.toString());
			}
		}
		else 
		{	throw new Exception("当前页面异常，请先检查！" + document.toString());
		}
	}
	
	
	//初始化页面数据
	private static WebPage initWebSite(WebSite webSite) throws Exception
	{
		WebPage webPage = new WebPage();
		
		String webSiteUrl = webSite.getUrl();
		
		Document document = JSoup.getHttps(webSiteUrl);
		
		//获取所有标签页，并分别找出：当前页、上一页、下一页
		Elements pageElements = document.select("div.box-page li"); 
		
		if (pageElements!=null && pageElements.size()>0) /*要求必须有页码信息*/
		{
			for(Element pageElement : pageElements) /**/
			{
				if (pageElement.children().size()==0) /*不处理没有子节点的*/
				{	
					continue;
				}
				
				if (pageElement.hasClass("thisclass")) /*找出当前页*/
				{	
					String currentPage = pageElement.child(0).attr("href");
					webPage.setCurrentPage(webSite.getUrl() + "/" + currentPage);
					continue;
				}
				
				String pageName = pageElement.child(0).html();
				
				if (pageName.equals("上一页")) /*找出上一页*/
				{	
					webPage.setPreviousPage(webSite.getUrl() + "/" + pageElement.child(0).attr("href"));
					continue;
				}
				
				if (pageName.equals("下一页")) /*找出下一页*/
				{	
					webPage.setNextPage(webSite.getUrl() + "/" + pageElement.child(0).attr("href"));
					continue;
				}
			}
		}
		else
		{	throw new Exception("当前页没有页码信息，请先检查！" + document.toString());
		}
		
		//获取所有项，然后依次保存进WebPage
		Elements itemElements = document.getElementsByClass("col-md-2 col-sm-3 col-xs-4 ");
		
		String hostUrl = webSiteUrl.substring(0, webSiteUrl.lastIndexOf("/"));
		
		if (itemElements!=null && itemElements.size()>0) /*必须保证有数据*/
		{
			for(Element itemElement : itemElements)/* 遍历当前页面所有项 */
			{
				WebItem webItem = new WebItem();
				
				String webItemUrl = itemElement.child(0).attr("href");
				
				webItem.setWebItemUrl(hostUrl + webItemUrl);
				
				webItem.setWebPage(webPage);
				
				webPage.getWebItemList().add(webItem);
			}
			
			Redis.set("webPage", webPage); /*存入Redis，以备后用*/
			
			webSite.setState(1); /*当前网站初始化完毕*/
			
			Redis.set("webSite", webSite); /*更新了数据，必须重新存入Redis*/
			
			return webPage;
		}
		else 
		{	throw new Exception("当前页没有数据，请先检查！" + webSiteUrl);
		}
	}
	
	
	////////////////////////////////////////////取一下部分////////////////////////////////////////////
	private static void getNextM3U8(M3U8 m3u8) throws Exception
	{
		WebItem webItem = m3u8.getWebItem();
		
		webItem.setState(9);
		
		Redis.set("webPage", webItem.getWebPage()); /*子集合更新状态，必须保存父容器，否则父容器集合的状态不正确*/
		
		Redis.set("webItem",webItem);
		
		getNextWebItem(webItem);
	}
	
	
	private static void getNextWebItem(WebItem webItem) throws Exception
	{
		WebPage webPage = webItem.getWebPage();
		
		List<WebItem> webItemList = webPage.getWebItemList();
		
		for(WebItem webItemNext : webItemList) /**/
		{
			if (webItemNext.getState()==0) /**/
			{
				webItemNext.setState(1); /*表示当前项已被取出，为了多线程而预留的*/
				
				Redis.set("webItem", webItemNext);
				
				continueWebItem(webItemNext);
				
				return;
			}
		}
		
		webPage.setState(9); /*当前页已全部取完*/
		
		Redis.set("webPage",webPage);
	}
}