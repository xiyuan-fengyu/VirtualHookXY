package com.xiyuan.hookmethod;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by xiyuan_fengyu on 2018/12/3.
 */

@AutoService(Processor.class)
public class HookMethodAnnotationProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;

    // method modifier: public, protected, private, abstract, static, final, synchronized, native, strictfp
    private static final Set<String> methodModifiers = new HashSet<>(Arrays.asList(
            "public", "protected", "private", "abstract", "static", "final", "synchronized", "native", "strictfp"
    ));

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(HookMethod.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> hookMethodEles = roundEnvironment.getElementsAnnotatedWith(HookMethod.class);
        Set<String> hookClasses = new LinkedHashSet<>();
        Map<String, Set<String>> originalStubs = new HashMap<>();
        for (Element ele : hookMethodEles) {
            ExecutableElement hookMethodEle = (ExecutableElement) ele;

            TypeElement hookClassEle = (TypeElement) hookMethodEle.getEnclosingElement();
            String hookClass = hookClassEle.getQualifiedName().toString();
            hookClasses.add(hookClass);

            // exp: protected void android.app.Activity.onCreate(android.os.Bundle)
            String orignalMethodSignature = hookMethodEle.getAnnotation(HookMethod.class).value();
            try {
                String[] stub = buidOriginalMethodStub(orignalMethodSignature, hookMethodEle);
                Set<String> stubMethods = originalStubs.get(stub[0]);
                if (stubMethods == null) {
                    stubMethods = new LinkedHashSet<>();
                    originalStubs.put(stub[0], stubMethods);
                }
                stubMethods.add(stub[1]);
            }
            catch (Exception e) {
                error("HookMethod(\"" + orignalMethodSignature + "\") " + hookMethodEle.toString() + " parsed with error: " + e.getMessage());
//                e.printStackTrace();
            }
        }

        if (hookClasses.size() > 0) {
            // begin to generate java file
            try {
                generateHooksJava(hookClasses);
            } catch (IOException e) {
                error("generate com.xiyuan.hookmethod.Hooks.java fail: " + e.getMessage());
//            e.printStackTrace();
            }

            for (Map.Entry<String, Set<String>> entry : originalStubs.entrySet()) {
                try {
                    generateStubsJava(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    error("generate " + entry.getKey() + ".java fail: " + e.getMessage());
//                e.printStackTrace();
                }
            }
            return true;
        }
        else return false;
    }

    /**
     * 通过原方法的签名生成对应的stub类和方法
     * 返回值为[完整类名, stub方法声明]
     * @return
     */
    private String[] buidOriginalMethodStub(String orignalMethodSignature, ExecutableElement hookMethodEle) throws Exception {
        // the hook method must be 'public static'
        Set<Modifier> hookMethodModifiers = hookMethodEle.getModifiers();
        if (hookMethodModifiers.contains(Modifier.PUBLIC) && hookMethodModifiers.contains(Modifier.STATIC)) {
        }
        else {
            throw new Exception("the hook method must be 'public static'");
        }

        boolean isStatic = false;
        boolean isConstructor;
        String[] modifiers_returnType_method = orignalMethodSignature.substring(0, orignalMethodSignature.indexOf('(')).split(" ");
        List<String> returnType_method = new ArrayList<>();
        for (String s : modifiers_returnType_method) {
            if (s.equals("static")) {
                isStatic = true;
            }
            else if (methodModifiers.contains(s)) {
            }
            else if (s.startsWith("<") && s.endsWith(">")) {
            }
            else {
                returnType_method.add(s);
            }
        }
        isConstructor = returnType_method.size() == 1;

        // check whether the return types of orignal and hook are match
        String orignalMethodReturnType = returnType_method.get(0);
        String hookMethodReturnType = hookMethodEle.getReturnType().toString();
        if (!isTypeMatch(orignalMethodReturnType, hookMethodReturnType)) {
            throw new Exception("return types are not match");
        }

        String orignalClassName;
        if (isConstructor) {
            orignalClassName = returnType_method.get(0);
        }
        else {
            orignalClassName = returnType_method.get(1);
            orignalClassName = orignalClassName.substring(0, orignalClassName.lastIndexOf('.'));
        }

        // check whether the parameters type match
        List<String> orignalMethodParamTypes = parseMethodParamTypes(orignalMethodSignature);
        if (!isStatic) {
            orignalMethodParamTypes.add(0, orignalClassName);
        }
        // exp: onCreate(java.lang.Object,android.os.Bundle)
        List<String> hookMethodParamTypes = parseMethodParamTypes(hookMethodEle.toString());
//        note(orignalMethodParamTypes.stream().collect(Collectors.joining("    ")) + "    size=" + orignalMethodParamTypes.size());
//        note(hookMethodParamTypes.stream().collect(Collectors.joining("    ")) + "    size=" + hookMethodParamTypes.size());
        if (orignalMethodParamTypes.size() != hookMethodParamTypes.size()) {
            throw new Exception("parameters size are not match");
        }
        for (int i = 0, size = orignalMethodParamTypes.size(); i < size; i++) {
            if (!isTypeMatch(orignalMethodParamTypes.get(i), hookMethodParamTypes.get(i))) {
                throw new Exception("parameters type are not match");
            }
        }

        String stubClassName;
        if (isConstructor) {
            stubClassName = orignalClassName + "_constructor";
        }
        else {
            stubClassName = returnType_method.get(1);
            int lastDotIndex= stubClassName.lastIndexOf('.');
            if (lastDotIndex > -1) {
                stubClassName = stubClassName.substring(0, lastDotIndex) + '_' + stubClassName.substring(lastDotIndex + 1);
            }
        }

        List<? extends VariableElement> paramNames = hookMethodEle.getParameters();
        StringBuilder stubMethod = new StringBuilder();
        stubMethod.append("    public static <T> T invoke(");
        for (int i = 0, size = hookMethodParamTypes.size(); i < size; i++) {
            stubMethod.append(hookMethodParamTypes.get(i)).append(' ');
            stubMethod.append(paramNames.get(i).toString());
            if (i + 1 < size) {
                stubMethod.append(", ");
            }
        }
        stubMethod.append(") {\n        // Stub for the orignal method\n        return null;\n    }\n\n");
        return new String[] {stubClassName, stubMethod.toString()};
    }

    private boolean isTypeMatch(String source, String match) {
        return source.equals(match) || match.equals("java.lang.Object");
    }

    private List<String> parseMethodParamTypes(String methodSignature) {
        List<String> res = new ArrayList<>();
        String params = methodSignature.substring(
                methodSignature.indexOf('(') + 1,
                methodSignature.indexOf(')'));
        if (params.length() > 0) {
            // num of not matched '<'
            int brackets = 0;
            int len = params.length();
            int j = 0;
            for (int i = 0; i < len; i++) {
                char c = params.charAt(i);
                if (c == ',') {
                    if (brackets == 0) {
                        res.add(params.substring(j, i));
                        j = i + 1;
                    }
                }
                else if (c == '<') {
                    brackets++;
                }
                else if (c == '>') {
                    brackets--;
                }
            }
            res.add(params.substring(j, len));
        }
        return res;
    }

    private void generateHooksJava(Set<String> hookClasses) throws IOException {
        note("generateHooksJava com.xiyuan.hookmethod.Hooks");
        JavaFileObject jfo = mFiler.createSourceFile("com.xiyuan.hookmethod.Hooks", new Element[]{});
        Writer writer = jfo.openWriter();
        writer.write("package com.xiyuan.hookmethod;\n\n");
        writer.write("//Auto generated by apt,do not modify!!\n\n");
        writer.write("public class Hooks { \n\n");
        writer.write("    public static final Class[] hooks = {\n");
        for (String hookClass : hookClasses) {
            writer.write("        " + hookClass + ".class,\n");
        }
        writer.write("    };\n\n");
        writer.write("}\n");
        writer.flush();
        writer.close();
    }

    private void generateStubsJava(String clazz, Set<String> methods) throws IOException {
        note("generateStubsJava " + clazz);
        JavaFileObject jfo = mFiler.createSourceFile(clazz, new Element[]{});
        Writer writer = jfo.openWriter();
        int lastDotIndex = clazz.lastIndexOf('.');
        if (lastDotIndex > -1) {
            writer.write("package " + clazz.substring(0, lastDotIndex) + ";\n\n");
        }
        writer.write("//Auto generated by apt,do not modify!!\n\n");
        writer.write("public class " + (lastDotIndex > -1 ? clazz.substring(lastDotIndex + 1) : clazz) + " { \n\n");
        for (String method : methods) {
            writer.write(method);
        }
        writer.write("}\n");
        writer.flush();
        writer.close();
    }

    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "[HookMethod] " + msg);
    }

    private void note(String format, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "[HookMethod] " + String.format(format, args));
    }

    private void error(String msg) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, "[HookMethod] " + msg);
    }

    private void error(String format, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, "[HookMethod] " + String.format(format, args));
    }

}