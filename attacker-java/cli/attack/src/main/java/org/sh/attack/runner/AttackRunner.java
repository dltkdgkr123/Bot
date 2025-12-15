package org.sh.attack.runner;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.factory.ScenarioRequestResolver;

public class AttackRunner {

  private final int threads;
  private final int requestsPerThread;
  private boolean burst;
  private final int delayMillis = 10;

  AtomicInteger success = new AtomicInteger();
  AtomicInteger fail = new AtomicInteger();

  private final HttpClient client;
  private final HttpRequest request;

  public AttackRunner(AttackConfig config) {
    this.request = ScenarioRequestResolver.create(config);
    this.client = HttpClient.newHttpClient();
    this.threads = config.threads();
    this.requestsPerThread = config.requestsPerThread();
    this.burst = config.burst();
  }

  public void run() {
    try {
      ExecutorService pool =
          Executors.newFixedThreadPool(this.threads);

      CountDownLatch latch = new CountDownLatch(
          this.burst ? 1 : this.threads
      );

      long start = System.currentTimeMillis();

      for (int i = 0; i < this.threads; i++) {
        pool.submit(() -> worker(latch));
        if (!this.burst) {
          latch.countDown();
        }
      }

      if (this.burst) {
        latch.countDown();
      }
      pool.shutdown();
      pool.awaitTermination(10, TimeUnit.MINUTES);

      long end = System.currentTimeMillis();
      int total = success.get() + fail.get();

      System.out.println("=== RESULT ===");
      System.out.println("Total Requests : " + total);
      System.out.println("Success        : " + success.get());
      System.out.println("Fail           : " + fail.get());
      System.out.println("Elapsed(ms)    : " + (end - start));
      System.out.println("RPS            : " + (total * 1000L / (end - start)));

    } catch (InterruptedException ignored) {

    }
  }

  private void worker(CountDownLatch latch) {
    try {
      latch.await();

      for (int i = 0; i < this.requestsPerThread; i++) {
        HttpResponse<Void> res =
            this.client.send(this.request, HttpResponse.BodyHandlers.discarding());

        if (res.statusCode() == 200) {
          success.incrementAndGet();
        } else {
          fail.incrementAndGet();
        }

        if (this.burst) {
          Thread.sleep(this.delayMillis);
        }
      }
    } catch (IOException | InterruptedException ignored) {
    }
  }
}

