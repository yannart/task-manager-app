package com.programmingfree.springservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class TaskEventHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(TaskEventHandler.class);

    @Autowired
    Producer producer;

    @Autowired
    TaskElasticsearchRepository taskElasticsearchRepository;

    @HandleAfterSave
    public void handleTaskSave(Task task) {
        logger.info("Task saved: {}", task);
        handleTaskModified(task);
    }

    @HandleAfterCreate
    public void handleTaskCreate(Task task) {
        logger.info("Task created: {}", task);
        handleTaskModified(task);
    }

    private void handleTaskModified(Task task) {
        producer.send("TASK updated: " + task.toString());

        if (task.isTaskArchived()) {
            
            logger.info("The task is archived: {}", task);
            taskElasticsearchRepository.delete(task);
        } else {
            
            logger.info("The task is not archived: {}", task);
            taskElasticsearchRepository.save(task);
        }
    }
}
