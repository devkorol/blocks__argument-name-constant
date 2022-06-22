package com.github.devkorol.blocks.argument.name.constant;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class ArgumentNameConstantProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(ArgumentNameConstant.class.getName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    processingEnv.getMessager().printMessage(Kind.NOTE, "@ArgumentNameConstant visitor started");
    for (TypeElement annotation : annotations) {
      try {
        processEachAnnotatedMethod(annotation, roundEnv);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return true;
  }

  private void processEachAnnotatedMethod(TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
    HashMap<TypeMirror, List<String>> classToMethodMap = new HashMap<>();

    Set<? extends Element> annotatedMethods = roundEnv.getElementsAnnotatedWith(annotation);
    for (Element method : annotatedMethods) {
      List<? extends VariableElement> parameters = ((ExecutableElement) method).getParameters();
      if (parameters.isEmpty()) {
        processingEnv.getMessager().printMessage(Kind.ERROR,
            "@ArgumentNameConstant must be applied to a method with at least one argument", method);
      } else {
        TypeMirror markedClass = method.getEnclosingElement().asType();
        String methodName = method.getSimpleName().toString();

        if (!classToMethodMap.containsKey(markedClass)) {
          classToMethodMap.put(markedClass, new ArrayList<>());
        }

        if (classToMethodMap.get(markedClass).contains(methodName)) {
          processingEnv.getMessager().printMessage(Kind.ERROR,
              "@ArgumentNameConstant is not supporting method overloading", method);
        } else {
          classToMethodMap.get(markedClass).add(methodName);
          writeMethodParamClass(markedClass, methodName, parameters);
        }
      }
    }
  }

  private void writeMethodParamClass(TypeMirror markedClass, String methodName,
      List<? extends VariableElement> parameters) throws IOException {
    String fullName = markedClass.toString();
    String className = fullName.substring(fullName.lastIndexOf(".") + 1);
    String classPackage = fullName.substring(0, fullName.lastIndexOf("."));
    String paramClassName =
        className + methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "Arguments";

    processingEnv.getMessager().printMessage(Kind.ERROR, fullName);

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(classPackage + "." + paramClassName);
    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.print("package ");
      out.print(classPackage);
      out.println(";");

      out.print("public class ");
      out.print(paramClassName);
      out.println(" {");

      for (VariableElement parameter : parameters) {
        String name = parameter.getSimpleName().toString();
        out.print("public static final String ");
        out.print(name);
        out.print("=\"");
        out.print(name);
        out.println("\";");
      }
      out.println("}");
    }
  }
}
