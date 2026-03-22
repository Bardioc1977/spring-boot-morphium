package de.caluga.morphium.spring.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables scanning for Morphium Jakarta Data repository interfaces.
 * Scans the annotated class's package (and sub-packages) by default.
 *
 * <pre>
 * {@code @SpringBootApplication}
 * {@code @EnableMorphiumRepositories}
 * public class MyApp { }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MorphiumRepositoryRegistrar.class)
public @interface EnableMorphiumRepositories {

    /**
     * Base packages to scan for repository interfaces.
     * Defaults to the package of the annotated class.
     */
    String[] value() default {};

    /**
     * Alias for {@link #value()}.
     */
    String[] basePackages() default {};
}
