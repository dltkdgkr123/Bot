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
import org.sh.attack.scenario.spec.AttackScenario;

public class AttackRunner {

  private final int threads;
  private final int requestsPerThread;
  private final boolean burst;
  private final int delayMillis = 10;

  private final AtomicInteger success = new AtomicInteger();
  private final AtomicInteger fail = new AtomicInteger();
  private final AtomicInteger globalSequence = new AtomicInteger(1);

  private final HttpClient client;
  private final AttackScenario scenario;

  public AttackRunner(AttackConfig config) {
    this.scenario = ScenarioRequestResolver.resolve(config);
    this.client = HttpClient.newHttpClient();
    this.threads = config.threads();
    this.requestsPerThread = config.requestsPerThread();
    this.burst = config.burst();
  }

  public void run() {
    try {
      ExecutorService pool = Executors.newFixedThreadPool(this.threads);
      CountDownLatch latch = new CountDownLatch(this.burst ? 1 : this.threads);

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
      printResult(end - start);

    } catch (InterruptedException e) {
      e.fillInStackTrace();
    }
  }

  private void worker(CountDownLatch latch) {
    try {
      latch.await();

      for (int i = 0; i < this.requestsPerThread; i++) {
        int seq = globalSequence.getAndIncrement();
        HttpRequest request = scenario.toRequest(seq);

        HttpResponse<Void> res = this.client.send(request, HttpResponse.BodyHandlers.discarding());

        if (res.statusCode() == 200) {
          success.incrementAndGet();
        } else {
          fail.incrementAndGet();
        }

        if (this.burst) {
          Thread.sleep(this.delayMillis);
        }
      }
    } catch (IOException | InterruptedException e) {
      e.fillInStackTrace();
      fail.incrementAndGet();
    }
  }

  private void printResult(long elapsedMs) {
    int total = success.get() + fail.get();
    System.out.println("=== RESULT ===");
    System.out.println("Total Requests : " + total);
    System.out.println("Success        : " + success.get());
    System.out.println("Fail           : " + fail.get());
    System.out.println("Elapsed(ms)    : " + elapsedMs);
    System.out.println("RPS            : " + (elapsedMs > 0 ? (total * 1000L / elapsedMs) : 0));
  }
}