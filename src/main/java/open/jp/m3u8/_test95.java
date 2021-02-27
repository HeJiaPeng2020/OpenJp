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

public class _test95
{
	private static final int UNINIT = 0;
	
	private static final int NEW = 0;
	
	private static final int GET = 1;
	
	private static final int READY = 1;
	
	private static final int PARSED = 2;
	
	private static final int START = 4;
	
	private static final int FAIL = 8;
	
	private static final int FINISH = 9;
	
	public static void main(String[] args) throws Exception
	{
		while(true)
		{
			try
			{
				begin(null);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Thread.sleep(60*1000);

				if(e instanceof NotFoundException)//
				{	//当前m3u8有问题，直接跳过
					M3U8 m3u8 = Redis.get("m3u8", M3U8.class);
					try
					{
						getNextM3U8(m3u8);
					}
					catch(Exception e2)
					{
						e2.printStackTrace();
						Thread.sleep(60*1000);
					}
				}
			}
		}
	}
	
	//从0开始
	private static void begin(WebSite webSite) throws Exception
	{
		if (webSite==null) /*表示需要从Redis恢复数据，因为原程序的数据已消失了*/
		{
			webSite = Redis.get("webSite", WebSite.class);
		}
		
		WebPage webPage = null;
		
		if (webSite!=null) /*只处理webSite有数据的情况*/
		{
			if (webSite.getState()==UNINIT) /*还未初始化*/
			{
				webPage = initWebPage(webSite);
			}
			
			if (webSite.getState()==READY) /*开始页面爬取*/
			{
				startWebPage(webPage);
			}
			
			if (webSite.getState()==FINISH) /*网站爬取完毕*/
			{
				System.out.println("恭喜，当前网站已被你全部爬完！！！");
			}
		}
	}
	
	
	//开始页面爬取
	private static void startWebPage(WebPage webPage) throws Exception
	{
		WebItem webItem = null;
		
		if (webPage==null) /*表示需要从Redis恢复数据，因为原程序的数据已消失了*/
		{
			webPage = Redis.get("webPage", WebPage.class);
		}
		
		if (webPage.getState()==NEW) /*表示当前页面还未取任何一个数据出来下载*/
		{
			webItem = webPage.getWebItemList().get(0);
			
			webItem.setState(GET); /*此状态，专门为多线程预备的*/
			
			Redis.set("webItem", webItem);
			
			webPage.setState(READY);
			
			Redis.set("webPage", webPage);  /*更新了数据，必须重新存入Redis*/
		}
		
		if (webPage.getState()==READY) /*开始数据项爬取*/
		{
			startWebItem(webItem);
		}
		
		if (webPage.getState()==FINISH) /*表示当前页已爬取完毕，取下一页*/
		{
			getNextPage(webPage);
		}
	}
	
	
	//开始数据项爬取
	private static void startWebItem(WebItem webItem) throws Exception
	{
		M3U8 m3u8 = null;
		
		if (webItem==null) /*表示需要从Redis恢复数据，因为原程序的数据已消失了*/
		{
			webItem = Redis.get("webItem", WebItem.class);
		}
		
		if (webItem.getState()==GET) /*表示当前项是正在被处理的数据项，下一步需要解析出m3u8网址*/
		{
			m3u8 = parseM3U8(webItem);
		}
		
		if (webItem.getState()==PARSED) /*表示当前m3u8已成功解析，开始下载*/
		{
			startM3U8(m3u8);
		}
		
		if (webItem.getState()==FINISH) /*表示当前项已爬取完毕，取下一个*/
		{
			getNextWebItem(webItem);
		}
	}
	
	
	//开始m3u8
	private static void startM3U8(M3U8 m3u8) throws Exception
	{
		if (m3u8==null) /*表示需要从Redis恢复数据，因为原程序的数据已消失了*/
		{	
			m3u8 = Redis.get("m3u8", M3U8.class);
		}
		
		if (m3u8.getState()==UNINIT) /*ts列表还未初始化*/
		{
			try
			{
				initTs(m3u8);
			}
			catch (java.net.MalformedURLException e) 
			{
				System.out.println("当前M3U8的地址有误，跳过并继续：" + m3u8.getUrl());
				getNextM3U8(m3u8);
			}
		}
		
		if (m3u8.getState()==START) /*已开始*/
		{
			downloadTs(m3u8);
		}
		
		if (m3u8.getState()==FINISH) /*当前m3u8已完成下载，取下一个*/
		{
			getNextM3U8(m3u8);
		}
	}
	
