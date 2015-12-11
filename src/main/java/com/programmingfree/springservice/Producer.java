package com.programmingfree.springservice;

import javax.jms.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Queue queue;

    public void send(String msg) {

        logger.info("Sending the message \"{}\" to the queue \"{}\".", msg,
                this.queue.toString());

        this.jmsMessagingTemplate.convertAndSend(this.queue, msg);
    }

}