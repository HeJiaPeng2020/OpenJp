package open.jp.m3u8;

public class NotFoundException extends Exception
{
    public NotFoundException()
    {
    }

    public NotFoundException(String message)
    {   super(message);
    }
}
