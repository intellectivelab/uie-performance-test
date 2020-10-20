package com.intellective.uie.test;

public class SearchRequestStatistics {

    int successfulRequests;
    int failedRequests;
    long execTime;

    long documentsReceived;
    long documentsFound;
    long totalRequestTimeMsec;


    public void incrementSuccessCount() {
        successfulRequests++;
    }

    public void incrementFailedCount() {
        failedRequests++;
    }

    public void incrementDocumentsReceived(long documentsReceived) {
        this.documentsReceived += documentsReceived;
    }

    public void incrementDocumentsFound(long documentsFound) {
        this.documentsFound += documentsFound;
    }

    public void addTotalRequestTimeMsec(long requestTime) {
        this.totalRequestTimeMsec += requestTime;
    }

    public long getTotalRequestTimeMsec() {
        return totalRequestTimeMsec;
    }

    public void addExecTime(long execTime) {
        this.execTime += execTime;
    }

    public long getExecTime() {
        return execTime;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public long getDocumentsReceived() {
        return documentsReceived;
    }

    public long getDocumentsFound() {
        return documentsFound;
    }

    public double getAverageRequestTime() {
        return (successfulRequests + failedRequests) == 0 ? -1 : (1.0 * totalRequestTimeMsec / (successfulRequests + failedRequests));
    }

    @Override
    public String toString() {
        return "Search Test Statistics: " +
                "\n  successful requests count: " + successfulRequests +
                "\n  failed requests count: " + failedRequests +
                "\n  total test time (msec): " + execTime +
                "\n  total requests time (msec): " + totalRequestTimeMsec +
                "\n  documents received: " + documentsReceived +
                "\n  documents found: " + documentsFound +
                "\n  average request time (msec): " + getAverageRequestTime();
    }

}
