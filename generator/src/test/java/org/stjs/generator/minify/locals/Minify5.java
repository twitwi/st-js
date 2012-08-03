package org.stjs.generator.minify.locals;

public class Minify5 {

	interface Handler {
		public void handle(String name, int value);
	}

	private static Handler handler = new Handler() {
		@Override
		public void handle(String nameInner, int valueInner) {
			String str = nameInner + " : " + valueInner;
		}
	};
}
