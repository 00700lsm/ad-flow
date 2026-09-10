package com.adflow.web;

import com.adflow.event.AdEventRepository;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.datasource.url=jdbc:h2:mem:workerpool;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@Import(ServingEventWorkerPoolCouplingTest.HoldWorkerSaveConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ServingEventWorkerPoolCouplingTest {

    private static final long HOLD_MS = 400L;
    static final CountDownLatch HOLDING = new CountDownLatch(1);

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void getAdsWaitsWhileWorkerInsertHoldsConnection() throws Exception {
        int campaignId = createCampaign();
        int creativeId = addCreative(campaignId);

        long postStarted = System.nanoTime();
        mockMvc.perform(post("/events/impression")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "worker-pool-1",
                                  "campaignId": %d,
                                  "creativeId": %d,
                                  "userId": 1,
                                  "contentId": 1
                                }
                                """.formatted(campaignId, creativeId)))
                .andExpect(status().isCreated());
        long postMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - postStarted);

        assertThat(HOLDING.await(2, TimeUnit.SECONDS))
                .as("worker save should open a connection")
                .isTrue();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Long> getAdsMs = executor.submit(() -> {
                long started = System.nanoTime();
                mockMvc.perform(get("/ads").param("userId", "1").param("contentId", "1"))
                        .andExpect(status().isOk());
                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            });
            long getAdsWaitMs = getAdsMs.get(5, TimeUnit.SECONDS);

            Files.writeString(
                    Path.of(".agent/artifacts/T4-04/measurement.txt"),
                    "workerHoldMs=%d getAdsWaitMs=%d postMs=%d pool=1%n"
                            .formatted(HOLD_MS, getAdsWaitMs, postMs)
            );

            assertThat(postMs)
                    .as("POST /events/impression should return before worker persist")
                    .isLessThan(150L);
            assertThat(getAdsWaitMs)
                    .as("GET /ads should wait while worker insert holds the only connection")
                    .isGreaterThanOrEqualTo(300L);
        } finally {
            executor.shutdownNow();
        }
    }

    @TestConfiguration
    static class HoldWorkerSaveConfig {
        @Bean
        static BeanPostProcessor holdWorkerSave(PlatformTransactionManager transactionManager) {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String beanName) {
                    if (bean instanceof AdEventRepository repository) {
                        return Proxy.newProxyInstance(
                                AdEventRepository.class.getClassLoader(),
                                new Class<?>[]{AdEventRepository.class},
                                (proxy, method, args) -> invoke(repository, tx, method, args)
                        );
                    }
                    return bean;
                }
            };
        }

        private static Object invoke(
                AdEventRepository delegate,
                TransactionTemplate tx,
                java.lang.reflect.Method method,
                Object[] args
        ) throws Throwable {
            if ("save".equals(method.getName()) && args != null && args.length == 1) {
                return tx.execute(status -> {
                    try {
                        Object saved = method.invoke(delegate, args);
                        HOLDING.countDown();
                        Thread.sleep(HOLD_MS);
                        return saved;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    } catch (InvocationTargetException e) {
                        throw unwrap(e);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                throw unwrap(e);
            }
        }

        private static RuntimeException unwrap(InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                return runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            return new IllegalStateException(cause);
        }
    }

    private int createCampaign() throws Exception {
        String response = mockMvc.perform(post("/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "worker-pool",
                                  "budget": 50000,
                                  "startAt": "2026-01-01T00:00:00Z",
                                  "endAt": "2026-12-31T23:59:59Z",
                                  "priority": 5,
                                  "targetAgeMin": 20,
                                  "targetAgeMax": 39,
                                  "targetCategory": "스포츠",
                                  "frequencyCap": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }

    private int addCreative(int campaignId) throws Exception {
        String response = mockMvc.perform(post("/campaigns/" + campaignId + "/creatives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "CARD",
                                  "mediaUrl": "/ads/iphone.svg",
                                  "clickUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(response).get("id").asInt();
    }
}
