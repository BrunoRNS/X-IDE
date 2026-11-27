package org.xide.frame.holders;

import org.xide.frame.MainFrame;

/** Owns the single {@link MainFrame} instance and controls its lifecycle. */
public final class MainFrameHolder {

	private static volatile MainFrame instance;

	private MainFrameHolder() {
	}

	public static synchronized void startFrame() {
		FrameHolderSupport.runOnEventDispatchThread(() -> {
			if (instance != null && instance.isDisplayable()) {
				instance.toFront();
				instance.requestFocus();
				return;
			}

			instance = new MainFrame();
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
