package open.jp.m3u8;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebPage 
{
	private int state;
	
	private String currentPage;
	private String nextPage;
	private String previousPage;
	
	private List<WebItem> webItemList = new ArrayList<WebItem>();
}