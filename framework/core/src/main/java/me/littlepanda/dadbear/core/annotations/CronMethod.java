package me.littlepanda.dadbear.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @author 张静波 myplaylife@icloud.com
 * <p>SchedulerService实现类的方法需要标注这个注解</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CronMethod{
	
}
