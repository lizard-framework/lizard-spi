package io.lizardframework.spi;

import io.lizardframework.spi.filter.Filter;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

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

	/**
	 * 模拟并发场景下，线程安全的生成单例对象
	 */
	@Test
	public void test3() throws InterruptedException {
		// 模拟10线程并发
		CyclicBarrier       cyclicBarrier = new CyclicBarrier(10);
		CopyOnWriteArraySet resultSet     = new CopyOnWriteArraySet();

		List<Callable<String>> callableList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			SpiObjCyclicBarrier barrier = new SpiObjCyclicBarrier(cyclicBarrier, resultSet);
			callableList.add(barrier);
		}

		// 主线程等待子线程全部执行完毕后再执行
		ExecutorService pool = Executors.newFixedThreadPool(10);
		pool.invokeAll(callableList);
		pool.shutdown();

		Assert.assertEquals(1, resultSet.size());
	}

	//.........................................//
	static class SpiObjCyclicBarrier implements Callable<String> {

		private CyclicBarrier               cyclicBarrier;
		private CopyOnWriteArraySet<String> resultSet;

		public SpiObjCyclicBarrier(CyclicBarrier cyclicBarrier) {
			this.cyclicBarrier = cyclicBarrier;
		}

		public SpiObjCyclicBarrier(CyclicBarrier cyclicBarrier, CopyOnWriteArraySet<String> resultSet) {
			this.cyclicBarrier = cyclicBarrier;
			this.resultSet = resultSet;
		}

		@Override
		public String call() throws Exception {
			this.cyclicBarrier.await();

			ExtensionLoader<Filter> loader   = ExtensionFactory.getExtensionLoader(Filter.class);
			String                  hashcode = loader.getExtension().doFilter();
			this.resultSet.add(hashcode);

			System.out.println(hashcode);
			return hashcode;
		}
	}
}
