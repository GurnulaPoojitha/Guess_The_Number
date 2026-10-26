package com.guessword;

import com.guessword.service.GameServiceTest;
import com.guessword.service.PasswordHasherTest;
import com.guessword.service.ReportServiceTest;
import com.guessword.service.UserServiceTest;
import com.guessword.service.ValidationRulesTest;
import com.guessword.service.WordEvaluatorTest;
import com.guessword.service.WordServiceTest;
import com.guessword.ui.BoardPanelTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Runs every method marked {@link Test} in the classes below. A fresh instance of the class is
 * created for each test, like JUnit does. Exits with code 1 if anything fails.
 */
public final class TestRunner {

    private static final List<Class<?>> TEST_CLASSES = List.of(
            WordEvaluatorTest.class,
            ValidationRulesTest.class,
            PasswordHasherTest.class,
            UserServiceTest.class,
            GameServiceTest.class,
            ReportServiceTest.class,
            WordServiceTest.class,
            BoardPanelTest.class);

    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        int passed = 0;
        List<String> failures = new ArrayList<>();

        for (Class<?> testClass : TEST_CLASSES) {
            Method[] methods = testClass.getDeclaredMethods();
            Arrays.sort(methods, Comparator.comparing(Method::getName));
            int classPassed = 0;
            int classTotal = 0;
            for (Method method : methods) {
                if (!method.isAnnotationPresent(Test.class)) {
                    continue;
                }
                classTotal++;
                Object instance = testClass.getDeclaredConstructor().newInstance();
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                    classPassed++;
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    failures.add(testClass.getSimpleName() + "." + method.getName() + ": " + cause);
                } finally {
                    if (instance instanceof AutoCloseable closeable) {
                        closeable.close();
                    }
                }
            }
            passed += classPassed;
            System.out.printf("%-22s %3d / %3d passed%n", testClass.getSimpleName(), classPassed, classTotal);
        }

        System.out.println();
        if (failures.isEmpty()) {
            System.out.println("ALL " + passed + " TESTS PASSED");
        } else {
            System.out.println(failures.size() + " FAILED, " + passed + " passed:");
            failures.forEach(f -> System.out.println("  FAIL " + f));
            System.exit(1);
        }
    }
}
