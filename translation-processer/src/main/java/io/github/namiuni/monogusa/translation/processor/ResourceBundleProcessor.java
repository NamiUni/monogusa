/*
 * monogusa
 *
 * Copyright (c) 2025. Namiu (うにたろう)
 *                     Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.namiuni.monogusa.translation.processor;

import io.github.namiuni.monogusa.translation.annotation.Locales;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceBundle;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceKey;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceSection;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceValue;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

/**
 * Processes {@link ResourceBundle} annotations to generate property files.
 * This processor scans interfaces annotated with {@link ResourceBundle}.
 * All nested interfaces MUST be annotated with {@link ResourceSection} to define
 * the key structure explicitly.
 */
@SupportedAnnotationTypes("io.github.namiuni.monogusa.translation.annotation.annotations.ResourceBundle")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class ResourceBundleProcessor extends AbstractProcessor {

    private static final Pattern CAMEL_CASE_SPLIT_PATTERN = Pattern.compile("(?=\\p{Upper})");
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        for (final var element : roundEnv.getElementsAnnotatedWith(ResourceBundle.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "@ResourceBundle can only be applied to interfaces.", element);
                continue;
            }
            this.processResourceBundle((TypeElement) element);
        }

        return true;
    }

    /**
     * Processes a single interface annotated with {@link ResourceBundle}.
     *
     * @param typeElement The annotated interface element.
     */
    private void processResourceBundle(final TypeElement typeElement) {
        final var resourceBundle = typeElement.getAnnotation(ResourceBundle.class);
        final var baseName = resourceBundle.baseName();
        if (baseName.isBlank()) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "baseName in @ResourceBundle must not be blank.", typeElement);
            return;
        }

        final var localeProperties = new HashMap<Locale, Properties>();
        final var defaultLocale = resourceBundle.defaultLocale().getLocale();
        final var initialPrefix = resourceBundle.prefix();
        final var initialDelimiter = resourceBundle.delimiter();

        this.processInterface(typeElement, initialPrefix, initialDelimiter, localeProperties, defaultLocale, true);

        this.generatePropertiesFiles(baseName, localeProperties, typeElement);
    }

    /**
     * Recursively processes an interface and its nested members.
     *
     * @param interfaceElement The interface element to process.
     * @param parentPrefix     The prefix inherited from the parent.
     * @param parentDelimiter  The delimiter inherited from the parent.
     * @param localeProperties The map to store collected properties.
     * @param defaultLocale    The default locale for the bundle.
     * @param isRoot           True if this is the root interface with @ResourceBundle.
     */
    private void processInterface(
            final TypeElement interfaceElement,
            final String parentPrefix,
            final char parentDelimiter,
            final Map<Locale, Properties> localeProperties,
            final Locale defaultLocale,
            final boolean isRoot
    ) {
        final var section = interfaceElement.getAnnotation(ResourceSection.class);

        if (!isRoot && section == null) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Nested interfaces must be annotated with @ResourceSection.", interfaceElement);
            return;
        }

        final String currentPrefix;
        final char currentDelimiter;

        if (section != null) {
            currentPrefix = this.buildPrefix(parentPrefix, parentDelimiter, section, interfaceElement.getSimpleName().toString());
            currentDelimiter = section.delimiter();
        } else {
            // This case only applies to the root interface.
            currentPrefix = parentPrefix;
            currentDelimiter = parentDelimiter;
        }

        for (final var enclosedElement : interfaceElement.getEnclosedElements()) {
            switch (enclosedElement.getKind()) {
                case METHOD -> this.processMethod(
                        (ExecutableElement) enclosedElement,
                        currentPrefix,
                        currentDelimiter,
                        localeProperties,
                        defaultLocale
                );
                case INTERFACE -> this.processInterface(
                        (TypeElement) enclosedElement,
                        currentPrefix,
                        currentDelimiter,
                        localeProperties,
                        defaultLocale,
                        false // Nested calls are never the root.
                );
                default -> {
                    // Other element types are ignored.
                }
            }
        }
    }

    /**
     * Processes a method to extract the resource key and values.
     *
     * @param method           The method element.
     * @param prefix           The current key prefix.
     * @param delimiter        The delimiter for key construction.
     * @param localeProperties The map to store collected properties.
     * @param defaultLocale    The default locale.
     */
    private void processMethod(
            final ExecutableElement method,
            final String prefix,
            final char delimiter,
            final Map<Locale, Properties> localeProperties,
            final Locale defaultLocale
    ) {
        final var key = this.buildKey(prefix, delimiter, method);
        final var resourceValues = method.getAnnotationsByType(ResourceValue.class);

        if (resourceValues.length == 0) {
            return;
        }

        final var valuesByLocale = this.collectValuesByLocale(resourceValues, defaultLocale);

        final var defaultContent = valuesByLocale.get(defaultLocale);
        if (defaultContent == null) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Missing @ResourceValue for the default locale '%s'.".formatted(defaultLocale), method);
            return;
        }

        valuesByLocale.forEach((key1, value) -> localeProperties
                .computeIfAbsent(key1, k -> new Properties()).setProperty(key, value)); // TODO: OrderedProperties
    }

    /**
     * Generates the .properties files from the collected data.
     *
     * @param baseName           The base name for the resource bundle files.
     * @param localeProperties   The map containing properties for each locale.
     * @param originatingElement The element that triggered the processing, for correct file placement.
     */
    private void generatePropertiesFiles(
            final String baseName,
            final Map<Locale, Properties> localeProperties,
            final Element originatingElement
    ) {
        for (final var entry : localeProperties.entrySet()) {
            final var locale = entry.getKey();
            final var properties = entry.getValue();
            final var fileName = "%s_%s.properties".formatted(baseName, locale.toString());

            try {
                final var fileObject = this.filer.createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        fileName,
                        originatingElement
                );
                try (Writer writer = fileObject.openWriter()) {
                    properties.store(writer, "Generated by ResourceBundleProcessor");
                }
            } catch (final IOException exception) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write resource bundle file: %s - %s".formatted(fileName, exception.getMessage()), originatingElement);
            }
        }
    }

    private String buildPrefix(final String parentPrefix, final char parentDelimiter, final ResourceSection section, final String interfaceName) {
        final var sectionPrefix = section.prefix().isEmpty()
                ? this.splitCamelCase(interfaceName, section.delimiter())
                : section.prefix();
        return parentPrefix + parentDelimiter + sectionPrefix;
    }

    private String buildKey(final String prefix, final char delimiter, final ExecutableElement method) {
        final var keyAnnotation = method.getAnnotation(ResourceKey.class);
        final var methodName = method.getSimpleName().toString();

        final var methodKeyPart = Optional.ofNullable(keyAnnotation)
                .map(ResourceKey::value)
                .filter(value -> !value.isEmpty())
                .orElseGet(() -> this.splitCamelCase(methodName, delimiter));

        return prefix.isEmpty() ? methodKeyPart : prefix + delimiter + methodKeyPart;
    }

    private Map<Locale, String> collectValuesByLocale(final ResourceValue[] values, final Locale defaultLocale) { // TODO: Copy default value
        return Arrays.stream(values).collect(Collectors.toMap(
                value -> value.locale() == Locales.UNSPECIFIED ? defaultLocale : value.locale().getLocale(),
                ResourceValue::content,
                (existing, replacement) -> existing
        ));
    }

    private String splitCamelCase(final String camelCase, final char delimiter) {
        return CAMEL_CASE_SPLIT_PATTERN.splitAsStream(camelCase)
                .map(String::toLowerCase)
                .collect(Collectors.joining(String.valueOf(delimiter)));
    }
}
