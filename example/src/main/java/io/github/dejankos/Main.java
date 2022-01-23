package io.github.dejankos;

import io.github.dejankos.jdoctest.core.JDocTest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Main {

    public static void main(String... a) {
        System.setProperty("java.class.path", "/home/dkos/IdeaProjects/jdoctest/example/target/classes");


        new JDocTest().processSources(".", emptyList());
    }

}
