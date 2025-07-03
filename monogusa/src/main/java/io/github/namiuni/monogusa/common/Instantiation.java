package io.github.namiuni.monogusa.common;

/**
 * A factory responsible for creating a new instance of a service or component.
 * It's expected that each call to {@code instantiate()} produces a fresh instance.
 *
 * @param <T> the type of instance to create
 */
@FunctionalInterface
public interface Instantiation<T> {

    T instantiate();
}
