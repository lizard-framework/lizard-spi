package io.lizardframework.spi;

/**
 * <p>扩展点加载器工厂</p>
 * <pre>
 *     author      XueQi
 *     date        2019-02-18
 *     email       job.xueqi@outlook.com
 * </pre>
 */
public class ExtensionFactory {
	private ExtensionFactory() {
	}

	private static class HOLDER {
		private static final ExtensionFactory INSTANCE = new ExtensionFactory();
	}

	public static ExtensionFactory getInstance() {
		return HOLDER.INSTANCE;
	}
}
