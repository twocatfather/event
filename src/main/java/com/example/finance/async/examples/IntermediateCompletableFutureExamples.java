package com.example.finance.async.examples;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * CompletableFuture의 중급 사용법을 보여주는 예제 클래스
 * 
 * 이 클래스에서는 CompletableFuture의 체이닝(chaining) 기능과 예외 처리 방법을 다룹니다.
 * 비동기 작업의 결과를 변환하거나 조합하는 다양한 방법을 보여줍니다.
 */
public class IntermediateCompletableFutureExamples {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("===== CompletableFuture 중급 예제 =====");

        // 예제 실행
        thenApplyExample();
        thenAcceptExample();
        thenRunExample();
        thenComposeExample();
        exceptionHandlingExample();

        System.out.println("\n모든 중급 예제가 완료되었습니다.");
    }

    /**
     * 1. thenApply 메서드 예제
     * - 비동기 작업의 결과를 변환하는 방법을 보여줍니다.
     * - thenApply는 Function을 인자로 받아 이전 단계의 결과를 변환합니다.
     */
    public static void thenApplyExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 1. thenApply 예제 -----");

        // 초기 CompletableFuture 생성
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("첫 번째 작업 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
            return "Hello";
        });

        // thenApply로 결과 변환 (String -> String)
        CompletableFuture<String> transformedFuture = future.thenApply(result -> {
            System.out.println("thenApply 작업 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
            return result + " World";
        });

        // 다시 thenApply로 결과 변환 (String -> Integer)
        CompletableFuture<Integer> lengthFuture = transformedFuture.thenApply(result -> {
            System.out.println("두 번째 thenApply 작업 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
            return result.length();
        });

        // 최종 결과 출력
        System.out.println("thenApply 결과: " + lengthFuture.get());

        // 메서드 체이닝 방식으로 한 번에 작성할 수도 있음
        int length = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World")
                .thenApply(String::length)
                .get();

        System.out.println("체이닝 방식 thenApply 결과: " + length);
    }

    /**
     * 2. thenAccept 메서드 예제
     * - 비동기 작업의 결과를 소비하는 방법을 보여줍니다.
     * - thenAccept는 Consumer를 인자로 받아 이전 단계의 결과를 소비하고 값을 반환하지 않습니다.
     */
    public static void thenAcceptExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 2. thenAccept 예제 -----");

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("데이터 생성 중 (스레드: " + Thread.currentThread().getName() + ")");
            return "처리할 데이터";
        }).thenAccept(data -> {
            System.out.println("thenAccept로 데이터 처리 중 (스레드: " + Thread.currentThread().getName() + ")");
            System.out.println("받은 데이터: " + data);
            // 여기서 결과를 처리하지만 반환하지는 않음
        });

        // thenAccept는 값을 반환하지 않으므로 future.get()은 null을 반환
        future.get();
        System.out.println("thenAccept 작업이 완료되었습니다.");
    }

    /**
     * 3. thenRun 메서드 예제
     * - 이전 작업이 완료된 후 추가 작업을 실행하는 방법을 보여줍니다.
     * - thenRun은 Runnable을 인자로 받아 이전 단계의 결과를 사용하지 않고 작업을 실행합니다.
     */
    public static void thenRunExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 3. thenRun 예제 -----");

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("첫 번째 작업 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
            return "이 결과는 사용되지 않습니다";
        }).thenRun(() -> {
            System.out.println("thenRun 작업 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
            System.out.println("이전 작업의 결과에 접근할 수 없고, 단순히 작업이 완료되었음을 알 수 있습니다.");
        });

        future.get();
        System.out.println("thenRun 작업이 완료되었습니다.");
    }

    /**
     * 4. thenCompose 메서드 예제
     * - 두 개의 비동기 작업을 순차적으로 실행하는 방법을 보여줍니다.
     * - thenCompose는 Function을 인자로 받아 이전 단계의 결과를 기반으로 새로운 CompletableFuture를 생성합니다.
     * - 이는 flatMap과 유사한 개념으로, CompletableFuture<CompletableFuture<T>>가 아닌 CompletableFuture<T>를 반환합니다.
     */
    public static void thenComposeExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 4. thenCompose 예제 -----");

        // 사용자 ID로 사용자 정보를 조회하는 비동기 작업
        Function<Long, CompletableFuture<String>> fetchUserInfo = userId -> 
            CompletableFuture.supplyAsync(() -> {
                System.out.println("사용자 정보 조회 중 (ID: " + userId + ", 스레드: " + Thread.currentThread().getName() + ")");
                try {
                    TimeUnit.MILLISECONDS.sleep(500); // 데이터베이스 조회 시간 시뮬레이션
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "User" + userId + " (홍길동)";
            });

        // 사용자 정보로 프로필 정보를 조회하는 비동기 작업
        Function<String, CompletableFuture<String>> fetchUserProfile = userInfo -> 
            CompletableFuture.supplyAsync(() -> {
                System.out.println("프로필 정보 조회 중 (사용자: " + userInfo + ", 스레드: " + Thread.currentThread().getName() + ")");
                try {
                    TimeUnit.MILLISECONDS.sleep(500); // 데이터베이스 조회 시간 시뮬레이션
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return userInfo + "의 프로필 - 직업: 개발자, 나이: 30";
            });

        // thenCompose를 사용하여 두 작업을 순차적으로 연결
        CompletableFuture<String> userProfileFuture = CompletableFuture
                .supplyAsync(() -> 100L) // 사용자 ID 생성
                .thenCompose(fetchUserInfo) // 사용자 정보 조회
                .thenCompose(fetchUserProfile); // 프로필 정보 조회

        String userProfile = userProfileFuture.get();
        System.out.println("최종 결과: " + userProfile);

        // thenApply와 thenCompose의 차이점 설명
        System.out.println("\nthenApply vs thenCompose 차이점:");
        System.out.println("- thenApply: CompletableFuture<CompletableFuture<T>>를 반환 (중첩된 Future)");
        System.out.println("- thenCompose: CompletableFuture<T>를 반환 (평탄화된 Future)");
    }

    /**
     * 5. 예외 처리 예제
     * - CompletableFuture에서 예외를 처리하는 방법을 보여줍니다.
     * - exceptionally, handle, whenComplete 메서드를 사용한 예외 처리 방법을 다룹니다.
     */
    public static void exceptionHandlingExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 5. 예외 처리 예제 -----");

        // 1. exceptionally 메서드를 사용한 예외 처리
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("예외 발생 작업 실행 중");
            if (true) { // 항상 예외 발생
                throw new RuntimeException("의도적인 오류 발생");
            }
            return "이 결과는 반환되지 않습니다";
        }).exceptionally(ex -> {
            System.out.println("exceptionally로 예외 처리: " + ex.getMessage());
            return "예외 발생 시 대체 결과";
        });

        System.out.println("exceptionally 결과: " + future1.get());

        // 2. handle 메서드를 사용한 예외 처리 (성공/실패 모두 처리)
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("정상 작업 실행 중");
            return "정상 결과";
        }).handle((result, ex) -> {
            if (ex != null) {
                System.out.println("handle로 예외 처리: " + ex.getMessage());
                return "예외 발생 시 대체 결과";
            } else {
                System.out.println("handle로 정상 결과 처리");
                return result + " (handle로 처리됨)";
            }
        });

        System.out.println("handle 결과 (정상 케이스): " + future2.get());

        // 3. 예외가 발생하는 경우의 handle
        CompletableFuture<String> future3 = CompletableFuture.<String>supplyAsync(() -> {
            System.out.println("예외 발생 작업 실행 중");
            throw new RuntimeException("의도적인 오류 발생");
        }).handle((result, ex) -> {
            if (ex != null) {
                System.out.println("handle로 예외 처리: " + ex.getMessage());
                return "예외 발생 시 대체 결과";
            } else {
                return result;
            }
        });

        System.out.println("handle 결과 (예외 케이스): " + future3.get());

        // 4. whenComplete 메서드를 사용한 예외 처리 (결과를 변경하지 않음)
        CompletableFuture<String> future4 = CompletableFuture.supplyAsync(() -> {
            System.out.println("정상 작업 실행 중");
            return "whenComplete 결과";
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("whenComplete에서 예외 감지: " + ex.getMessage());
            } else {
                System.out.println("whenComplete에서 정상 결과 감지: " + result);
                // 결과를 변경할 수 없음, 단순히 결과나 예외를 확인만 가능
            }
        });

        System.out.println("whenComplete 최종 결과: " + future4.get());
    }
}
