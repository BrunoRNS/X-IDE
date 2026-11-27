package org.xide.frame.holders;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/** Shared helpers for the frame holders. */
final class FrameHolderSupport {

	private FrameHolderSupport() {
	}

	static void runOnEventDispatchThread(Runnable action) {
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
			return;
		}

		try {
			SwingUtilities.invokeAndWait(action);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while updating the UI", exception);
		}
		catch (InvocationTargetException exception) {
			throw new IllegalStateException("Could not update the UI", exception.getCause());
		}
	}

}
