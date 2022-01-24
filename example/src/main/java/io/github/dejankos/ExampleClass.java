package io.github.dejankos;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Example class
 */
public class ExampleClass implements ExampleInterface {

    /**
     * Constructor
     */
    public ExampleClass() {
    }

    @Override
    public int methodA() {
        return 42;
    }

    /**
     * Some public method
     * <jdoctest>
     * <pre>
     *     {@code
     *     int a = 5;
     *     String b = "abc";
     *     String res = a + "" + b;
     *     System.out.println(res);
     *     }
     *     Other example
     *     {@code
     *     System.out.println(new ExampleClass());
     *     }
     * </pre>
     * </jdoctest>
     *
     * @param a first
     * @param b second
     */
    public int publicMethod(int a, String... b) {
        System.out.println("a = " + a);
        System.out.println("b = " + Arrays.toString(b));
        new HashMap<String, String>();
        return a;
    }

    /**
     * Some public static method
     *
     * @return time
     */
    public static long staticMethod() {
        return System.currentTimeMillis();
    }

    // some private method
    private void privateMethod() {
    }
}
