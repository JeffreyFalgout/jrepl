package falgout.jrepl.command;

import java.io.IOException;

import falgout.jrepl.Environment;

public interface Command {
    /**
     * 
     * @param e The {@code Environment} to execute in
     * @return {@code true} if the command executed successfully
     * @throws IOException If an {@code IOException} occurs during execution
     */
    public boolean execute(Environment e) throws IOException;
}