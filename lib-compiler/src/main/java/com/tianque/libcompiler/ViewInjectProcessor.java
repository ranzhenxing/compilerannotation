package com.tianque.libcompiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.tianque.libannotations.BindView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.tianque.libannotations.BindView"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ViewInjectProcessor extends AbstractProcessor {

    //存放同一个Class下的所有注解
    private Map<String, List<VariableInfo>> classMap = new HashMap<>();
    //存放Class对应的TypeElement
    private Map<String, TypeElement> classTypeElement = new HashMap<>();
    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //Filer filer; 跟文件相关的辅助类，生成JavaSourceCode.
        //Elements elementUtils;跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectInfo(roundEnvironment);
        writeToFile();
        return true;
    }

    private void collectInfo(RoundEnvironment roundEnvironment) {
        //因为process可能会多次调用，避免生成重复的代理类，避免生成类的类名已存在异常。
        classMap.clear();
        classTypeElement.clear();
        //拿到我们通过@BindView注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //Element
                //- VariableElement //一般代表成员变量
                //- ExecutableElement //一般代表类中的方法
                //- TypeElement //一般代表代表类
                //- PackageElement //一般代表Package
        for (Element element : elements) {
            //获取 BindView 注解的值
            int viewId = element.getAnnotation(BindView.class).value();
            //代表被注解的元素
            VariableElement variableElement = (VariableElement) element;
            //获取注解元素所在的Class
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //Class的完整路径
            String classFullName = typeElement.getQualifiedName().toString();

            //收集Class中所有被注解的元素,key为classFullName,value为List<VariableInfo>被注解的元素集合
            List<VariableInfo> variableList = classMap.get(classFullName);
            if (variableList == null) {
                variableList = new ArrayList<>();
                classMap.put(classFullName, variableList);
                classTypeElement.put(classFullName, typeElement);
            }
            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setVariableElement(variableElement);
            variableInfo.setViewId(viewId);
            variableList.add(variableInfo);
        }
    }
/*
* //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tianque.compilerannotation;

import android.widget.TextView;

public class MainActivity$$ViewInjector {
    public MainActivity$$ViewInjector(MainActivity activity) {
        activity.tvTest = (TextView)activity.findViewById(2131165312);
    }
}*/
private void writeToFile() {
        try {

            for (String classFullName : classMap.keySet()) {
                TypeElement typeElement = classTypeElement.get(classFullName);
                 //编写构造函数    public MainActivity$$ViewInjector(MainActivity activity) {
                //
                //    }
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "activity").build());

                //构造函数中增加实体代码 activity.tvTest = (TextView)activity.findViewById(2131165312);
                List<VariableInfo> variableList = classMap.get(classFullName);
                for (VariableInfo variableInfo : variableList) {
                    VariableElement variableElement = variableInfo.getVariableElement();
                    // 变量名称(比如：TextView tv 的 tv)
                    String variableName = variableElement.getSimpleName().toString();
                    // 变量类型的完整类路径（比如：android.widget.TextView）
                    String variableFullName = variableElement.asType().toString();
                    // 在构造方法中增加赋值语句，例如：activity.tvTest = (android.widget.TextView)activity.findViewById(2131165312);
                    constructor.addStatement("activity.$L=($L)activity.findViewById($L)", variableName, variableFullName, variableInfo.getViewId());
                }
                    // 构建Class 名称为MainActivity$$ViewInjector
                TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$ViewInjector")
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc("代理类")
                        .addMethod(constructor.build())
                        .build();
                // 与目标Class放在同一个包下，解决Class属性的可访问性
                String packageFullName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec)
                        .build();
                // 生成class文件
                javaFile.writeTo(filer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
