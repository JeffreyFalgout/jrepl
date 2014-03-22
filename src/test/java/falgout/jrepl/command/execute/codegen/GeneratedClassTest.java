package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.ClassCompiler.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.Types;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedClassTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;

    @Test
    public void blankClassCanCompile() throws IOException {
        GeneratedClass g = new GeneratedClass(e);
        Class<?> clazz = compile(g);
        assertEquals(g.getName(), clazz.getName());
    }

    private Class<?> compile(GeneratedClass clazz) throws IOException {
        Optional<? extends Class<?>> opt = INSTANCE.execute(e, clazz);
        assertTrue(opt.isPresent());
        return opt.get();
    }
    
    @Test
    public void containsEnvironmentVariables() throws IOException, NoSuchFieldException, SecurityException {
        Variable<?> var1 = new Variable<>(true, Types.OBJECT, "var1", new Object());
        Variable<?> var2 = new Variable<>(true, Types.INT, "var2", 5);
        Variable<?> var3 = new Variable<>(Types.OBJECT, "var3", new Object());
        Variable<?> var4 = new Variable<>(true, Types.CHAR, "var4", '5');
        List<Variable<?>> vars = Arrays.asList(var1, var2, var3, var4);
        
        for (Variable<?> var : vars) {
            assertTrue(e.addVariable(var));
        }
        
        GeneratedClass g = new GeneratedClass(e);
        Class<?> clazz = compile(g);
        for (Variable<?> var : vars) {
            Field f = clazz.getField(var.getIdentifier());
            assertNotNull(f);
            if (var.isFinal()) {
                assertTrue(Modifier.isFinal(f.getModifiers()));
            }
        }
    }
}