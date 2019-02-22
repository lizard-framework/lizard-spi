package io.lizardframework.spi;

import io.lizardframework.spi.filter.Filter;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>测试用例</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-22
 *     email       job.xueqi@outlook.com
 * </pre>
 */
public class ExtensionTest {

	/**
	 * 单线程 + 单例实现
	 */
	@Test
	public void test1() {
		ExtensionLoader<Filter> loader = ExtensionFactory.getExtensionLoader(Filter.class);
		System.out.println(loader.getExtension().doFilter());
	}

	/**
	 * 单线程 + 循环 + 单例
	 */
	@Test
	public void test2() {
		Set<String> resultSet = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			ExtensionLoader<Filter> loader = ExtensionFactory.getExtensionLoader(Filter.class);
			resultSet.add(loader.getExtension().doFilter());
		}

		Assert.assertEquals(1, resultSet.size());
	}

}
