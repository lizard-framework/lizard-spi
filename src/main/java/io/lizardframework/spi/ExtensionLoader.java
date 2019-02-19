package io.lizardframework.spi;

import io.lizardframework.spi.common.ClassLoaderUtils;
import io.lizardframework.spi.common.ClassUtils;
import io.lizardframework.spi.common.Holder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
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
	private final Class<T>               type;
	// 扩展类加载器工厂
	private final ExtensionFactory       objectFactory;
	// 异常列表
	private final Map<String, Exception> exceptions = new ConcurrentHashMap<>();

	// 缓存扩展点 name 和 实现类 映射
	private final        Holder<Map<String, Class<?>>>     CACHE_CLASSES       = new Holder<>();
	// 缓存单例扩展实现 name 和 object 映射
	private final        Map<String, Object>               CACHE_SINGLETON_OBJ = new ConcurrentHashMap<>();
	// 存放扩展点类型和加载器的缓存
	private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS   = new ConcurrentHashMap<>();

	// 默认字符集
	private static final String DEFAULT_CHARSET           = "UTF-8";
	// SPI路径
	private static final String SERVICR_DIRECTORY         = "META-INF/lizard/services/";
	private static final String LIZARD_INTERNAL_DIRECTORY = "META-INF/dubbo/internal/";

	public ExtensionLoader(Class<T> type) {
		this.type = type;
		this.objectFactory = ExtensionFactory.getInstance();
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
		// 获取 name 对应的实现类
		Class<?> clazz = this.getExtensionClasses().get(name);
		if (clazz == null) {
			// 输出异常信息 findException()
			return null;
		}

		// 针对单例模式的 class，需要保证线程安全
		Extensions extensions = clazz.getAnnotation(Extensions.class);
		boolean    singleton  = extensions.singleton();
		if (singleton) {
			Object extensionObject = CACHE_SINGLETON_OBJ.get(name);
			if (extensionObject == null) {
				synchronized (CACHE_SINGLETON_OBJ) {
					extensionObject = CACHE_SINGLETON_OBJ.get(name);
					if (extensionObject == null) {
						extensionObject = ClassUtils.newInstanceWithArgs(clazz, argTypes, args);
						CACHE_SINGLETON_OBJ.put(name, extensionObject);
					}
				}
			}
			return (T) extensionObject;
		} else {
			return (T) ClassUtils.newInstanceWithArgs(clazz, argTypes, args);
		}
	}

	/**
	 * 创建扩展点实现对象
	 *
	 * @param clazz
	 * @param argTypes
	 * @param args
	 * @return
	 */
	private T createExtensionsObject(Class<?> clazz, Class[] argTypes, Object[] args) {
		return null;
	}

	/**
	 * 获取接口所有的扩展点SPI配置的 name 和 class
	 *
	 * @return
	 */
	private Map<String, Class<?>> getExtensionClasses() {
		Map<String, Class<?>> classes = CACHE_CLASSES.get();
		if (classes == null) {
			synchronized (CACHE_CLASSES) {   // 此处为什么做线程同步？可能有多个线程通过getExtensionLoader获取到了该接口的同一个ExtensionLoader对象，但是初始化的过程，只能有一个线程进行
				classes = CACHE_CLASSES.get();
				if (classes == null) {
					classes = this.loadExtensionClasses();
					CACHE_CLASSES.set(classes);
				}
			}
		}
		return classes;
	}

	/**
	 * 装载该接口的SPI扩展实现和 name 的缓存, 该方法已经在getExtensionClasses中同步过
	 *
	 * @return
	 */
	private Map<String, Class<?>> loadExtensionClasses() {
		Map<String, Class<?>> extensionClasses = new HashMap<>();
		this.loadFiles(extensionClasses, SERVICR_DIRECTORY);
		this.loadFiles(extensionClasses, LIZARD_INTERNAL_DIRECTORY);

		return extensionClasses;
	}

	/**
	 * 装载SPI描述文件
	 *
	 * @param extensionClasses
	 * @param dir
	 */
	private void loadFiles(Map<String, Class<?>> extensionClasses, String dir) {
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

								// 通过 name 获取是否已经装载过同名的扩展配置
								Class<?> alreadyClazz = extensionClasses.get(name);
								if (alreadyClazz == null) {
									extensionClasses.put(name, clazz);
								} else {
									throw new IllegalStateException("Duplicate extension " + this.type.getName() + " name: " + name
											+ " on: " + alreadyClazz.getName() + " and: " + clazz.getName() + " in: " + fileName);
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
	 * 获取扩展点加载器
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
		if (type == null)
			throw new IllegalArgumentException("Extension type is null");
		if (!type.isInterface())
			throw new IllegalArgumentException("Extension type(" + type + ") not interface");
		if (!isExtensionAnnontation(type))
			throw new IllegalArgumentException("Extension type(" + type + ") is not extension, because without @" +
					SPI.class.getSimpleName() + " Annontation");

		ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		if (loader == null) {
			EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
			loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		}
		return loader;
	}

	/**
	 * 判断当前扩展点类型的接口是否有SPI注解
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> boolean isExtensionAnnontation(Class<T> type) {
		return type.isAnnotationPresent(SPI.class);
	}
}
