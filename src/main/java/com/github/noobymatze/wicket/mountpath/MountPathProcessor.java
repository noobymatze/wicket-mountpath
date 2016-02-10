package com.github.noobymatze.wicket.mountpath;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.apache.wicket.protocol.http.WebApplication;

/**
 *
 * @author Matthias Metzger
 */
@SupportedAnnotationTypes({"com.github.noobymatze.wicket.mountpath.MountPath"})
public class MountPathProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(MountPathProcessor.class.getName());

    private final Set<TypeElement> elements = new HashSet<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private void generateMountPathConfiguration() {
        MethodSpec.Builder configureBuilder = MethodSpec.methodBuilder("configure").
            addModifiers(Modifier.PUBLIC).
            addParameter(WebApplication.class, "application", Modifier.FINAL).
            returns(void.class);

        MethodSpec configure = elements.stream().
            map(this::processClassWithAnnotation).
            map(pageCall -> "application." + pageCall).
            reduce(configureBuilder, MethodSpec.Builder::addStatement, (b1, b2) -> b1).
            build();

        AnnotationSpec generated = AnnotationSpec.builder(Generated.class).
            addMember("value", "$S", MountPathProcessor.class.getCanonicalName()).
            build();
        
        TypeSpec mountPathConfiguration = TypeSpec.
            classBuilder("MountPathConfiguration").
            addAnnotation(generated).
            addModifiers(Modifier.PUBLIC, Modifier.FINAL).
            addMethod(configure).
            build();

        Filer filer = processingEnv.getFiler();

        try {
            JavaFile file = JavaFile.
                builder("com.github.noobymatze.wicket.mountpath", mountPathConfiguration).
                build();

            file.writeTo(filer);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        System.out.println(mountPathConfiguration);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver() || annotations.isEmpty()) {
            generateMountPathConfiguration();
            return false;
        }
        else {
            env.getElementsAnnotatedWith(MountPath.class).stream().
                map(e -> (TypeElement) e).
                forEach(elements::add);

            return false;
        }
    }

    private String processClassWithAnnotation(TypeElement element) {
        Name name = element.getQualifiedName();
        String path = element.getAnnotation(MountPath.class).value();
        String normalizedPath = normalizePath(path);

        return String.format(
            "mountPage(\"%s\", %s.class)",
            normalizedPath, name
        );
    }

    private String normalizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }
    
}
