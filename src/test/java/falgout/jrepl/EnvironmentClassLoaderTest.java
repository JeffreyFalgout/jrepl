package falgout.jrepl;

import static org.junit.Assert.assertSame;

import java.awt.Window.Type;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.command.execute.codegen.ClassCompiler;
import falgout.jrepl.command.execute.codegen.ClassSourceCode;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class EnvironmentClassLoaderTest {
    @Inject @Rule public TestEnvironment env;
    public EnvironmentClassLoader cl;
    
    @Before
    public void before() {
        cl = (EnvironmentClassLoader) Thread.currentThread().getContextClassLoader();
    }
    
    @Test
    public void LoadsImports() throws ClassNotFoundException {
        assertSame(Object.class, cl.loadClass("Object"));
    }
    
    @Test
    public void CanLoadNestedTypes() throws ExecutionException, ClassNotFoundException {
        env.execute("import java.awt.Window.Type;");
        assertSame(Type.class, cl.loadClass("Type"));
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void CannotLoadAmbiguousClass() throws ExecutionException, ClassNotFoundException {
        env.execute("import java.lang.reflect.Type; import java.awt.Window.Type;");
        cl.loadClass("Type");
    }
    
    @Test
    public void CanLoadGeneratedClasses() throws ExecutionException, ClassNotFoundException, ExecutionException {
        ClassSourceCode c = ClassSourceCode.builder().build();
        Class<?> clazz = ClassCompiler.INSTANCE.execute(env.getEnvironment(), c);
        
        assertSame(clazz, cl.loadClass(c.getName()));
    }
    
    @Test
    public void canLoadEnvironmentClass() throws ExecutionException, ClassNotFoundException {
        env.execute("public class Foo {}");
        cl.loadClass("Foo");
    }
}
