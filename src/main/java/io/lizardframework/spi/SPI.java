package io.lizardframework.spi;

import java.lang.annotation.*;

/**
 * <p>扩展点接口标示</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-18
 *     email       job.xueqi@outlook.com
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {

	/**
	 * 缺省扩展点名称
	 *
	 * @return
	 */
	String value() default "default";

}
