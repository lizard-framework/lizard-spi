package io.lizardframework.spi.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * <p></p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-18
 *     email       job.xueqi@outlook.com
 * </pre>
 */
public class ClassUtils {
	/**
	 * 实例化一个对象(根据参数自动检测构造方法）
	 *
	 * @param clazz    对象类
	 * @param argTypes 构造函数需要的参数
	 * @param args     构造函数需要的参数
	 * @param <T>      对象具体类
	 * @return 对象实例
	 * @throws IllegalStateException 没有找到方法，或者无法处理，或者初始化方法异常等
	 */
	public static <T> T newInstanceWithArgs(Class<T> clazz, Class<?>[] argTypes, Object[] args) {
		if (argTypes == null || argTypes.length == 0) {
			return newInstance(clazz);
		}
		try {
			if (!(clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()))) {
				Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes);
				constructor.setAccessible(true);
				return constructor.newInstance(args);
			} else {
				Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
				if (constructors == null || constructors.length == 0) {
					throw new IllegalStateException("The " + clazz.getCanonicalName()
							+ " has no constructor with argTypes :" + Arrays.toString(argTypes));
				}
				Constructor<T> constructor = null;
				for (Constructor<T> c : constructors) {
					Class[] ps = c.getParameterTypes();
					if (ps.length == argTypes.length + 1) { // 长度多一
						boolean allMath = true;
						for (int i = 1; i < ps.length; i++) { // 而且第二个开始的参数类型匹配
							if (ps[i] != argTypes[i - 1]) {
								allMath = false;
								break;
							}
						}
						if (allMath) {
							constructor = c;
							break;
						}
					}
				}
				if (constructor == null) {
					throw new IllegalStateException("The " + clazz.getCanonicalName()
							+ " has no constructor with argTypes :" + Arrays.toString(argTypes));
				} else {
					constructor.setAccessible(true);
					Object[] newArgs = new Object[args.length + 1];
					System.arraycopy(args, 0, newArgs, 1, args.length);
					return constructor.newInstance(newArgs);
				}
			}
		} catch (IllegalStateException e) {
			throw e;
		} catch (Throwable e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * 实例化一个对象(只检测默认构造函数，其它不管）
	 *
	 * @param clazz 对象类
	 * @param <T>   对象具体类
	 * @return 对象实例
	 * @throws IllegalStateException 没有找到方法，或者无法处理，或者初始化方法异常等
	 */
	public static <T> T newInstance(Class<T> clazz) {
		if (clazz.isPrimitive()) {
			return (T) getDefaultPrimitiveValue(clazz);
		}

		T t = getDefaultWrapperValue(clazz);
		if (t != null) {
			return t;
		}

		try {
			// 普通类，如果是成员类（需要多传一个父类参数）
			if (!(clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()))) {
				try {
					// 先找一个空的构造函数
					Constructor<T> constructor = clazz.getDeclaredConstructor();
					constructor.setAccessible(true);
					return constructor.newInstance();
				} catch (Exception ignore) { // NOPMD
				}
			}
			// 不行的话，找一个最少参数的构造函数
			Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
			if (constructors == null || constructors.length == 0) {
				throw new IllegalStateException("The " + clazz.getCanonicalName()
						+ " has no default constructor!");
			}
			Constructor<T> constructor = constructors[0];
			if (constructor.getParameterTypes().length > 0) {
				for (Constructor<T> c : constructors) {
					if (c.getParameterTypes().length < constructor.getParameterTypes().length) {
						constructor = c;
						if (constructor.getParameterTypes().length == 0) {
							break;
						}
					}
				}
			}
			constructor.setAccessible(true);
			// 虚拟构造函数的参数值，基本类型使用默认值，其它类型使用null
			Class<?>[] argTypes = constructor.getParameterTypes();
			Object[]   args     = new Object[argTypes.length];
			for (int i = 0; i < args.length; i++) {
				args[i] = getDefaultPrimitiveValue(argTypes[i]);
			}
			return constructor.newInstance(args);
		} catch (IllegalStateException e) {
			throw e;
		} catch (Throwable e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * 得到基本类型的默认值
	 *
	 * @param clazz Class类
	 * @return 默认值
	 */
	public static Object getDefaultPrimitiveValue(Class clazz) {
		if (clazz == int.class) {
			return 0;
		} else if (clazz == boolean.class) {
			return false;
		} else if (clazz == long.class) {
			return 0L;
		} else if (clazz == byte.class) {
			return (byte) 0;
		} else if (clazz == double.class) {
			return 0d;
		} else if (clazz == short.class) {
			return (short) 0;
		} else if (clazz == float.class) {
			return 0f;
		} else if (clazz == char.class) {
			return (char) 0;
		} else {
			return null;
		}
	}

	/**
	 * 得到包装类的默认值
	 *
	 * @param clazz Class类
	 * @return 默认值
	 */
	public static <T> T getDefaultWrapperValue(Class<T> clazz) {
		if (clazz == Short.class) {
			return (T) Short.valueOf((short) 0);
		} else if (clazz == Integer.class) {
			return (T) Integer.valueOf(0);
		} else if (clazz == Long.class) {
			return (T) Long.valueOf(0L);
		} else if (clazz == Double.class) {
			return (T) Double.valueOf(0d);
		} else if (clazz == Float.class) {
			return (T) Float.valueOf(0f);
		} else if (clazz == Byte.class) {
			return (T) Byte.valueOf((byte) 0);
		} else if (clazz == Character.class) {
			return (T) Character.valueOf((char) 0);
		} else if (clazz == Boolean.class) {
			return (T) Boolean.FALSE;
		}
		return null;
	}
}
