package open.jp.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Data
@MappedSuperclass
public class CommonEntity
{
	@Id
	@GenericGenerator(name="uuidGenerator", strategy="uuid")
	@GeneratedValue(generator="uuidGenerator")
	private String id;
	private String state;
	private String lastTime;
}