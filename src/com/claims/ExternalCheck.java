package com.claims;

import com.claims.model.Claim;

import java.util.Random;
import java.util.concurrent.*;


public class ExternalCheck {
    public enum Result { SUCCESS, TRANSIENT_ERROR, PERMANENT_ERROR, TIMEOUT }

    private static final Random random = new Random();

    public static Result check(Claim claim, int timeoutMs) {
        Callable<Result> task = () -> {
            int r = random.nextInt(100);
            if (r < 70) return Result.SUCCESS;
            if (r < 85) return Result.TRANSIENT_ERROR;
            return Result.PERMANENT_ERROR;
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Result> future = executor.submit(task);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return Result.TIMEOUT;
        } catch (Exception e) {
            return Result.PERMANENT_ERROR;
        } finally {
            executor.shutdownNow();
        }
    }
}
