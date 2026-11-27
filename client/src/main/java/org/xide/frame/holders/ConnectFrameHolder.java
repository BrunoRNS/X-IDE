package org.xide.frame.holders;

import java.util.concurrent.ExecutionException;

import org.xide.frame.ConnectFrame;

/** Owns the single {@link ConnectFrame} instance and controls its lifecycle. */
public final class ConnectFrameHolder {

	private static volatile ConnectFrame instance;

	private ConnectFrameHolder() {
	}

	public static synchronized void startFrame() {
		FrameHolderSupport.runOnEventDispatchThread(() -> {
			if (instance != null && instance.isDisplayable()) {
				instance.toFront();
				instance.requestFocus();
				return;
			}

			instance = new ConnectFrame();
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

	public static String getTextField() {
		return requireInstance().getTextField();
	}

	public static void setMessage(String message) {
		ConnectFrame frame = instance;
		if (frame != null) {
			frame.setMessage(message);
		}
	}

	public static String awaitEndpoint() throws InterruptedException, ExecutionException {
		return requireInstance().awaitEndpoint();
	}

	public static synchronized void submitEndpoint(String endpoint) {
		FrameHolderSupport.runOnEventDispatchThread(() -> {
			ConnectFrame frame = requireInstance();
			frame.setEndpoint(endpoint);
			frame.dispose();
			instance = null;
		});
	}

	private static ConnectFrame requireInstance() {
		ConnectFrame frame = instance;
		if (frame == null) {
			throw new IllegalStateException("Connect frame is not running");
		}
		return frame;
	}

}
