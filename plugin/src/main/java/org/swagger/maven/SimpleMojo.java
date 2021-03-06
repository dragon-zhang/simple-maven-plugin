package org.swagger.maven;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author zhangzicheng
 * @date 2021/03/05
 */
@Component(role = ModelBuilder.class)
@Mojo(name = "doc", defaultPhase = LifecyclePhase.COMPILE)
public class SimpleMojo extends AbstractMojo {

    @Requirement
    private Logger log;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor plugin;

    private ClassRealm classRealm;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log.info("simple-maven-plugin started !!!");
        classRealm = plugin.getClassRealm();
        try {
            Map<String, Class<?>> classMap = loadClasses(project.getBuild().getOutputDirectory());
            for (Class<?> klass : classMap.values()) {
                try {
                    ByteBuddy byteBuddy = new ByteBuddy();
                    DynamicType.Builder<?> builder = byteBuddy.redefine(klass);
                    boolean classPresent = klass.isAnnotationPresent(Deprecated.class);
                    if (!classPresent) {
                        builder = builder.annotateType(AnnotationDescription.Builder.ofType(Deprecated.class).build());
                    }
                    for (Field field : klass.getDeclaredFields()) {
                        field.setAccessible(true);
                        boolean fieldPresent = field.isAnnotationPresent(Deprecated.class);
                        if (!fieldPresent) {
                            builder = builder.field(ElementMatchers.named(field.getName()))
                                    .annotateField(AnnotationDescription.Builder
                                            .ofType(Deprecated.class).build());
                        }
                    }
                    for (Method method : klass.getDeclaredMethods()) {
                        method.setAccessible(true);
                        boolean methodPresent = method.isAnnotationPresent(Deprecated.class);
                        if (!methodPresent) {
                            //?????????????????????????????????????????????????????????...
                            builder = builder.visit(new MemberAttributeExtension.ForMethod()
                                    .annotateMethod(AnnotationDescription.Builder
                                            .ofType(Deprecated.class).build())
                                    .on(ElementMatchers.named(method.getName())));
                        }
                    }
                    builder.make()
                            .saveIn(new File(project.getBuild().getOutputDirectory()));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info("simple-maven-plugin finished !!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Class<?>> loadClasses(String rootClassPath) throws Exception {
        Map<String, Class<?>> classMap = new HashMap<>(64);
        // ??????class?????????????????????
        File clazzPath = new File(rootClassPath);

        // ????????????.class???????????????
        int clazzCount = 0;

        if (clazzPath.exists() && clazzPath.isDirectory()) {
            // ??????????????????
            int clazzPathLen = clazzPath.getAbsolutePath().length() + 1;
            Stack<File> stack = new Stack<>();
            stack.push(clazzPath);
            // ???????????????
            while (!stack.isEmpty()) {
                File path = stack.pop();
                File[] classFiles = path.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        //?????????class??????
                        return pathname.isDirectory() || pathname.getName().endsWith(".class");
                    }
                });
                if (classFiles == null) {
                    break;
                }
                for (File subFile : classFiles) {
                    if (subFile.isDirectory()) {
                        stack.push(subFile);
                    } else {
                        if (clazzCount++ == 0) {
                            try {
                                // ??????????????????????????????????????????
                                classRealm.addURL(clazzPath.toURI().toURL());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // ????????????
                        String className = subFile.getAbsolutePath();
                        className = className.substring(clazzPathLen, className.length() - 6);
                        //???/?????????. ?????????????????????
                        className = className.replace(File.separatorChar, '.');
                        // ??????Class???
                        Class<?> klass = Class.forName(className);
                        classMap.put(className, klass);
                    }
                }
            }
        }
        return classMap;
    }
}
