package falgout.jrepl.command.execute.codegen;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;

/**
 * Binds each variable in an {@code Environment} to an {@link Named} variable of
 * the same type.
 *
 * @author jeffrey
 */
public class GeneratorModule extends AbstractModule {
    private final Environment env;
    
    public GeneratorModule(Environment env) {
        this.env = env;
    }
    
    @Override
    protected void configure() {
        for (Variable<?> var : env.getVariables()) {
            bindVariable(var);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> void bindVariable(Variable<T> var) {
        Key<T> key = (Key<T>) Key.get(var.getType().getType(), Names.named(var.getIdentifier()));
        T value = var.get();
        if (value == null) {
            bind(key).toProvider(Providers.of(null));
        } else {
            bind(key).toInstance(value);
        }
    }
}