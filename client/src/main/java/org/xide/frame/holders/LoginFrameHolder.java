package org.xide.frame.holders;

import org.xide.frame.LoginFrame;

/** Owns the single {@link LoginFrame} instance and controls its lifecycle. */
public final class LoginFrameHolder {

	private static volatile LoginFrame instance;

	private LoginFrameHolder() {
	}

	public static synchronized void startFrame() {
		FrameHolderSupport.runOnEventDispatchThread(() -> {
			if (instance != null && instance.isDisplayable()) {
				instance.toFront();
				instance.requestFocus();
				return;
			}

			instance = new LoginFrame();
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

	public static String getEmail() {
		return requireInstance().getEmail();
	}

	public static String getPassword() {
		return requireInstance().getPassword();
	}

	public static void setMessage(String message) {
		LoginFrame frame = instance;
		if (frame != null) {
			frame.setMessage(message);
		}
	}

	private static LoginFrame requireInstance() {
		LoginFrame frame = instance;
		if (frame == null) {
			throw new IllegalStateException("Login frame is not running");
		}
		return frame;
	}

}
