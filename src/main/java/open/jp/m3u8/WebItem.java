package open.jp.m3u8;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebItem 
{
	private int state;
	
	private String webItemUrl;
	
	private String m3u8Url;
	
	private WebPage webPage;
}