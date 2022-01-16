package io.github.dejankos;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Example class
 */
public class Example {

    /**
     * Constructor
     */
    public Example() {

    }

    /**
     * Some public method
     * <jdoctest>
     * <pre>
     *     {@code
     *     int a = 5;
     *     String b = "abc";
     *     int res = publicMethod(a, b);
     *     System.out.println(res);
     *     }
     *     Other example
     *     {@code
     *     import java.util.HashMap;
     *
     *     System.out.println("lalal");
     *           }
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
