package com.programmingfree.springservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class TaskEventHandler {

    @Autowired
    Producer producer;

    @HandleAfterSave
    public void handleTaskSave(Task task) {
        producer.send("TASK Saved: " + task.toString());
    }

    @HandleAfterCreate
    public void handleTaskCreate(Task task) {
        producer.send("TASK Created: " + task.toString());
    }
}
