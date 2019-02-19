package io.lizardframework.spi;

import java.lang.annotation.*;

/**
 * <p>扩展SPI实现注解</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-18
 *     email       job.xueqi@outlook.com
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extensions {

	/**
	 * 是否单例模式，默认为 true
	 *
	 * @return
	 */
	boolean singleton() default true;

}
