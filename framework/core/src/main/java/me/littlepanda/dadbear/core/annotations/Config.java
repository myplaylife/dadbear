package me.littlepanda.dadbear.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 张静波 myplaylife@icloud.com
 * <p>如果服务需要额外的配置参数，可以在实现类中加一个Configuration对象，并加上这个注解，运行时会把相应的配置注入到这个这个属性中</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Config {

}
