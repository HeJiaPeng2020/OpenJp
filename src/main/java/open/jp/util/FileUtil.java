package open.jp.util;

import java.io.File;

public class FileUtil
{
    public static void main(String[] args)
    {
        String dir = "G:\\计算机系统";
        String oldPrefix = "【脚本】";
        String newPrefix = "【Linux】脚本 ";

        FileUtil.renamePrefix(dir, oldPrefix, newPrefix);
    }


    /*功能：更改文件夹内符合条件的文件名前缀*/
    public static void renamePrefix(String dir,String oldPrefix,String newPrefix)
    {
        if(dir==null || oldPrefix==null || newPrefix==null)//非null校验
        {   return;
        }

        if(dir.equals(""))//非空检验
        {   return;
        }

        File dirFile = new File(dir);
        if(dirFile.exists() && dirFile.isDirectory())//只处理目录
        {   File[] files = dirFile.listFiles();
            if(files!=null && files.length>0)//目录下有内容
            {
                for(File file: files)
                {
                    if(file.isFile())//只处理文件
                    {   String filename = file.getName();
                        if(filename.startsWith(oldPrefix))//只处理前缀匹配的
                        {   String tail = filename.substring(oldPrefix.length());
                            String newFilename = newPrefix + tail;
                            File newFile = new File(dir, newFilename);
                            boolean isSuccess = file.renameTo(newFile);
                            System.out.println(isSuccess ? "true" : "=====================false");
                        }
                    }
                }
            }
        }
    }
}
