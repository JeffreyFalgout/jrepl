package falgout.jrepl.command.execute;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.LocalVariable;
import falgout.jrepl.Variable;
import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.JavaCommandFactory;
import falgout.jrepl.command.execute.codegen.CodeCompiler;
import falgout.jrepl.command.parse.Statements;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class LocalVariableDeclarerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public JavaCommandFactory<List<LocalVariable<?>>> variableParser;
    
    @Before
    public void before(CodeCompiler<Class<?>> compiler) {
        LocalVariableDeclarer declarer = new LocalVariableDeclarer(compiler);
        variableParser = new JavaCommandFactory<>(new Pair<>(Statements.INSTANCE, (env, input) -> {
            List<LocalVariable<?>> vars = new ArrayList<>();
            for (Statement st : (List<Statement>) input.statements()) {
                if (st instanceof VariableDeclarationStatement) {
                    vars.addAll(declarer.execute(env, (VariableDeclarationStatement) st));
                }
            }
            return vars;
        }));
    }
    
    public List<? extends Variable<?>> parse(String input) throws ExecutionException {
        return variableParser.execute(e, input);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void variablesAreAccessibleFromEnvironment() throws ExecutionException {
        Variable<?> var = parse("int x = 5;").get(0);
        assertThat(e.getVariables(), contains(var));
        assertTrue(e.containsVariable("x"));
        assertEquals((Object) 5, e.getVariable("x").get().get(GoogleTypes.INT));
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotHaveVoidVariable() throws ExecutionException {
        parse("void x = null;");
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotAssignStringToPrimitive() throws ExecutionException {
        parse("int x = \"hi\";");
    }
    
    @Test
    public void canDeclareMultipleVariables() throws ExecutionException {
        List<? extends Variable<?>> vars = parse("int x, y[], z[][];");
        assertEquals(3, vars.size());
        assertEquals(GoogleTypes.INT, vars.get(0).getType());
        TypeToken<?> type = GoogleTypes.addArrays(GoogleTypes.INT, 1);
        assertEquals(type, vars.get(1).getType());
        type = GoogleTypes.addArrays(type, 1);
        assertEquals(type, vars.get(2).getType());
    }
    
    @Test(expected = ExecutionException.class)
    public void genericSafetyIsRetained() throws ExecutionException {
        env.execute("import java.util.*;");
        
        Variable<?> var = parse("List<String> x = Arrays.asList(\"1\");").get(0);
        assertEquals(Arrays.asList("1"), var.get());
        
        parse("List<String> y = Arrays.asList(1);");
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotDeclareDuplicateVariablesOnSameLine() throws ExecutionException {
        parse("int x, x[];");
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotDeclareDuplicateVariableFromEnvironment() throws ExecutionException {
        assertEquals(1, parse("int x;").size());
        parse("String x;");
    }
    
    @Test
    public void canAccessPreviousVariables() throws ExecutionException {
        Variable<?> var1 = parse("int x = 5;").get(0);
        Variable<?> var2 = parse("int z = x;").get(0);
        
        assertEquals(var1.get(), var2.get());
    }
    
    @Test
    public void canDeclareVariableOfGeneratedType() throws ExecutionException {
        env.execute("public class Foo {}");
        Variable<?> var = parse("Foo bar = new Foo();").get(0);
        assertEquals("Foo", var.getType().getRawType().getSimpleName());
    }
    
    @Test
    public void canUseVariablesOfGeneratedType() throws ExecutionException {
        env.execute("public class Foo {}");
        Variable<?> var1 = parse("Foo f1 = new Foo();").get(0);
        Variable<?> var2 = parse("Foo f2 = f1;").get(0);
        
        assertSame(var1.get(), var2.get());
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotHaveDuplicateVariables() throws ExecutionException {
        env.execute("Object foo = new Object();");
        env.execute("Object foo = new Object();");
    }
    
    @Test(expected = ExecutionException.class)
    public void CannotAddUninitializedFinalVariable() throws ExecutionException {
        env.execute("final Object foo;");
    }
    
    @Test
    public void CanCreateVariableWithThrowingDeclaration() throws ExecutionException {
        env.execute("public class Foo { public Foo() throws Throwable { } }");
        Variable<?> var = parse("Foo f = new Foo();").get(0);
        assertNotNull(var.get());
    }
    
    @Test(expected = ExecutionException.class)
    public void ThrownExceptionsDuringInitializationAreVisible() throws ExecutionException {
        env.execute("public class Foo { public Foo() throws Throwable { throw new RuntimeException(); } }");
        parse("Foo f = new Foo();");
    }
}