	//下载ts
	private static void downloadTs(M3U8 m3u8) throws Exception
	{
		List<Ts> tsList = m3u8.getTsList();
		
		if (tsList!=null && tsList.size()>0) /*必须保证列表中有数据*/
		{
			for(Ts ts : tsList) /*遍历所有ts，依次下载*/
			{
				if (ts.getState()==NEW) /*表示还未被取出*/
				{
					ts.setState(GET); /*表示当前ts已被取出，专门为多线程准备的*/
					
					downloadTsOne(ts); /*取消无线循环，如遇异常，则抛出*/
					
					ts.setState(FINISH);
					
					Redis.set("m3u8", m3u8); /*父容器的集合中有更新，必须重新保存到Redis*/
				}
			}
			
			int size = 0;
			for(int i=0; i<tsList.size(); i++)
			{
				if (tsList.get(i).getState()==FINISH) /*统计下载成功ts的个数*/
				{
					size++;
				}
			}
			
			if (size==tsList.size()) /*只有全部ts下载完，才算成功*/
			{
				m3u8.setState(FINISH);
				
				Redis.set("m3u8", m3u8); /*有更新，必须重新保存到Redis*/
				
				System.out.println("========================当前m3u8已成功下载完毕==========================");
			}
		}
		else 
		{	throw new Exception("m3u8没有ts数据，请先检查！" + m3u8.getUrl());
		}
	}
	
	
	//单独一个ts下载
	private static void downloadTsOne(Ts ts) throws Exception
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
			ts.setState(FAIL);
			
