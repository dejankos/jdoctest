package io.github.dejankos;

import io.github.dejankos.jdoctest.core.JDocTest;

import static java.util.Collections.singletonList;

public class Main {

    public static void main(String... a) {
        new JDocTest().processSources(".", singletonList("example/target/classes"));
    }

}
