package io.github.dejankos.invalid.compile;

public class SimpleCompileError {
    /**
     * <p>
     * <jdoctest>
     * <pre>
     * {@code
     * // missing 'new'
     * final SimpleCompileError example = new SimpleCompileError();
     * example = null;
     * }
     * </pre>
     * </jdoctest>
     */
    public SimpleCompileError() {
    }
}
