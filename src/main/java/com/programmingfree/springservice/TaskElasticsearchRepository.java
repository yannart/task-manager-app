package com.programmingfree.springservice;

import java.util.List;

public interface TaskElasticsearchRepository {

    boolean save(Task task);

    boolean delete(Task task);

    List<Task> findByNameOrDescription(String filter);
}
