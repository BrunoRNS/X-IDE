package org.xide;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Best-effort local check used to enforce the exam client's environment policy.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class VirtualMachineDetector {

	private VirtualMachineDetector() {
	}

	static boolean isVirtualMachine() {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (os.contains("linux")) {
			return linuxVirtualMachine();
		}
		if (os.contains("mac")) {
			return containsVirtualizationMarker(readCommand("sysctl", "-n", "hw.model"));
		}
		if (os.contains("windows")) {
			return containsVirtualizationMarker(readCommand("powershell.exe", "-NoProfile", "-NonInteractive",
					"-Command",
					"(Get-CimInstance Win32_ComputerSystem | ForEach-Object { $_.Manufacturer + ' ' + $_.Model })"));
		}
		return false;
	}

	private static boolean linuxVirtualMachine() {
		String virtualization = readCommand("systemd-detect-virt");
		if (!virtualization.isBlank() && !virtualization.equalsIgnoreCase("none")) {
			return true;
		}
		for (Path path : new Path[] { Path.of("/sys/class/dmi/id/sys_vendor"),
				Path.of("/sys/class/dmi/id/product_name"), Path.of("/proc/cpuinfo") }) {
			try {
				if (containsVirtualizationMarker(Files.readString(path))) {
					return true;
				}
			}
			catch (IOException ignored) {
				// Some Linux systems do not expose DMI or CPU information to regular
				// users.
			}
		}
		return false;
	}

	private static String readCommand(String... command) {
		try {
			Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
			boolean finished = process.waitFor(2, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return "";
			}
			if (process.exitValue() != 0) {
				return "";
			}
			return new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
		}
		catch (IOException ignored) {
			return "";
		}
		catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
			return "";
		}
	}

	private static boolean containsVirtualizationMarker(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		return normalized.contains("virtualbox") || normalized.contains("vmware") || normalized.contains("kvm")
				|| normalized.contains("qemu") || normalized.contains("xen") || normalized.contains("parallels")
				|| normalized.contains("virtual machine") || normalized.contains("virtualmachine")
				|| normalized.contains("hyper-v") || normalized.contains("hyperv") || normalized.contains("hypervisor")
				|| normalized.contains(" bhyve") || normalized.contains("virtualmac");
	}

}
