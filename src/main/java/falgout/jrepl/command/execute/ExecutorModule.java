package falgout.jrepl.command.execute;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.util.Providers;

import falgout.jrepl.Import;
import falgout.jrepl.LocalVariable;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.NestedClass;

public class ExecutorModule extends AbstractModule {
    @SuppressWarnings("rawtypes") private static final TypeToken<Executor> EXECUTOR = TypeToken.of(Executor.class);
    @SuppressWarnings("rawtypes") private static final TypeToken<List> LIST = TypeToken.of(List.class);
    
    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        TypeToken<List<LocalVariable<?>>> r1 = new TypeToken<List<LocalVariable<?>>>() {
            private static final long serialVersionUID = 549616204851758983L;
        };
        TypeToken<NestedClass<?>> r2 = new TypeToken<NestedClass<?>>() {
            private static final long serialVersionUID = -2723154219330053325L;
        };
        TypeToken<Optional<?>> opt = new TypeToken<Optional<?>>() {
            private static final long serialVersionUID = -5652778594381620786L;
        };
        TypeToken<List<? extends Optional<?>>> r3 = (TypeToken<List<? extends Optional<?>>>) GoogleTypes.newParameterizedType(
                null, LIST, GoogleTypes.subtypeOf(opt));
        
        bindAbstractExecutor(LocalVariableDeclarer.class, VariableDeclarationStatement.class, r1);
        bindAbstractExecutor(Providers.of(ClassDefiner.INSTANCE), AbstractTypeDeclaration.class, r2);
        bindAbstractExecutor(StatementExecutor.class, Block.class, r3);
        bindAbstractExecutor(CompilationUnitExecutor.class, CompilationUnit.class, r3);
        bindAbstractExecutor(Providers.of(Importer.INSTANCE), ImportDeclaration.class, Import.class);
        bindAbstractExecutor(ExpressionExecutor.class, Expression.class, Object.class);
        bindAbstractExecutor(MethodDefiner.class, MethodDeclaration.class, Method.class);
    }
    
    private <I, R> void bindAbstractExecutor(Class<? extends AbstractExecutor<I, R>> clazz, Class<I> in, Class<R> out) {
        bindAbstractExecutor(getProvider(clazz), in, out);
    }
    
    private <I, R> void bindAbstractExecutor(Class<? extends AbstractExecutor<I, R>> clazz, Class<I> in,
            TypeToken<R> out) {
        bindAbstractExecutor(getProvider(clazz), in, out);
    }
    
    private <I, R> void bindAbstractExecutor(Provider<? extends AbstractExecutor<I, R>> exec, Class<I> in, Class<R> out) {
        bindAbstractExecutor(exec, TypeToken.of(in), TypeToken.of(out));
    }
    
    private <I, R> void bindAbstractExecutor(Provider<? extends AbstractExecutor<I, R>> exec, Class<I> in,
            TypeToken<R> out) {
        bindAbstractExecutor(exec, TypeToken.of(in), out);
    }
    
    @SuppressWarnings("unchecked")
    private <I, R> void bindAbstractExecutor(Provider<? extends AbstractExecutor<I, R>> exec, TypeToken<I> in,
            TypeToken<R> out) {
        TypeToken<Executor<I, R>> simple = (TypeToken<Executor<I, R>>) GoogleTypes.newParameterizedType(null, EXECUTOR,
                in, out);
        
        bind(GoogleTypes.get(simple)).toProvider(exec);
    }
    
    // XXX https://code.google.com/p/google-guice/issues/detail?id=757
    // XXX https://bugs.eclipse.org/bugs/show_bug.cgi?id=431190
}
