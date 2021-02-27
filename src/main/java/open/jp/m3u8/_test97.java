package open.jp.m3u8;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import open.jp.HttpsUrlConnection;
import open.jp.JSoup;
import open.jp.Redis;

public class _test97 
{
	
	
	//继续当前网站
	public static void main(String[] args) throws Exception
	{
		WebSite webSite = Redis.get("webSite", WebSite.class);
		
		if (webSite!=null) /*只处理Redis中存在webSite的情况*/
		{
			if (webSite.getState()==0) /*还未开始过，必须先初始化*/
			{
				initWebPage(webSite);
			}
			
			if (webSite.getState()==1) /*正在爬取状态，继续即可*/
			{
				continueWebPage();
			}
			
			if (webSite.getState()==9) /*已全部爬取完毕*/
			{
				System.out.println("恭喜，当前网站已被你全部爬完！！！");
			}
		}
	}
	
	
	//继续当前页
	private static void continueWebPage() throws Exception
	{
		WebPage webPage = Redis.get("webPage", WebPage.class);
		
		if (webPage!=null) /*只处理Redis中存在webPage的情况*/
		{
			if (webPage.getState()==0) /*还未开始过，必须先初始化*/
			{
				initWebItem(webPage);
			}
			
			if (webPage.getState()==1) /*正在爬取状态，继续即可*/
			{
				continueWebItem();
			}
			
			if (webPage.getState()==9) /*当前页已爬取完毕，取下一页*/
			{
				
			}
		}
	}
	
	
	//继续当前项
	private static void continueWebItem() throws Exception
	{
		WebItem webItem = Redis.get("webItem", WebItem.class);
		
		if (webItem!=null) /*只处理Redis中存在webItem的情况*/
		{
			if (webItem.getState()==1) /*当前项已被成功取出，但还未开始过，必须先初始化*/
			{
				initM3U8(webItem);
			}
			
			if (webItem.getState()==2) /*正在爬取状态，继续即可*/
			{
				continueM3U8();
			}
			
			if (webItem.getState()==9) /*当前项已爬取完毕，取下一项*/
			{
				
			}
		}
		
	}
	
	
	//继续当前M3U8
	private static void continueM3U8() throws Exception
	{
		M3U8 m3u8 = Redis.get("m3u8", M3U8.class);
		
		if (m3u8!=null) /*只处理Redis中存在m3u8的情况*/
		{
			if (m3u8.getState()==0) /*还未开始过，必须先初始化ts列表*/
			{
				initTs(m3u8);
			}
			
			if (m3u8.getState()==1) /*正在爬取状态，继续即可*/
			{
				continueTs(m3u8);
			}
			
			if (m3u8.getState()==9) /*当前m3u8已爬取完毕，取下一个m3u8*/
			{
				
			}
		}
	}
	
	
	//继续当前Ts列表
	private static void continueTs(M3U8 m3u8) throws Exception
	{
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	//初始化当前页
	private static void initWebPage(WebSite webSite) throws Exception
	{
		WebPage webPage = new WebPage();
		
		Document document = JSoup.getHttps(webSite.getUrl());
		
		//获取所有标签页，并分别找出：当前页、上一页、下一页
		Elements pagesElements = document.select("div.box-page li"); 
		
		for(Element pageElement : pagesElements) /*遍历所有页标签*/
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
		
		String webUrl = webSite.getUrl().substring(0, webSite.getUrl().lastIndexOf("/"));
		
		//获取所有项，然后依次保存进WebPage
		Elements itemElements = document.getElementsByClass("col-md-2 col-sm-3 col-xs-4 ");
		
		for(Element itemElement : itemElements)/* 遍历当前页面所有项 */
		{
			WebItem webItem = new WebItem();
			
			String webItemUrl = itemElement.child(0).attr("href");
			
			webItem.setWebItemUrl(webUrl + webItemUrl);
			
			webPage.getWebItemList().add(webItem);
		}
		
		if (webPage.getCurrentPage()!=null && webPage.getWebItemList().size()>0) /*必须保证已成功取到数据*/
		{
			Redis.set("webPage", webPage);
			
			webSite.setState(1);
			
			Redis.set("webSite", webSite);
		}
		else
		{
			throw new Exception("当前页没有取到任何数据，请先检查！" + document.toString());
		}
	}
	
	
	//初始化当前项
	private static void initWebItem(WebPage webPage) throws Exception
	{
		List<WebItem> webItemList = webPage.getWebItemList();
		
		if (webItemList.size()>0) /*必须保证List中有数据，否则直接报错，阻止继续*/
		{
			WebItem webItem = webItemList.get(0);
			
			webItem.setState(1); /*表示当前项已被取出，为多线程而预留*/
			
			Redis.set("webItem", webItem);
			
			webPage.setState(1); /*表示初始化已完毕*/
			
			Redis.set("webPage", webPage);
		}
		else
		{
			throw new Exception("当前页没有任何数据，请先检查！" + webPage.getCurrentPage());
		}
	}
	
	
	//初始化当前M3U8
	private static void initM3U8(WebItem webItem) throws Exception
	{
		String webItemUrl = webItem.getWebItemUrl();
		
		Document document = JSoup.getHttps(webItemUrl);
		
		Elements elements = document.select("div.layout-box script:not([src])");
		
		if (elements!=null && elements.size()>0) /*必须保证元素存在，否则报错*/
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
				
				Redis.set("m3u8", m3u8);
				
				webItem.setState(2); /*表示当前项已成功解析出m3u8网址*/
				
				Redis.set("webItem", webItem);
			}
			else
			{
				System.out.println(document.toString());
				throw new Exception("没有截取到m3u8网址，请先检查！" + webItemUrl);
			}
		}
		else
		{
			System.out.println(document.toString());
			throw new Exception("没有符合条件的<script>标签，请先检查！" + webItemUrl);
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
				
				if (mediaUrl!=null && !mediaUrl.equals("")) /*必须保证媒体播放列表地址存在*/
				{
					initTsParseMaster(m3u8, mediaUrl); return;
				}
				else
				{
					throw new Exception("主播放列表里面的媒体播放列表为空，请先检查！" + m3u8Url);
				}
			}
			else if (line.startsWith("#EXTINF")) /*表明此m3u8是媒体播放列表*/
			{
				//initTsParseMedia(); return;
			}
			else /*其它则略过，并继续寻找*/
			{
				continue;
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
			if (!line.startsWith("#")) /*只要不是#开头的，都是ts地址*/
			{
				String tsAbsoluteUrl = null;
				
				if (line.startsWith("http")) /*ts地址是绝对路径*/
				{
					tsAbsoluteUrl = line;
				}
				else if (line.startsWith("/")) /*ts路径是根路径】*/
				{
					tsAbsoluteUrl = Redis.get("webSite", WebSite.class).getUrl() + line;
				}
				else /*其它都被认为是ts相对路径*/
				{
					int index = mediaUrl.lastIndexOf("/");
					
					tsAbsoluteUrl = mediaUrl.substring(0, index+1) + line;
				}
				
				if (tsAbsoluteUrl!=null) /*必须保证ts地址存在*/
				{
					Ts ts = new Ts();
					ts.setUrl(tsAbsoluteUrl);
					ts.setFileName(tsAbsoluteUrl.substring(tsAbsoluteUrl.lastIndexOf("/")+1));
					ts.setParentFileName(URLEncoder.encode(mediaUrl, "UTF-8"));
					m3u8.getTsList().add(ts);
				}
			}
		}
		
		if (m3u8.getTsList().size()>0) /*必须保证m3u8成功解析出ts列表*/
		{
			m3u8.setState(1);
			
			Redis.set("m3u8", m3u8);
		}
		else
		{
			throw new Exception("当前m3u8文件没有ts，请先检查" + mediaUrl);
		}
	}
}