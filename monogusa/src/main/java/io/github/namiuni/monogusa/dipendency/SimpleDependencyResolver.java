package io.github.namiuni.monogusa.dipendency;

import java.nio.file.Path;
import java.util.Set;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.gremlin.runtime.DependencyCache;
import xyz.jpenilla.gremlin.runtime.DependencyResolver;
import xyz.jpenilla.gremlin.runtime.DependencySet;

@NullMarked
public final class SimpleDependencyResolver {

    private SimpleDependencyResolver() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Set<Path> resolve(final Path cacheDir) {
        return SimpleDependencyResolver.resolve(cacheDir, "dependencies.txt");
    }

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
