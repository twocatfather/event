package com.example.finance.async.examples;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class BasicCompletableFutureExamples {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        basicCreationExample();
        manualCompletionExample();
        supplyAsyncExample();
        runAsyncExample();
    }

    public static void basicCreationExample() throws ExecutionException, InterruptedException {
        // 비어있는 future
        CompletableFuture<String> future = new CompletableFuture<>();

        // 별도의 스레드에서 future를 완료시키는 작업
        new Thread(() -> {
            try {
                log.info("비동기 작업 시작 (별도 스레드: {})", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(1);
                future.complete("작업 완료!");
                log.info("비동기 작업 완료 (별도 스레드: {})", Thread.currentThread().getName());
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        }).start();

        log.info("결과를 기다리는 중... (메인 스레드: {})", Thread.currentThread().getName());
        String result = future.get();
        log.info("받은 결과: {}", result);

    }

    public static void manualCompletionExample() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future =  new CompletableFuture<>();

        // 이미 결과를 알고 있느 ㄴ경우 바로 완료 상태로 만들 수 있다.
        future.complete("미리 완료된 결과");

        String result = future.get();
        log.info("수동으로 완료된 결과: {}", result);

        // 이미 완료된 CompletableFuture를 생성하는 편의 메서드
        CompletableFuture<String> completedFuture = CompletableFuture.completedFuture("처음부터 완료된 상태");
        log.info("completedFuture 결과: {}", completedFuture.get());

        // 예외로 완료된 CompletableFuture 생성
        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("의도적인 오류"));

        try {
            failedFuture.get();
        }catch (ExecutionException e) {
            log.error("예상된 예외 발생: {}", e.getCause().getMessage());
        }
    }

    public static void supplyAsyncExample() throws ExecutionException, InterruptedException {
        Supplier<String> task = () -> {
            try {
                log.info("supplyAsync 작업 실행 중 (스레드: {})", Thread.currentThread());
                TimeUnit.SECONDS.sleep(1);
                return "supplyAsync 작업 결과";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        CompletableFuture<String> future = CompletableFuture.supplyAsync(task);

        log.info("supplyAsync 작업이 백그라운드에서 실행 중이다...");
        String result = future.get();// 결과를 기다린다
        log.info("supplyAsync 결과: {}", result);
    }

    public static void runAsyncExample() throws ExecutionException, InterruptedException {
        Runnable task = () -> {
            try {
                log.info("runAsync 작업 실행 중 (스레드: {})", Thread.currentThread());
                TimeUnit.SECONDS.sleep(1);
                log.info("runAsync 작업완료 (결과 없음)");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        CompletableFuture<Void> future = CompletableFuture.runAsync(task);
        log.info("runAsync 작업이 백그라운드에서 실행 중이다...");
        future.get();// 작업이 완료될 때까지 기다린다(대기한다)
        log.info("runAsync 작업이 완료되었다.");
    }

}
