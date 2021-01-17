package open.jp.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class 通用实体
{
    private String 编号;
    private String 状态;
    private String 更新时间;//格式统一：yyyy-MM-dd HH:mm:ss
}
