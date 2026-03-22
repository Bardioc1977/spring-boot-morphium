package de.caluga.morphium.spring.autoconfigure;

import java.lang.annotation.*;

/**
 * Marks a method or class to run within a Morphium transaction.
 * Requires a replica set (or Atlas) — single-node standalone MongoDB
 * does not support multi-document transactions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MorphiumTransactional {
}
