package io.lizardframework.spi.filter;

import io.lizardframework.spi.SPI;

/**
 * <p>SPI Interface </p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-22
 *     email       job.xueqi@outlook.com
 * </pre>
 */
@SPI
public interface Filter {

	/**
	 * 模拟执行方法
	 */
	String doFilter();

}
