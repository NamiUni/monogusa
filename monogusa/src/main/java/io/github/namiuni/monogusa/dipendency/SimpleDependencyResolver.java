package io.github.namiuni.monogusa.dipendency;

import java.nio.file.Path;
import java.util.Set;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.gremlin.runtime.DependencyCache;
import xyz.jpenilla.gremlin.runtime.DependencyResolver;
import xyz.jpenilla.gremlin.runtime.DependencySet;

/**
 * A utility class for resolving and caching project dependencies using Gremlin.
 * It simplifies downloading JAR files based on a dependency list.
 */
@NullMarked
public final class SimpleDependencyResolver {

    private SimpleDependencyResolver() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Resolves dependencies from "dependencies.txt" and caches them in the specified directory.
     *
     * @param cacheDirectory the directory to cache resolved JAR files
     * @return a set of paths to the resolved JAR files
     */
    public static Set<Path> resolve(final Path cacheDirectory) {
        return SimpleDependencyResolver.resolve(cacheDirectory, "dependencies.txt");
    }

    /**
     * Resolves dependencies from a specified resource file and caches them in the specified directory.
     *
     * @param cacheDirectory the directory to cache resolved JAR files
     * @param fileName the name of the classpath resource file containing the dependency list
     * @return a set of paths to the resolved JAR files
     */
    public static Set<Path> resolve(final Path cacheDirectory, final String fileName) {
        final DependencySet dependencies = DependencySet.readFromClasspathResource(SimpleDependencyResolver.class.getClassLoader(), fileName);
        final DependencyCache cache = new DependencyCache(cacheDirectory);
        final Logger logger = LoggerFactory.getLogger(SimpleDependencyResolver.class.getSimpleName());
        final Set<Path> files;
        try (final DependencyResolver downloader = new DependencyResolver(logger)) {
            files = downloader.resolve(dependencies, cache).jarFiles();
        }

        cache.cleanup();
        return files;
    }
}
