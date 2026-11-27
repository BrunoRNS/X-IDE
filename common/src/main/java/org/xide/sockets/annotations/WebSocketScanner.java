package org.xide.sockets.annotations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * Classpath scanner (ClassGraph) that finds {@link WebSocketController} classes in a
 * package, sorts them by name, instantiates them with a public no-argument constructor,
 * and returns them ready to be registered in a {@link WebSocketRouter}. Instances cannot
 * be created.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class WebSocketScanner {

	private WebSocketScanner() {
	}

	public static List<Object> scan(String packageName) {
		Objects.requireNonNull(packageName, "packageName");
		if (packageName.isBlank()) {
			throw new IllegalArgumentException("packageName must not be blank");
		}
		List<Object> controllers = new ArrayList<>();

		try (ScanResult scanResult = new ClassGraph().enableClassInfo()
			.enableAnnotationInfo()
			.acceptPackages(packageName)
			.scan()) {

			ClassInfoList classes = scanResult.getClassesWithAnnotation(WebSocketController.class.getName());

			for (ClassInfo classInfo : classes.stream().sorted(Comparator.comparing(ClassInfo::getName)).toList()) {
				Class<?> controllerClass = classInfo.loadClass();
				try {
					controllers.add(controllerClass.getConstructor().newInstance());
				}
				catch (ReflectiveOperationException e) {
					throw new IllegalStateException(
							"WebSocket controller needs a public no-argument constructor: " + controllerClass.getName(),
							e);
				}
			}
		}

		if (controllers.isEmpty()) {
			throw new IllegalStateException("No @WebSocketController classes found in package " + packageName);
		}
		return List.copyOf(controllers);
	}

}
