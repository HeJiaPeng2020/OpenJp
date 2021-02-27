package open.jp;

import java.io.InputStream;
import java.io.OutputStream;

public class File
{
	public static void main(String[] args)
	{
		String dir = "";
		String oldPrefix = "";
		String newPrefix = "";
		
		open.jp.File.renamePrefix(dir, oldPrefix, newPrefix);
	}
	
	public static void renamePrefix(String dir, String oldPrefix, String newPrefix)
	{
		if(dir==null || oldPrefix==null || newPrefix==null)//非null校验
        {   return;
        }
		
		if(dir.equals(""))//非空检验
        {   return;
        }
		
		java.io.File dirFile = new java.io.File(dir);
		if(dirFile.exists() && dirFile.isDirectory())//只处理目录
		{
			java.io.File[] files = dirFile.listFiles();
			if(files!=null && files.length>0)//要求目录下有内容
			{	
				for(java.io.File file : files)
				{	
					if(file!=null && file.isFile())//只处理文件
					{	
						String filename = file.getName();
						if(filename.startsWith(oldPrefix))//满足前缀
						{	
							String newFilename = newPrefix + filename.substring(oldPrefix.length());
							java.io.File newFile = new java.io.File(dir, newFilename);
							boolean isSuccess = file.renameTo(newFile);
							if(isSuccess)//如果重命名成功
							{	System.out.print("1");
							}
							else
							{	System.out.println("出错了！");
							}
						}
					}
				}
			}
		}
	}
	
	//写入文件
	public static void write(InputStream inputStream, OutputStream outputStream) throws Exception
	{
		byte[] bytes = new byte[1024];
		
		int readLength = 0;
		
		while((readLength=inputStream.read(bytes)) != -1) /*-1表示已读完*/
		{
			outputStream.write(bytes, 0, readLength);
		}
	}
}
