
package org.ibp.api.domain.common;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * A helper test to test getter and setter solely for reducing noise in the test coverage.
 *
 */
public class TestGetterAndSetter {

	private static final String REGEX_ALL = ".*";
	private static final String REGEX_ESCAPE = "\\";
	final PodamFactory factory = new PodamFactoryImpl();

	@Test
	public void testBmsApiGetterAndSetter() throws Exception {
		final List<Class<? extends Object>> findAllPojosInPackage = findAllPojosInPackage("org.ibp.api");
		for (final Class<? extends Object> class1 : findAllPojosInPackage) {
			testClassesForCodeCoverage(class1);
		}
	}

	private Set<Class<? extends Object>> getAllClasses(final String packageName) throws Exception {
		final Set<Class<? extends Object>> allClasses = new HashSet<Class<? extends Object>>();

		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		final URL root = contextClassLoader.getResource(packageName.replace(".", "/"));

		// Look for all Java files under Project_Directory/target/classes/
		final String rootFolder = root.getFile().replace("test-classes", "classes");
		final Collection<File> files = FileUtils.listFiles(new File(rootFolder), new String[] {"class"}, true);
		// Find classes implementing ICommand.
		for (final File file : files) {

			final String className = getPackageNameFromFilePath(file);
			if (className.contains("$")) {
				continue;
			}

			// Need to load this class dynamically and thus call below.
			contextClassLoader.getResource(className);
			final Class<?> cls = Class.forName(className);
			if (Object.class.isAssignableFrom(cls)) {
				allClasses.add(cls);
			}
		}
		return allClasses;
	}

	private String getPackageNameFromFilePath(final File file) {
		// Remove everything from before Project_Directory/target/classes/ and replaces all / with .
		String packageRelatedFileSuffix =
				file.getAbsolutePath().replaceAll(
						REGEX_ALL + "target" + REGEX_ESCAPE + File.separator + "classes" + REGEX_ESCAPE + File.separator, "");
		return packageRelatedFileSuffix.replace(File.separator, ".").replace(".class", "");
	}

	private void testClassesForCodeCoverage(final Class<?> klass) throws Exception, Exception {
		final Object source = factory.manufacturePojo(klass);

		Assert.assertNotEquals(source, new Object());

		final Method toStringMethod = getMethodIfItExists(klass, "toString");
		if (toStringMethod != null) {
			toStringMethod.invoke(source);
		}

		final Method equalsMethod = getMethodIfItExists(klass, "equals", Object.class);

		final Object destination = klass.newInstance();
		privateCopy(destination, source);

		if (equalsMethod != null) {
			Assert.assertTrue((Boolean) equalsMethod.invoke(source, destination));
			Assert.assertEquals(source.hashCode(), destination.hashCode());
		}

	}

	private Method getMethodIfItExists(final Class<?> klass, final String name, final Class<?>... parameterTypes) {
		try {
			return klass.getDeclaredMethod(name, parameterTypes);
		} catch (final Exception e) {
			// This is only there because if the method does not exist we want to carry on.
			// Do not need to log anything as this is not an erroneous case
		}
		// return null if the method does not exist
		return null;
	}

	private void privateCopy(final Object destination, final Object source) throws Exception {
		final Field[] fields = source.getClass().getDeclaredFields();
		for (final Field field : fields) {

			if (PropertyUtils.isWriteable(source, field.getName())) {
				BeanUtils.setProperty(destination, field.getName(), PropertyUtils.getProperty(source, field.getName()));
			} else {

				if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				field.setAccessible(true);
				field.set(destination, field.get(source));
			}
		}
	}

	private List<Class<? extends Object>> findAllPojosInPackage(final String packageName) throws Exception {
		final Set<Class<? extends Object>> allClasses = getAllClasses(packageName);
		return ReflectionPojoUtilities.getAllPojos(allClasses);
	}

}
