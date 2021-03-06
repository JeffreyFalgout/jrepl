package falgout.jrepl.command.execute;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.TypeSourceCode;
import falgout.jrepl.reflection.NestedClass;

public class ClassDefiner extends RepositoryDefiner<AbstractTypeDeclaration, NestedClass<?>> {
    public static final ClassDefiner INSTANCE = new ClassDefiner();
    
    public ClassDefiner() {
        super((env, input) -> TypeSourceCode.get(input).asNestedClass(true));
    }
    
    @Override
    protected CodeRepository<NestedClass<?>> getRepository(Environment env) {
        return env.getClassRepository();
    }
}
