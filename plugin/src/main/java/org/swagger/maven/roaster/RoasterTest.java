package org.swagger.maven.roaster;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaDocTag;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.util.List;

/**
 * 类上的注释
 *
 * @author zhangzicheng
 * @date 2021/03/06
 */
public class RoasterTest {

    /**
     * 字段上的注释
     */
    private final String id = "id";

    /**
     * 方法上的注释
     */
    public static void main(String[] args) throws Exception {
        File file = new File("/Users/zhangzicheng/Desktop/simple-maven-plugin/plugin/src/main/java/org/swagger/maven/roaster/RoasterTest.java");
        JavaClassSource source = Roaster.parse(JavaClassSource.class, file);
        System.out.println(source.getQualifiedName());
        JavaDocSource<JavaClassSource> classDoc = source.getJavaDoc();
        System.out.println(classDoc.getText());
        List<JavaDocTag> classTags = classDoc.getTags();
        for (JavaDocTag tag : classTags) {
            System.out.println(tag.getName() + " " + tag.getValue());
        }

        List<FieldSource<JavaClassSource>> fields = source.getFields();
        for (FieldSource<JavaClassSource> field : fields) {
            System.out.println(field.getName());
            JavaDocSource<FieldSource<JavaClassSource>> fieldDoc = field.getJavaDoc();
            System.out.println(fieldDoc.getText());
            List<JavaDocTag> fieldTags = fieldDoc.getTags();
            for (JavaDocTag tag : fieldTags) {
                System.out.println(tag.getName() + " " + tag.getValue());
            }
        }

        List<MethodSource<JavaClassSource>> methods = source.getMethods();
        for (MethodSource<JavaClassSource> method : methods) {
            System.out.println(method.getName());
            JavaDocSource<?> methodDoc = method.getJavaDoc();
            System.out.println(methodDoc.getText());
            List<JavaDocTag> methodTags = methodDoc.getTags();
            for (JavaDocTag tag : methodTags) {
                System.out.println(tag.getName() + " " + tag.getValue());
            }
        }
    }
}
