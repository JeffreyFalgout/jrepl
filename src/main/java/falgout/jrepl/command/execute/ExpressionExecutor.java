package falgout.jrepl.command.execute;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeExecutor;
import falgout.jrepl.command.execute.codegen.DelegateSourceCode;
import falgout.jrepl.command.execute.codegen.MethodSourceCode;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.guice.MethodExecutorFactory;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;

@Singleton
public class ExpressionExecutor extends AbstractBatchExecutor<Expression, Object> {
    private final CodeExecutor<Method, Object> executor;
    
    @Inject
    public ExpressionExecutor(MethodExecutorFactory factory) {
        executor = factory.create();
    }
    
    @Override
    public List<? extends Object> execute(Environment env, Collection<? extends Expression> input)
        throws ExecutionException {
        List<MethodSourceCode> methods = new ArrayList<>(input.size());
        
        MethodSourceCode.Builder builder = MethodSourceCode.builder();
        builder.addModifier(Modifier.STATIC);
        builder.addThrows(GoogleTypes.THROWABLE);
        
        input.forEach(e -> {
            SourceCode<Statement> st;
            
            TypeToken<?> returnType = GoogleTypes.VOID;
            try {
                returnType = JDTTypes.getType(e, env);
            } catch (ReflectiveOperationException e1) {
                // looks like the code doesn't make sense.
                // let the compiler have its say
            }
            if (returnType.equals(GoogleTypes.VOID)) {
                st = new DelegateSourceCode<Statement>(e) {
                    @Override
                    public String toString() {
                        return e.toString() + ";";
                    }
                };
            } else {
                st = new DelegateSourceCode<Statement>(e) {
                    @Override
                    public String toString() {
                        return "return " + e.toString() + ";";
                    }
                };
            }
            
            builder.setReturnType(returnType);
            builder.addChildren(st);
            methods.add(builder.build());
            builder.getChildren().clear();
        });
        
        return executor.execute(env, methods);
    }
}
