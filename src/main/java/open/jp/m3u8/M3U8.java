package open.jp.m3u8;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class M3U8 
{
	private int state;
	
	private String url;
	
	private List<Ts> tsList = new ArrayList<Ts>();
	
	private WebItem webItem;
}