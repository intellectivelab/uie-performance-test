package com.intellective.uie.test;

import com.intellective.commons.security.VuUserContext;
import com.intellective.uie.repository.StopIndicator;
import com.intellective.uie.search.client.UieHttpClient;
import com.intellective.uie.transport.message.PagedSearchRequest;
import com.intellective.uie.transport.serde.json.UieJsonParser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class PerformanceTest {


    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    UieJsonParser jsonParser = new UieJsonParser() {
        @Override
        protected Set<Class<?>> scanTypeAdapters(ClassLoader... classLoaders) {
            return Collections.emptySet();
        }
    };


    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("application.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            PerformanceTest test = new PerformanceTest();
            test.run(properties);
        } finally {
            System.exit(0);
        }
    }

    private void  run(Properties properties ) {


        int tasksCount = Integer.parseInt(properties.getProperty("request.count", "1000"));
        int threadCount = Integer.parseInt(properties.getProperty("request.threads", "5"));
        URL uuieURL = null;
        try {
            uuieURL = new URL(properties.getProperty("uie.url"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        UieHttpClient client = new UieHttpClient(uuieURL);

        List<PagedSearchRequest> queries = loadQueries();
        List<VuUserContext> contexts = getUserContexts();

        SearchRequestExecutor executor = new SearchRequestExecutor(client, tasksCount, threadCount, queries, contexts);

        StopIndicator stopIndicator = new StopIndicator();
        SearchRequestStatistics statistics = executor.process(stopIndicator);


    }


    private List<PagedSearchRequest> loadQueries() {

        List<PagedSearchRequest> queries = new ArrayList<>();

        Path queriesPath = Paths.get("queries");

        logger.info("Scanning search queries folder: {}", queriesPath);

        try (Stream<Path> paths = Files.walk(queriesPath)) {
            paths.forEach((path) -> {
                if (Files.isDirectory(path)) {
                    return;
                }

                if ("file".equalsIgnoreCase(path.toUri().getScheme())) {
                    if (!"json".equalsIgnoreCase(FilenameUtils.getExtension(new File(path.toUri()).getName()))) {
                        logger.info("File is not json file, ignored: {}", path);
                        return;
                    }
                }

                try {
                    URI uri = path.toUri();
                    String schema = uri.getScheme();

                    logger.info("Loading topology: {}, schema: {}", path, schema, uri.toURL());

                    File file = path.toFile();
                    try(FileInputStream fis = new FileInputStream(file)) {
                        PagedSearchRequest query = jsonParser.fromJson(fis, PagedSearchRequest.class);
                        queries.add(query);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return queries;
    }

    private List<VuUserContext> getUserContexts() {
        List<VuUserContext> userContexts = new ArrayList<>();

        Path usersPath = Paths.get("users");

        logger.info("Scanning user contexts folder: {}", usersPath);

        try (Stream<Path> paths = Files.walk(usersPath)) {
            paths.forEach((path) -> {
                if (Files.isDirectory(path)) {
                    return;
                }

                if ("file".equalsIgnoreCase(path.toUri().getScheme())) {
                    if (!"json".equalsIgnoreCase(FilenameUtils.getExtension(new File(path.toUri()).getName()))) {
                        logger.info("File is not json file, ignored: {}", path);
                        return;
                    }
                }

                try {
                    URI uri = path.toUri();
                    String schema = uri.getScheme();

                    logger.info("Loading topology: {}, schema: {}", path, schema, uri.toURL());

                    VuUserContext context = jsonParser.fromJson(new FileInputStream(path.toFile()), VuUserContext.class);

                    userContexts.add(context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return userContexts;
    }

}
