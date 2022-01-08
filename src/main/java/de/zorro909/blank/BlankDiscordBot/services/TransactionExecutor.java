package de.zorro909.blank.BlankDiscordBot.services;

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

}
