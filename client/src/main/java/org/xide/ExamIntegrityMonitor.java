package org.xide;

import java.awt.AWTEvent;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class ExamIntegrityMonitor {

	private static final Path ACTIVE_TTY = Path.of("/sys/class/tty/tty0/active");

	private static final AtomicBoolean VIOLATION_REPORTED = new AtomicBoolean();

	private static final AtomicBoolean STARTED = new AtomicBoolean();

	private static final AtomicBoolean ARMED = new AtomicBoolean(false);

	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "exam-integrity-monitor");
		thread.setDaemon(true);
		return thread;
	});

	private static final AtomicReference<ScheduledFuture<?>> PENDING_VIOLATION = new AtomicReference<>();

	private static final AtomicReference<AWTEventListener> FOCUS_LISTENER = new AtomicReference<>();

	private static final AtomicReference<ScheduledFuture<?>> TTY_MONITOR = new AtomicReference<>();

	private ExamIntegrityMonitor() {
	}

	public static synchronized void start() {
		if (!STARTED.compareAndSet(false, true)) {
			return;
		}
		monitorWindowFocus();
		if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux")) {
			monitorLinuxTty();
		}
	}

	/** Stops monitoring so the client can return to the login screen safely. */
	public static synchronized void stop() {
		if (!STARTED.compareAndSet(true, false)) {
			return;
		}

		AWTEventListener focusListener = FOCUS_LISTENER.getAndSet(null);
		if (focusListener != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(focusListener);
		}

		ScheduledFuture<?> ttyMonitor = TTY_MONITOR.getAndSet(null);
		if (ttyMonitor != null) {
			ttyMonitor.cancel(false);
		}

		cancelPendingViolation();
		ARMED.set(false);
	}

	private static void monitorWindowFocus() {

		Window currentActive = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		if (isClientWindow(currentActive)) {
			ARMED.set(true);
		}

		AWTEventListener windowFocusListener = event -> {
			if (!(event instanceof WindowEvent windowEvent)) {
				return;
			}

			int id = windowEvent.getID();

			if (id == WindowEvent.WINDOW_GAINED_FOCUS || id == WindowEvent.WINDOW_ACTIVATED) {
				if (isClientWindow(windowEvent.getWindow())) {
					ARMED.set(true);
					cancelPendingViolation();
				}
			}

			else if ((id == WindowEvent.WINDOW_LOST_FOCUS || id == WindowEvent.WINDOW_DEACTIVATED) && ARMED.get()) {
				Window opposite = windowEvent.getOppositeWindow();
				if (!isClientWindow(opposite)) {
					scheduleViolation("An X-IDE window lost focus.");
				}
			}
		};

		FOCUS_LISTENER.set(windowFocusListener);
		Toolkit.getDefaultToolkit()
			.addAWTEventListener(windowFocusListener, AWTEvent.WINDOW_FOCUS_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
	}

	private static void scheduleViolation(String reason) {
		// Dá 250ms de tolerância para transições internas de janela ou atrasos do
		// gerenciador de janelas (X11/Wayland)
		ScheduledFuture<?> task = SCHEDULER.schedule(() -> {
			Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
			if (!isClientWindow(active)) {
				endExam(reason);
			}
		}, 250, TimeUnit.MILLISECONDS);

		ScheduledFuture<?> previous = PENDING_VIOLATION.getAndSet(task);
		if (previous != null) {
			previous.cancel(false);
		}
	}

	private static void cancelPendingViolation() {
		ScheduledFuture<?> pending = PENDING_VIOLATION.getAndSet(null);
		if (pending != null) {
			pending.cancel(false);
		}
	}

	private static void monitorLinuxTty() {
		String initialTty = readActiveTty();
		if (initialTty == null) {
			System.err.println("TTY monitoring is unavailable; window focus monitoring remains active.");
			return;
		}
		TTY_MONITOR.set(SCHEDULER.scheduleAtFixedRate(() -> {
			String activeTty = readActiveTty();
			if (activeTty != null && !initialTty.equals(activeTty)) {
				endExam("The active Linux TTY changed during the exam.");
			}
		}, 100, 100, TimeUnit.MILLISECONDS));
	}

	private static String readActiveTty() {
		try {
			String activeTty = Files.readString(ACTIVE_TTY).trim();
			return activeTty.isEmpty() ? null : activeTty;
		}
		catch (IOException ignored) {
			return null;
		}
	}

	private static boolean isClientWindow(Window window) {
		for (Window current = window; current != null; current = current.getOwner()) {
			if (current.getClass().getName().startsWith("org.xide.")) {
				return true;
			}
		}
		return false;
	}

	private static void endExam(String reason) {
		if (VIOLATION_REPORTED.compareAndSet(false, true)) {
			System.err.println("Exam ended: " + reason);
			System.exit(3);
		}
	}

}
