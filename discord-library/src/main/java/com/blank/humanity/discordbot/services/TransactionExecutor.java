package com.blank.humanity.discordbot.services;

import java.sql.Connection;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class TransactionExecutor {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * Executes {@code executable} asynchronously in a dedicated Thread with a
     * new Transaction.<br>
     * If any exception gets thrown the {@code exceptionHandler} will get called
     * with the Exception as parameter.<br>
     * The {@code finishHandler} is always called with the result of the
     * executable after the execution has finished.
     * 
     * @param <T>              Type of the result Object for the Transaction
     *                         call.
     * @param executable       The call to be executed. Can return null.
     * @param exceptionHandler Handler for any exceptions thrown by executable.
     * @param finishHandler    Always gets called after execution.
     */
    public <T> void executeAsTransaction(TransactionCallback<T> executable,
        Consumer<Exception> exceptionHandler, Consumer<T> finishHandler) {
        TransactionTemplate txTemplate = new TransactionTemplate(
            transactionManager);
        txTemplate
            .setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        taskExecutor.execute(() -> {
            T result = null;
            try {
                result = txTemplate.execute(executable);
            } catch (Exception e) {
                exceptionHandler.accept(e);
            }
            finishHandler.accept(result);
        });
    }

    public <T> T executeAsTransactionSync(TransactionCallback<T> executable) {
        TransactionTemplate txTemplate = new TransactionTemplate(
            transactionManager);
        txTemplate
            .setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
        return txTemplate.execute(executable);
    }

}
