package org.xide.frame.holders;

import org.xide.frame.LoadingFrame;

/**
 * Owns the single {@link LoadingFrame} instance and controls its lifecycle.
 *
 * @since v1.0.0
 * @author BrunoRNS
 */
public final class LoadingFrameHolder {

	private static volatile LoadingFrame instance;

	private LoadingFrameHolder() {
	}

	public static synchronized void startFrame() {
		FrameHolderSupport.runOnEventDispatchThread(() -> {
			if (instance != null && instance.isDisplayable()) {
				instance.toFront();
				return;
			}

			instance = new LoadingFrame();
			instance.setVisible(true);
		});
	}

	public static synchronized void disposeFrame() {
		FrameHolderSupport.runOnEventDispatchThread(() -> {
			if (instance != null) {
				instance.dispose();
				instance = null;
			}
		});
	}

}
