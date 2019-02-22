package io.lizardframework.spi.filter;

import io.lizardframework.spi.Extensions;

/**
 * <p>无参数 + 单例 实现</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-22
 *     email       job.xueqi@outlook.com
 * </pre>
 */
@Extensions
public class SimpleFilterImpl implements Filter {
	@Override
	public String doFilter() {
		return this.getClass().getName() + " hashCode: " + this.hashCode();
	}
}
