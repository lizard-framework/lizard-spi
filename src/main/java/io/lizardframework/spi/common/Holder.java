package io.lizardframework.spi.common;

/**
 * <p>SPI加载并发锁对象，保证多线程下对于同一个 name 的加载，只有一个线程在执行，保证线程安全</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-18
 *     email       job.xueqi@outlook.com
 * </pre>
 */
public class Holder<T> {

	/**
	 * 存放当前 name 的扩展实现对象，多线程情况下，同时作为锁对象
	 */
	private volatile T value;

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}
}
