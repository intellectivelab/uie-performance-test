package com.intellective.uie.test;


import com.intellective.commons.security.VuUserContext;
import com.intellective.uie.repository.StopIndicator;
import com.intellective.uie.search.client.UieClient;
import com.intellective.uie.transport.message.PagedSearchRequest;
import commonj.work.*;
import de.myfoo.commonj.util.ThreadPool;
import de.myfoo.commonj.work.FooWorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchRequestExecutor implements WorkListener {

    protected static final Logger logger = LoggerFactory.getLogger(SearchRequestExecutor.class);


    protected final WorkManager workManager;
    protected final int numberOfParallelWorkers;

    protected final UieClient client;
    protected final List<PagedSearchRequest> searchRequests;
    protected final List<VuUserContext> userContexts;
    protected final  int taskCount;

    public SearchRequestExecutor(UieClient client, int taskCount, int numberOfParallelWorkers, List<PagedSearchRequest> searchRequests, List<VuUserContext> userContexts) {
        this.taskCount = taskCount;
        this.numberOfParallelWorkers = numberOfParallelWorkers;
        this.client = client;
        this.searchRequests = searchRequests;
        this.userContexts = userContexts;
        this.workManager = getWorkManager(numberOfParallelWorkers);
    }

    protected WorkManager getWorkManager(int numberOfParallelWorkers) {
         // create the thread pool for this work manager
        final ThreadPool pool = new ThreadPool(numberOfParallelWorkers, numberOfParallelWorkers, 20);
        WorkManager workManager = new FooWorkManager(pool);

        return workManager;
    }


    @Override
    public void workAccepted(WorkEvent workEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Work 'accepted': {}", toLog(workEvent));
        }
    }

    @Override
    public void workCompleted(WorkEvent workEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Work 'COMPLETED': {}", toLog(workEvent));
        }
    }

    @Override
    public void workRejected(WorkEvent workEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Work 'rejected': {}", toLog(workEvent));
        }
    }

    @Override
    public void workStarted(WorkEvent workEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Work 'STARTED': {}", toLog(workEvent));
        }
    }

    private String toLog(WorkEvent workEvent) {
        String result;
        try {
            result = workEvent.getWorkItem().getResult().toString();
        } catch (WorkException e) {
            result = e.getCause().getClass().getName() + " : " + e.getCause().getMessage();
        }
        return result;
    }


    /**
     * Returns input documents with content, security or other supplementary fields field
     *
     * @param stopIndicator
     * @return list of input documents with content field (parsed content text)
     */
    public synchronized SearchRequestStatistics process(final StopIndicator stopIndicator)
    {

        final List<SearchRequestTask> tasks = new ArrayList<>();
        for(int i = 0; i < taskCount; i++) {
            tasks.add(createTask(i));
        }

        int workerCount = numberOfParallelWorkers > tasks.size() ? tasks.size() : numberOfParallelWorkers;


        logger.debug("Starting concurrent supplementary load: tasks: {}, workers: {}", tasks.size(), workerCount);

        SearchRequestStatistics statistics = processTasks(tasks, workerCount, stopIndicator);

        logger.debug("Completed search requests: {}", statistics);

        return statistics;

    }

    protected SearchRequestWork createWork(int id, BlockingQueue<SearchRequestTask> taskQueue,
                                                 StopIndicator stopIndicator) {
        return new SearchRequestWork(id, taskQueue, client, stopIndicator);
    }

    protected SearchRequestStatistics processTasks(final List<SearchRequestTask> tasks,
                                                                 final int workerCount,
                                                                 final StopIndicator stopIndicator) {

        SearchRequestStatistics statistics = new SearchRequestStatistics();

        try {
            assert workerCount >= 0;

            if(tasks == null || tasks.isEmpty()) {
                logger.debug("Tasks list is empty or null, document content retrieval skipped");

                return statistics;
            }

            int tasksCount = tasks.size();

            final BlockingQueue<SearchRequestTask> taskQueue = new LinkedBlockingQueue<>(tasks);

            long execTime;
            long start;

            if(workerCount <= 1) {
                //Process in the same thread

                logger.debug("Running {} search tasks in single thread", tasks.size());

                start = System.currentTimeMillis();

                SearchRequestWork downloadWork = createWork(0, taskQueue, stopIndicator);

                downloadWork.run();

                execTime = System.currentTimeMillis() - start;
            } else {

                final List<WorkItem> workItems = new ArrayList<>(workerCount);
                for (int workerId = 0; workerId < workerCount; workerId++) {
                    SearchRequestWork searchWork = createWork(workerId, taskQueue, stopIndicator);


                    workItems.add(workManager.schedule(searchWork, this));
                }


                long totalTimeout = Math.round((120000.0 * taskCount ) / workerCount);
                logger.debug("Running {} search tasks, workers={}, timeout={} msec", tasks.size(), workerCount, totalTimeout);

                start = System.currentTimeMillis();

                workManager.waitForAll(workItems, totalTimeout);

                execTime = System.currentTimeMillis() - start;
            }

            logger.debug("Search tasks completed");

            for(SearchRequestTask task : tasks) {
                if (task.isCompleted() && task.getError() == null) {

                    statistics.incrementSuccessCount();
                } else {
                    statistics.incrementFailedCount();
                }
                statistics.incrementDocumentsFound(task.getDocumentsFound());
                statistics.incrementDocumentsReceived(task.getDocumentsReceived());
                statistics.addTotalRequestTimeMsec(task.getRequestProcessingMsec());
            }

            statistics.addExecTime(execTime);

            return statistics;
        } catch (WorkException we) {
            throw new RuntimeException("Error when fetching documents content. Problem: " + we.getMessage(), we);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument when fetching documents content. Problem: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.warn("Interrupted when fetching documents content. Problem: " + e.getMessage(), e);

            Thread.currentThread().interrupt();

            return statistics;
        }
    }



    protected SearchRequestTask createTask(int taksId) {
        PagedSearchRequest searchRequest = searchRequests.get((int)Math.round(Math.random() * (searchRequests.size() - 1)));
        VuUserContext userContext = userContexts.get((int)Math.round(Math.random() * (userContexts.size() - 1)));

        SearchRequestTask task = new SearchRequestTask(taksId, searchRequest, userContext);

        return task;
    }

}
