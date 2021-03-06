package falgout.jrepl.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.parse.Parser;

public abstract class AbstractCommandFactory<I, M, R> implements CommandFactory<R> {
    public static class Pair<I, M, R> implements Parser<I, M>, Command<R> {
        private final Parser<? super I, ? extends M> parser;
        private final Executor<? super M, ? extends R> executor;
        private M intermediary;
        
        public Pair(Parser<? super I, ? extends M> parser, Executor<? super M, ? extends R> executor) {
            this.parser = parser;
            this.executor = executor;
        }
        
        @Override
        public R execute(Environment env) throws ExecutionException {
            return executor.execute(env, intermediary);
        }
        
        @Override
        public M parse(I input) {
            intermediary = parser.parse(input);
            return intermediary;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Pair [parser=");
            builder.append(parser.getClass());
            builder.append("]");
            return builder.toString();
        }
    }
    
    private final Predicate<? super M> accept;
    private final Comparator<? super M> ranker;
    private final List<Pair<? super I, ? extends M, ? extends R>> pairs;
    
    @SafeVarargs
    protected AbstractCommandFactory(Predicate<? super M> accept, Comparator<? super M> ranker,
            Pair<? super I, ? extends M, ? extends R>... pairs) {
        this.accept = accept;
        this.ranker = ranker;
        this.pairs = Arrays.asList(pairs);
    }
    
    @Override
    public Command<? extends R> getCommand(Environment env, String input) throws ParsingException {
        I in = createNewInput();
        
        List<M> min = new ArrayList<>();
        for (Pair<? super I, ? extends M, ? extends R> pair : pairs) {
            in = initialize(in, input);
            
            M result = pair.parse(in);
            if (result != null) {
                int c = 1;
                if (accept.test(result)) {
                    reportSuccess(env, result);
                    return pair;
                } else if (min.size() > 0) {
                    c = ranker.compare(result, min.get(0));
                    if (c < 0) {
                        min.clear();
                    }
                }
                
                if (min.size() == 0 || c <= 0) {
                    min.add(result);
                }
            }
        }
        
        throw createParsingException(min);
    }
    
    protected abstract I createNewInput();
    
    protected abstract I initialize(I blank, String input);
    
    protected abstract void reportSuccess(Environment env, M success);
    
    protected abstract ParsingException createParsingException(List<? extends M> min);
}
