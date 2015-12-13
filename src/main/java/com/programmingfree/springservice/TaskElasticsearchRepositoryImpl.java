package com.programmingfree.springservice;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TaskElasticsearchRepositoryImpl implements
        TaskElasticsearchRepository {

    private static final Logger logger = LoggerFactory
            .getLogger(TaskElasticsearchRepositoryImpl.class);

    public static final String ES_TYPE = "task";

    @Value("${elasticsearch.index}")
    private String index;

    @Value("${elasticsearch.hosts}")
    private String[] hosts;

    @Value("${elasticsearch.port}")
    private int port = 9300;

    TransportClient client;

    @PostConstruct
    public void init() throws IOException {

        logger.info("Configure Elasticsearch client.");

        client = TransportClient.builder().build();

        for (String host : hosts) {
            logger.info(
                    "Adding elasticsearch host with hostname \"{}\" and port \"{}\".",
                    host, port);

            client.addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName(host), port));
        }
    }

    @Override
    public boolean save(Task task) {
        String json;
        XContentBuilder builder;
        try {
            builder = jsonBuilder().startObject()
                    .field("name", task.getTaskName())
                    .field("description", task.getTaskDescription())
                    .endObject();
            json = builder.string();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            IndexResponse response = client
                    .prepareIndex(index, ES_TYPE,
                            Integer.toString(task.getTaskId())).setSource(json)
                    .get();

            logger.info(
                    "Saved task in elasticsearch. index={}, type={}, id={}, version={}, created={}.",
                    response.getIndex(), response.getType(), response.getId(),
                    response.getVersion(), response.isCreated());

            return response.isCreated();

        } catch (Exception ex) {
            logger.error("Problem with Elasticsearch.", ex);
        }
        return false;
    }

    @Override
    public boolean delete(Task task) {

        try {
            DeleteResponse response = client
                    .prepareDelete(index, ES_TYPE,
                            Integer.toString(task.getTaskId())).execute()
                    .actionGet();

            logger.info(
                    "Deleted task in elasticsearch. index={}, type={}, id={}, version={}, found={}.",
                    response.getIndex(), response.getType(), response.getId(),
                    response.getVersion(), response.isFound());

            return response.isFound();

        } catch (Exception ex) {
            logger.error("Problem with Elasticsearch.", ex);
        }

        return false;
    }

    @Override
    public List<Task> findByNameOrDescription(String filter) {

        QueryBuilder query = boolQuery().minimumNumberShouldMatch(1)
                .should(matchQuery("name", filter))
                .should(matchQuery("description", filter));

        List<Task> result = new ArrayList<Task>();

        try {
            SearchResponse response = client.prepareSearch(index)
                    .setTypes(ES_TYPE).setQuery(query).setExplain(true)
                    .execute().actionGet();

            SearchHits sh = response.getHits();

            if (sh == null || sh.getTotalHits() == 0) {
                logger.info("No tasks found with filter {}.", filter);
            } else {

                for (SearchHit searchHit : sh) {
                    logger.info("Task with id {} found with filter {}: {}.",
                            searchHit.getId(), filter,
                            searchHit.getSourceAsString());

                    Map<String, Object> fields = searchHit.getSource();

                    Task task = new Task();
                    task.setTaskId(Integer.parseInt(searchHit.getId()));
                    task.setTaskName(fields.get("name").toString());
                    task.setTaskDescription(fields.get("description")
                            .toString());

                    result.add(task);

                    logger.info("Adding task to results: {}", task);
                }
            }

        } catch (Exception ex) {
            logger.error("Problem with Elasticsearch.", ex);
        }

        return result;
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Elasticsearch client.");
        client.close();
    }

}
