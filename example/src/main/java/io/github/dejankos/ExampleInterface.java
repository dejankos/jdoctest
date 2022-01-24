package io.github.dejankos;

public interface ExampleInterface {

    /**
     * This is a javadoc comment !
     * <p>
     * Example:
     * <jdoctest>
     * <pre>
     * {@code
     *  //create instance
     *  ExampleInterface example = new ExampleClass();
     *
     *  // invoke
     *  int value = example.methodA();
     *
     *  // assert as expected
     *  assert value >= 0;
     * }
     * </pre>
     * </jdoctest>
     *
     * @return some very important number
     */
    int methodA();

}
