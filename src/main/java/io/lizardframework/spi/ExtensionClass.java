package io.lizardframework.spi;

import io.lizardframework.spi.common.ClassUtils;

/**
 * <p>扩展接口实现类包装</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-19
 *     email       job.xueqi@outlook.com
 * </pre>
 */
public class ExtensionClass<T> {

	/**
	 * 接口T具体实现类
	 */
	private          Class<? extends T> clazz;
	/**
	 * 实现类的名称
	 */
	private          String             name;
	/**
	 * 实现类的Extensions注解
	 */
	private          Extensions         extensions;
	/**
	 * 当扩展类指定为单例时，保留创建的单例对象
	 */
	private volatile T                  instance;

	public ExtensionClass(Class<? extends T> clazz, String name, Extensions extensions) {
		this.clazz = clazz;
		this.name = name;
		this.extensions = extensions;
	}

	/**
	 * 获取实例对象
	 *
	 * @param argTypes
	 * @param args
	 * @return
	 */
	public T getInstance(Class[] argTypes, Object[] args) {
		if (clazz != null) {
			if (extensions.singleton()) {
				// 创建单例对象
				if (instance == null) {
					synchronized (this) {
						if (instance == null) {
							instance = ClassUtils.newInstanceWithArgs(clazz, argTypes, args);
						}
					}
				}
				return instance;
			} else {
				// 直接创建对象返回
				return ClassUtils.newInstanceWithArgs(clazz, argTypes, args);
			}
		}
		throw new IllegalStateException("class of ExtensionClass is null");
	}

	public Class<? extends T> getClazz() {
		return clazz;
	}

	public String getName() {
		return name;
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public T getInstance() {
		return instance;
	}

}
