package io.lizardframework.spi;

import io.lizardframework.spi.common.ClassLoaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>扩展点加载器</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-18
 *     email       job.xueqi@outlook.com
 * </pre>
 */
public class ExtensionLoader<T> {

	// 扩展点加载器对应的接口类型
	private final Class<T>                       type;
	// 异常列表
	private final Map<String, Exception>         exceptions            = new ConcurrentHashMap<>();
	// 缓存扩展点 name 和 包装类 映射
	private final Map<String, ExtensionClass<T>> CACHE_EXTENSION_CLAZZ = new ConcurrentHashMap<>();

	// 默认字符集
	private static final String DEFAULT_CHARSET           = "UTF-8";
	// SPI路径
	private static final String SERVICR_DIRECTORY         = "META-INF/lizard/services/";
	private static final String LIZARD_INTERNAL_DIRECTORY = "META-INF/lizard/internal/";

	public ExtensionLoader(Class<T> type) {
		this.type = type;
		this.isExtensionAnnontation(this.type);
		this.loadExtensionClasses();
	}

	/**
	 * 获取扩展点实现
	 *
	 * @param name
	 * @param argTypes
	 * @param args
	 * @return
	 */
	public T getExtension(String name, Class[] argTypes, Object[] args) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Extension name is null");

		return this.createExtension(name, argTypes, args);
	}

	/**
	 * 获取默认的扩展点实现
	 *
	 * @return
	 */
	public T getExtension() {
		SPI    spi         = this.type.getAnnotation(SPI.class);
		String defaultName = spi.value();
		if (defaultName == null || defaultName.length() == 0) {
			throw new IllegalArgumentException("Extension type(" + this.type + ") default implement name is null");
		}
		return this.getExtension(defaultName, null, null);
	}

	/**
	 * 创建扩展点实现，需要注意单例扩展点和 prototype 扩展点
	 *
	 * @param name
	 * @param argTypes
	 * @param args
	 * @return
	 */
	private T createExtension(String name, Class[] argTypes, Object[] args) {
		// 获取ExtensionClass，从中获取实例
		ExtensionClass<T> extensionClass = CACHE_EXTENSION_CLAZZ.get(name);
		if (extensionClass == null) {
			throw new IllegalStateException("Error when load extension type(" + this.type + ", name:" + name + ") class is null");
		}

		return extensionClass.getInstance(argTypes, args);
	}

	/**
	 * 装载该接口的SPI扩展实现和 name 的缓存
	 *
	 * @return
	 */
	private synchronized void loadExtensionClasses() {
		this.loadFiles(SERVICR_DIRECTORY);
		this.loadFiles(LIZARD_INTERNAL_DIRECTORY);
	}

	/**
	 * 装载SPI描述文件
	 *
	 * @param dir
	 */
	private void loadFiles(String dir) {
		// 获取类型扩展点路径
		String fileName = dir + this.type.getName();

		try {
			// 获取ClassLoader，并装载配置文件
			ClassLoader      classLoader = ClassLoaderUtils.getCurrentClassLoader();
			Enumeration<URL> urls        = classLoader != null ? classLoader.getResources(fileName) : ClassLoader.getSystemResources(fileName);

			if (urls != null) {
				while (urls.hasMoreElements()) {
					URL    url  = urls.nextElement();
					String line = null;
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), DEFAULT_CHARSET))) {
						while ((line = reader.readLine()) != null) {
							line = line.trim();
							// # 开头为注释，直接忽略
							if (line.startsWith("#")) continue;

							// name=实现接口
							int    equalIndex = line.indexOf("=");
							String name       = null;
							String clazzName  = null;
							// 获取 name 和实现类名
							if (equalIndex > 0) {
								name = line.substring(0, equalIndex).trim();
								clazzName = line.substring(equalIndex + 1).trim();
							}
							// 如果实现类不为空
							if (clazzName != null && clazzName.length() > 0) {
								// 第二个参数 true 初始化，执行 static 块
								Class<?> clazz = Class.forName(clazzName, true, classLoader);

								// 如果当前配置的实现类不是 type 的实现，抛出异常
								if (!this.type.isAssignableFrom(clazz)) {
									throw new IllegalStateException("Error when load extension class(interface: " +
											this.type + ", class line: " + clazz.getName() + "), class " + clazz.getName() +
											" is not subtype of interface");
								}

								// 判断实现类是否标有Extensions注解
								Extensions extensions = clazz.getAnnotation(Extensions.class);
								if (extensions == null) {
									throw new IllegalStateException("Error when load extension class(interface: " +
											this.type + ", class line: " + clazz.getName() + "), class " + clazz.getName() +
											" without @" + Extensions.class + " Annontation");
								}

								// 通过 name 获取是否已经装载过同名的扩展配置类
								ExtensionClass<T> alreadyClazz = CACHE_EXTENSION_CLAZZ.get(name);
								if (alreadyClazz == null) {
									CACHE_EXTENSION_CLAZZ.put(name, new ExtensionClass(clazz, name, extensions));
								} else {
									throw new IllegalStateException("Duplicate extension " + this.type.getName() + " name: " + name
											+ " on: " + alreadyClazz.getClazz() + " and: " + clazz.getName() + " in: " + fileName);
								}
							}
						}
					} catch (Exception cause) {
						// 将异常写入 map，当获取 clazz 为空时，就将异常输出
						exceptions.put(line, cause);
					}
				}
			}
		} catch (IOException e) {
			//todo logger
		}
	}


	/**
	 * 判断当前扩展点类型的接口是否有SPI注解
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	public <T> void isExtensionAnnontation(Class<T> type) {
		boolean isAnnon = type.isAnnotationPresent(SPI.class);
		if (!isAnnon)
			throw new IllegalArgumentException("Extension type(" + type + ") is not extension, because without @" +
					SPI.class.getSimpleName() + " Annontation");
	}
}
