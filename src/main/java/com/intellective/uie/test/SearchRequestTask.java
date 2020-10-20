package com.intellective.uie.test;


import com.intellective.commons.security.VuUserContext;
import com.intellective.search.hli.v1.PagedSearchQuery;
import com.intellective.uie.transport.message.PagedSearchRequest;

import java.util.Objects;

public class SearchRequestTask {

    private final int taskId;
    private final VuUserContext userContext;
    private final PagedSearchRequest searchRequest;
    private volatile boolean completed;
    long documentsReceived;
    long documentsFound;
    long requestProcessingMsec;
    private Exception error;

    public SearchRequestTask(int taskId, PagedSearchRequest searchRequest, VuUserContext userContext) {
        assert searchRequest != null;

        this.searchRequest = searchRequest;
        this.userContext = userContext;
        this.taskId = taskId;
    }

    public VuUserContext getUserContext() {
        return userContext;
    }

    public PagedSearchRequest getSearchRequest() {
        return searchRequest;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public int getTaskId() {
        return taskId;
    }

    public long getDocumentsReceived() {
        return documentsReceived;
    }

    public void setDocumentsReceived(long documentsReceived) {
        this.documentsReceived = documentsReceived;
    }

    public long getDocumentsFound() {
        return documentsFound;
    }

    public void setDocumentsFound(long documentsFound) {
        this.documentsFound = documentsFound;
    }

    public long getRequestProcessingMsec() {
        return requestProcessingMsec;
    }

    public void setRequestProcessingMsec(long requestProcessingMsec) {
        this.requestProcessingMsec = requestProcessingMsec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRequestTask that = (SearchRequestTask) o;
        return taskId == that.taskId &&
                Objects.equals(userContext, that.userContext) &&
                Objects.equals(searchRequest, that.searchRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userContext, searchRequest);
    }

    @Override
    public String toString() {
        return "SearchRequestTask{" +
                "taskId=" + taskId +
                ", userContext=" + userContext +
                ", searchRequest=" + searchRequest +
                ", completed=" + completed +
                ", documentsReceived=" + documentsReceived +
                ", documentsFound=" + documentsFound +
                ", requestProcessingMsec=" + requestProcessingMsec +
                ", error=" + error +
                '}';
    }
}
