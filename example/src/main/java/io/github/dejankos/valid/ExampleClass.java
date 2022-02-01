package io.github.dejankos.valid;

import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * Example class
 */
public class ExampleClass implements ExampleInterface {

    /**
     * Constructor
     * <p>
     * Example code ofc it works
     * <pre>
     * {@code
     * // pls help me jDocTest you're my only hope
     * let instance = nu ExmapleCluzz(got args?);
     * }
     * </pre>
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
     *     First example
     *     {@code
     *     ExampleClass e = new ExampleClass();
     *     Map<String, String> map = e.getMap("k", "v");
     *     assert map.size() == 1;
     *     }
     *
     *     Second example
     *     Let's do something with it
     *     {@code
     *     ExampleClass e = new ExampleClass();
     *     Map<String, String> map = e.getMap("k", "v");
     *
     *     assert map.size() == 1;
     *
     *     String v = map.get("k");
     *     assert v.equals("v");
     *
     *     import java.util.HashSet;
     *
     *     java.util.Set<String> set = new HashSet<String>();
     *     set.add(v);
     *
     *     assert set.size() == 1;
     *     Assertions.assertEquals(1, set.size()); // external dep check
     *     }
     * </pre>
     * </jdoctest>
     *
     * @param a first
     * @param b second
     */
    public Map<String, String> getMap(String a, String b) {
        Assertions.assertNotNull(a);
        HashMap<String, String> map = new HashMap<>();
        map.put(a, b);

        return map;
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
