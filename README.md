# JDocTest

JDocTest add support for executing documentation examples.  
This makes sure that examples within your documentation are up-to-date and working.  
JDocTest works on java POJO classes, records, interfaces and enums.  
Inspired by [rustdoc](https://doc.rust-lang.org/rustdoc/documentation-tests.html#documentation-tests)

## Usage

JDocTest allows opt-in with tag `<jdoctest>` in any javadoc comment. Supports multiple code examples within the same tag
as long as code is properly enclosed in @code tags.  

### Examples

```java 
 /**
     * <jdoctest>
     * <pre>
     *     {@code
     *     ExampleClass e = new ExampleClass();
     *     Map<String, String> map = e.getMap("k", "v");
     *     assert map.size() == 1;
     *     }
     * </pre>
     * </jdoctest>
     *
     * @param a first
     * @param b second
     */
    public Map<String, String> getMap(String a, String b) 
```

All class imports are implicitly available in doc code and other imports can be added; fully qualified names are also  
supported.

```java 
 /**
     * <jdoctest>
     * <pre>
     *     {@code
     *     import java.util.HashSet;
     *
     *     java.util.Set<String> set = new HashSet<String>();
     *     set.add("some value");
     *
     *     assert set.size() == 1;
     *     }
     * </pre>
     * </jdoctest>
     */
```
All examples available in `example` module.  

### Passing and failing

Like in regular unit tests JDocTests are considered to "pass" if they compile and run without throwing an exception  
or assertion errors.

For validation purposes assertions are enabled in all jdoctest code blocks.

## Plugin

JDocPlugin can be integrated with a Maven project and run on build.  
Maven task will fail if any violations are found.  
```
    <plugin>
        <groupId>io.github.dejankos</groupId>
        <artifactId>jdoctest-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <goals>
                    <goal>jdoctest</goal>
                </goals>
                <phase>verify</phase>
            </execution>
        </executions>
        <!-- optional: default is project sources root -->
        <configuration>
            <docPath>${project.basedir}/src/main/java/io/github/dejankos/valid</docPath>
        </configuration>
    </plugin>
```

## License

JDocTest is licensed under the [MIT License](https://opensource.org/licenses/MIT)