			throw e;
		}
	}
	
	
	
	
	
	
	/////////////////////////////////////////////取下一部分/////////////////////////////////////////////
	private static void getNextM3U8(M3U8 m3u8) throws Exception
	{
		WebItem webItem = m3u8.getWebItem();
		
		webItem.setState(FINISH);
		
		Redis.set("webPage", webItem.getWebPage()); /*子集合更新了状态，必须保存父容器，否则父容器集合状态不正确*/
		
		Redis.set("webItem",webItem);
		
		getNextWebItem(webItem);
	}
	
	
	//取下一项
	private static void getNextWebItem(WebItem webItem) throws Exception
	{
		WebPage webPage = webItem.getWebPage();
		
		List<WebItem> webItemList = webPage.getWebItemList();
		
		for(WebItem webItemNext : webItemList) /**/
		{
			if (webItemNext.getState()==NEW) /**/
			{
				webItemNext.setState(GET); /*表示当前项已被取出，为了多线程而预留的*/
				
				Redis.set("webItem", webItemNext);
				
				startWebItem(webItemNext);
				
				return;
			}
		}
		
		webPage.setState(FINISH); /*当前页已全部取完*/
		
		Redis.set("webPage",webPage);
		
		System.out.println("========================当前页面已成功下载完毕==========================");
	}
	
	
	//取下一页
	public static void getNextPage(WebPage webPage) throws Exception
	{
		if (webPage==null) /*表示需要从Redis恢复数据*/
		{
			webPage = Redis.get("webPage", WebPage.class);
		}
		
		if (webPage!=null) /*只处理有数据的情况*/
		{
			String nextPage = webPage.getNextPage();
			
			WebSite webSite = new WebSite();
			
			webSite.setUrl(nextPage);
			
			Redis.set("webSite", webSite);
			
			begin(webSite);
		}
	}
	
	
	
	
	
	
	/////////////////////////////////////////////初始化部分/////////////////////////////////////////////
	private static WebPage initWebPage(WebSite webSite) throws Exception
	{
		String webSiteUrl = webSite.getUrl();
		
		URL webSiteURL = new URL(webSiteUrl);
		
		String path = webSiteURL.getPath();
		
		int index = path.indexOf("/", 1); /*搜索path后面是否还跟着下一级目录，比如：/aaa/bbb */
		
		if (index!=-1) /*只取主路径*/
		{
			path = path.substring(0, index); /*最终结果都是如此样式：/aaa */
		}
		
		Document document = JSoup.getHttps(webSiteUrl);
		
		//获取所有标签页，并分别找出：当前页、上一页、下一页
		Elements pageElements = document.select("div.box-page li"); 
		
		WebPage webPage = new WebPage();
		
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
					String currentPage = pageElement.child(0).attr("href"); /*比如：list_3_1.html */
					
					currentPage = webSiteURL.getProtocol() + "://" + webSiteURL.getHost() + path + "/" + currentPage;
					
					webPage.setCurrentPage(currentPage);
					
					continue;
				}
				
				String pageName = pageElement.child(0).html();
				
				if (pageName.equals("上一页")) /*找出上一页*/
				{
					String previousPage = pageElement.child(0).attr("href");
					
					previousPage = webSiteURL.getProtocol() + "://" + webSiteURL.getHost() + path + "/" + previousPage;
					
					webPage.setPreviousPage(previousPage);
					
					continue;
				}
				
				if (pageName.equals("下一页")) /*找出下一页*/
				{	
					String nextPage = pageElement.child(0).attr("href");
					
					nextPage = webSiteURL.getProtocol() + "://" + webSiteURL.getHost() + path + "/" + nextPage;
					
					webPage.setNextPage(nextPage);
					
					continue;
				}
			}
		}
		else
		{	throw new Exception("没有页码信息，请先检查！" + document.toString());
		}
		
		//获取所有项，然后依次保存进WebPage
		Elements itemElements = document.getElementsByClass("col-md-2 col-sm-3 col-xs-4 ");
		
		if (itemElements!=null && itemElements.size()>0) /*必须保证有数据*/
		{
			for(Element itemElement : itemElements)/* 遍历当前页面所有项 */
			{
				WebItem webItem = new WebItem();
				
				String webItemUrl = itemElement.child(0).attr("href");
				
				webItem.setWebItemUrl(webSiteURL.getProtocol() + "://" + webSiteURL.getHost() + webItemUrl);
				
				webItem.setWebPage(webPage);
				
				webPage.getWebItemList().add(webItem);
			}
			
			Redis.set("webPage", webPage); /*存入Redis，以备后用*/
			
			webSite.setState(READY); /*网站初始化完毕*/
			
			Redis.set("webSite", webSite); /*更新了数据，必须重新存入Redis*/
			
			return webPage;
		}
		else 
		{	throw new Exception("当前页面没有数据，请先检查！" + document.toString());
		}
	}
	
	
	//解析m3u8网址
	private static M3U8 parseM3U8(WebItem webItem) throws Exception
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
				
				webItem.setState(PARSED); /*表示当前项已成功解析出m3u8网址*/
				
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
	
	
	//初始化ts列表
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
						
						mediaUrlAbsolute = urlURL.getProtocol() + "://" + urlURL.getHost() + mediaUrl;
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
					
					tsAbsoluteUrl = urlURL.getProtocol() + "://" + urlURL.getHost() + line;
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
			m3u8.setState(START);
			
			Redis.set("m3u8", m3u8);
		}
		else
		{	throw new Exception("当前m3u8文件没有ts，请先检查！" + mediaUrl);
		}
	}
}
