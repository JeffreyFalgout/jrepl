package falgout.jrepl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.command.Command;
import falgout.jrepl.command.CommandFactory;
import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

@Singleton
public final class Environment {
    public static final TypeToken<Object> OBJECT = TypeToken.of(Object.class);
    
    private final CommandFactory factory;
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;

    private final Set<Import> imports = new ImportSet();
    private final EnvironmentClassLoader cl = new EnvironmentClassLoader(imports);
    private final Map<String, Variable<?>> variables = new LinkedHashMap<>();

    @Inject
    public Environment(CommandFactory factory, Reader in, @Stdout Writer out, @Stderr Writer err) {
        this.factory = factory;
        this.in = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
        this.out = createPrintWriter(out);
        this.err = createPrintWriter(err);
        
        try {
            execute("import java.lang.*;");
        } catch (IOException e) {
            throw new Error(e);
        }

        Thread.currentThread().setContextClassLoader(cl);
    }

    private PrintWriter createPrintWriter(Writer w) {
        return w instanceof PrintWriter ? (PrintWriter) w : new PrintWriter(w, true);
    }

    public CommandFactory getFactory() {
        return factory;
    }
    
    public BufferedReader getInput() {
        return in;
    }

    public PrintWriter getOutput() {
        return out;
    }

    public PrintWriter getError() {
        return err;
    }
    
    public boolean addVariable(Variable<?> variable) {
        if (containsVariable(variable.getIdentifier())) {
            return false;
        }
        
        variables.put(variable.getIdentifier(), variable);
        return true;
    }

    public Collection<? extends Variable<?>> getVariables() {
        return Collections.unmodifiableCollection(variables.values());
    }

    public Variable<?> getVariable(String variableName) {
        return variables.get(variableName);
    }

    public <T> T getVariable(String variableName, TypeToken<T> type) {
        Variable<?> var = variables.get(variableName);
        return var.get(type);
    }

    public boolean containsVariable(String variableName) {
        return variables.containsKey(variableName);
    }

    public Set<Import> getImports() {
        return imports;
    }

    public EnvironmentClassLoader getImportClassLoader() {
        return cl;
    }

    public void execute(String input) throws IOException {
        try {
            Command<?> c = factory.getCommand(this, input);
            if (c != null) {
                c.execute(this);
            }
        } finally {
            out.flush();
            err.flush();
        }
    }
}
