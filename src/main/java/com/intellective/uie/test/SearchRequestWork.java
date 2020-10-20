package com.intellective.uie.test;


import com.intellective.commons.security.VuUserContext;
import com.intellective.uie.repository.StopIndicator;
import com.intellective.uie.repository.SupplementaryFieldsLoader;
import com.intellective.uie.search.client.ContextResponse;
import com.intellective.uie.search.client.UieClient;
import com.intellective.uie.transport.message.PagedSearchRequest;
import com.intellective.uie.transport.message.PagedSearchResponse;
import commonj.work.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class SearchRequestWork implements Work {
    protected static final Logger logger = LoggerFactory.getLogger(SupplementaryFieldsLoader.class);

    private int id;
    private BlockingQueue<SearchRequestTask> taskQueue;
    private UieClient client;
    private volatile boolean running;
    private final StopIndicator stopIndicator;
    private AtomicReference<Integer> taskId = new AtomicReference<>();

    public SearchRequestWork(int id, BlockingQueue<SearchRequestTask> taskQueue,
                             UieClient client,
                             StopIndicator stopIndicator)
    {
        this.id = id;
        this.taskQueue = taskQueue;
        this.client = client;
        this.stopIndicator = stopIndicator;
        this.running = true;
    }

    @Override
    public void release() {
        running = false; // signal to stop execution
        logger.debug("Content load task released: {}", this);
    }

    @Override
    public boolean isDaemon() {
        return false;
    }

    @Override
    public void run() {
        SearchRequestTask task;

        while (running
                && !Thread.interrupted()
                && (stopIndicator == null || !stopIndicator.isStopped())
                && (task = taskQueue.poll()) != null)
        {
            try {
                logger.debug("Loading document content, task={}", task);

                if(task.isCompleted()) {
                    logger.debug("Search task completed (skipped): {}", task);
                    taskId.set(null);

                    continue;
                }

                taskId.set(task.getTaskId());
                PagedSearchRequest query = task.getSearchRequest();
                query.setRequestToken(null);

                VuUserContext userContext = task.getUserContext();

                long start = System.currentTimeMillis();
                ContextResponse<PagedSearchResponse> contextResponse = client.selectDocuments(query, userContext, null);
                PagedSearchResponse response = contextResponse.getResponse();
                if(response.getObjectList() != null) task.setDocumentsReceived(response.getObjectList().size());
                if(response.getTotal() > 0) task.setDocumentsFound(response.getTotal());
                task.setRequestProcessingMsec(System.currentTimeMillis() - start);

                task.setCompleted();

                taskId.set(null);

                logger.debug("Document supplementary fields load task completed: {}", task);
            } catch (Exception ex) {
                running = false;

                logger.error("Error returned from supplementary fields task={}, stopping worker id={}", task, id, ex);

                task.setError(ex);

                logger.info("Cleaning supplementary fields task queue after error");

                taskQueue.clear();
            }
        }
    }

    public Integer getTaskId() {
        return taskId.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRequestWork that = (SearchRequestWork) o;
        return id == that.id &&
                Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskId);
    }

    @Override
    public String toString() {
        return "SupplementaryFieldsWork{" +
                "id=" + id +
                ", running=" + running +
                ", taskId=" + taskId +
                '}';
    }
}
