package sample;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AppTest {

    @LocalServerPort
    private int port;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MeterRegistry meterRegistry;

    final static int THREAD_POOL_SIZE = 30;
    final static int TASKS_NUMBER = 30;
    final static int REQUESTS_PER_TASK = 20;

    final static Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    void test() throws Exception {
        // Can be skipped it if the test duration is well known for your environment
        log.info("---- run preliminary test to estimate required timings ----");
        Duration duration = estimateRequiredTimings();
        log.info(">>> preliminary load test took " + duration);
        log.info("---------------------- test itself ------------------------");
        actualTest(duration);
    }

    private void actualTest(Duration duration) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        loadApplication(es);

        Thread.sleep(duration.toMillis() / 2);

        log.info("----------------------- shutdown --------------------------");
        es.shutdownNow();
        es.awaitTermination(0, TimeUnit.MILLISECONDS);

        /*
        Wait for the application finishes
         */
        Thread.sleep(duration.toMillis());

        log.info("------------------------- idle ----------------------------");
        log.info("------------------------- idle ----------------------------");
        log.info("------------------------- idle ----------------------------");

        double acquired = getMetricValue("r2dbc.pool.acquired");
        assertEquals(0.0, acquired,
                "expected r2dbc.pool.acquired to be 0.0, but was " + acquired);
    }

    private Duration estimateRequiredTimings() throws InterruptedException {
        var es = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        var start = Instant.now();
        loadApplication(es);
        es.shutdown();
        Duration duration;
        if (es.awaitTermination(120, TimeUnit.SECONDS)) {
            duration = Duration.between(start, Instant.now());
        } else {
            throw new RuntimeException("Too heavy load");
        }
        return duration;
    }

    private void loadApplication(ExecutorService es) {
        final String url = "http://localhost:" + port + "/payment";
        Runnable task = () -> {
            for (int i = 0; i < REQUESTS_PER_TASK; i++) {
                makeHTTPRequestForPayment(url);
            }
        };
        for (int i = 0; i < TASKS_NUMBER; i++) {
            es.submit(task);
        }
    }

    private void makeHTTPRequestForPayment(String url) {
        try {
            ResponseEntity<String> response = restTemplate
                    .postForEntity(url, null, String.class);
            assertEquals("OK", response.getBody());
        } catch (Exception e) {
            if ((e.getCause() instanceof SocketTimeoutException)) {
                return; // suppress timeouts
            }
            log.error("Error: " + e.getMessage());
        }
    }

    private double getMetricValue(String key) {
        Gauge acquired = meterRegistry.find(key).gauge();
        if (acquired == null) {
            throw new RuntimeException("no " + key + " metrics found");
        }
        return acquired.value();
    }
}