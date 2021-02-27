package open.jp.m3u8;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Ts 
{
	private int state;
	
	private String url;
	
	private String fileName;
	
	private String parentFileName;
	
	private String rootFilePath = "F:\\_m3u8_download\\";
}