package com.example.finance.async.examples;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * CompletableFuture의 고급 사용법을 보여주는 예제 클래스
 * 
 * 이 클래스에서는 여러 CompletableFuture를 조합하는 방법, 타임아웃 처리,
 * 커스텀 스레드 풀 사용 등 고급 기능을 다룹니다.
 */
public class AdvancedCompletableFutureExamples {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("===== CompletableFuture 고급 예제 =====");
        
        // 예제 실행
        combiningFuturesExample();
        anyOfExample();
        timeoutExample();
        customExecutorExample();
        realWorldExample();
        
        System.out.println("\n모든 고급 예제가 완료되었습니다.");
    }
    
    /**
     * 1. 여러 CompletableFuture 조합하기 (allOf)
     * - 여러 비동기 작업을 병렬로 실행하고 모두 완료될 때까지 기다리는 방법을 보여줍니다.
     */
    public static void combiningFuturesExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 1. 여러 CompletableFuture 조합하기 (allOf) -----");
        
        // 여러 비동기 작업 생성
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            simulateDelay(500);
            System.out.println("작업 1 완료 (스레드: " + Thread.currentThread().getName() + ")");
            return "결과 1";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            simulateDelay(300);
            System.out.println("작업 2 완료 (스레드: " + Thread.currentThread().getName() + ")");
            return "결과 2";
        });
        
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            simulateDelay(700);
            System.out.println("작업 3 완료 (스레드: " + Thread.currentThread().getName() + ")");
            return "결과 3";
        });
        
        // CompletableFuture.allOf를 사용하여 모든 작업이 완료될 때까지 대기
        System.out.println("모든 작업이 완료될 때까지 대기 중...");
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
        
        // allOf는 CompletableFuture<Void>를 반환하므로 결과값을 직접 얻을 수 없음
        // 각 Future의 결과를 수집하려면 추가 작업 필요
        allFutures.get(); // 모든 작업이 완료될 때까지 대기
        
        // 모든 작업이 완료된 후 각 결과 수집
        String result1 = future1.get();
        String result2 = future2.get();
        String result3 = future3.get();
        
        System.out.println("모든 작업 완료!");
        System.out.println("결과 1: " + result1);
        System.out.println("결과 2: " + result2);
        System.out.println("결과 3: " + result3);
        
        // 스트림을 사용하여 모든 결과를 한 번에 수집하는 방법
        System.out.println("\n스트림을 사용한 결과 수집:");
        List<CompletableFuture<String>> futures = Arrays.asList(future1, future2, future3);
        
        CompletableFuture<List<String>> allResults = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join) // join은 get과 유사하지만 checked exception을 발생시키지 않음
                        .collect(Collectors.toList()));
        
        List<String> results = allResults.get();
        System.out.println("수집된 모든 결과: " + results);
    }
    
    /**
     * 2. 여러 CompletableFuture 중 가장 빨리 완료되는 것 기다리기 (anyOf)
     * - 여러 비동기 작업 중 하나라도 완료되면 결과를 반환하는 방법을 보여줍니다.
     */
    public static void anyOfExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 2. 가장 빨리 완료되는 Future 기다리기 (anyOf) -----");
        
        // 여러 비동기 작업 생성 (각각 다른 지연 시간)
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            simulateDelay(500);
            System.out.println("작업 1 완료 (스레드: " + Thread.currentThread().getName() + ")");
            return "결과 1 (500ms)";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            simulateDelay(200); // 가장 빠른 작업
            System.out.println("작업 2 완료 (스레드: " + Thread.currentThread().getName() + ")");
            return "결과 2 (200ms)";
        });
        
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            simulateDelay(800);
            System.out.println("작업 3 완료 (스레드: " + Thread.currentThread().getName() + ")");
            return "결과 3 (800ms)";
        });
        
        // CompletableFuture.anyOf를 사용하여 가장 빨리 완료되는 작업의 결과를 기다림
        System.out.println("가장 빨리 완료되는 작업을 기다리는 중...");
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(future1, future2, future3);
        
        // anyOf는 가장 빨리 완료된 Future의 결과를 Object 타입으로 반환
        Object firstResult = anyFuture.get();
        System.out.println("가장 빨리 완료된 작업의 결과: " + firstResult);
        
        // 참고: anyOf 이후에도 다른 작업들은 계속 실행됨
        System.out.println("다른 작업들도 계속 실행 중...");
        TimeUnit.SECONDS.sleep(1); // 나머지 작업들이 완료될 시간을 줌
        System.out.println("모든 작업이 완료되었습니다.");
    }
    
    /**
     * 3. 타임아웃 처리 예제
     * - CompletableFuture에 타임아웃을 설정하는 방법을 보여줍니다.
     */
    public static void timeoutExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 3. 타임아웃 처리 예제 -----");
        
        // 오래 걸리는 작업 생성
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("오래 걸리는 작업 시작 (스레드: " + Thread.currentThread().getName() + ")");
            simulateDelay(2000); // 2초 소요되는 작업
            System.out.println("오래 걸리는 작업 완료");
            return "작업 결과";
        });
        
        // Java 9 이상에서는 orTimeout 메서드 사용 가능
        try {
            System.out.println("1초 타임아웃으로 결과 기다리는 중...");
            // 1초 후에 타임아웃 발생
            String result = future.orTimeout(1, TimeUnit.SECONDS).get();
            System.out.println("결과: " + result);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TimeoutException) {
                System.out.println("타임아웃 발생! 작업이 1초 내에 완료되지 않았습니다.");
            } else {
                throw e;
            }
        }
        
        // 타임아웃 시 대체 값 제공 (Java 9 이상)
        try {
            System.out.println("\n타임아웃 시 대체 값 사용:");
            CompletableFuture<String> futureWithTimeout = CompletableFuture.supplyAsync(() -> {
                simulateDelay(2000); // 2초 소요되는 작업
                return "원래 작업 결과";
            }).completeOnTimeout("타임아웃 대체 값", 1, TimeUnit.SECONDS);
            
            String result = futureWithTimeout.get();
            System.out.println("결과: " + result); // 타임아웃 대체 값 출력
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
        }
        
        // Java 8에서의 타임아웃 처리 방법
        System.out.println("\nJava 8에서의 타임아웃 처리 방법:");
        CompletableFuture<String> slowTask = CompletableFuture.supplyAsync(() -> {
            simulateDelay(3000); // 3초 소요되는 작업
            return "느린 작업 결과";
        });
        
        try {
            // Future.get()에 타임아웃 설정
            String result = slowTask.get(1, TimeUnit.SECONDS);
            System.out.println("결과: " + result);
        } catch (TimeoutException e) {
            System.out.println("Java 8 방식의 타임아웃 발생!");
        }
    }
    
    /**
     * 4. 커스텀 스레드 풀 사용 예제
     * - CompletableFuture에 커스텀 Executor를 제공하는 방법을 보여줍니다.
     */
    public static void customExecutorExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 4. 커스텀 스레드 풀 사용 예제 -----");
        
        // 커스텀 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setName("custom-thread-" + t.getId());
            t.setDaemon(true); // 데몬 스레드로 설정 (애플리케이션 종료 시 자동 종료)
            return t;
        });
        
        try {
            System.out.println("기본 스레드 풀 사용:");
            CompletableFuture<String> defaultPoolFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("기본 스레드 풀에서 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
                return "기본 스레드 풀 결과";
            });
            System.out.println(defaultPoolFuture.get());
            
            System.out.println("\n커스텀 스레드 풀 사용:");
            CompletableFuture<String> customPoolFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("커스텀 스레드 풀에서 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
                return "커스텀 스레드 풀 결과";
            }, executor);
            System.out.println(customPoolFuture.get());
            
            // 여러 작업을 커스텀 스레드 풀에서 실행
            System.out.println("\n여러 작업을 커스텀 스레드 풀에서 실행:");
            CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
                System.out.println("작업 1 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
                simulateDelay(500);
                return "작업 1 결과";
            }, executor);
            
            CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
                System.out.println("작업 2 실행 중 (스레드: " + Thread.currentThread().getName() + ")");
                simulateDelay(300);
                return "작업 2 결과";
            }, executor);
            
            CompletableFuture.allOf(task1, task2).get();
            System.out.println("작업 1 결과: " + task1.get());
            System.out.println("작업 2 결과: " + task2.get());
            
        } finally {
            // 스레드 풀 종료
            executor.shutdown();
            System.out.println("커스텀 스레드 풀 종료");
        }
    }
    
    /**
     * 5. 실제 사용 사례 예제
     * - 실제 애플리케이션에서 CompletableFuture를 활용하는 방법을 보여줍니다.
     * - 여러 서비스 호출을 병렬로 처리하는 시나리오를 시뮬레이션합니다.
     */
    public static void realWorldExample() throws ExecutionException, InterruptedException {
        System.out.println("\n----- 5. 실제 사용 사례 예제 -----");
        
        // 사용자 정보 조회 서비스 (외부 API 호출 시뮬레이션)
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("사용자 정보 조회 중... (스레드: " + Thread.currentThread().getName() + ")");
            simulateDelay(500); // 외부 API 호출 지연 시뮬레이션
            return new User(1L, "홍길동", "hong@example.com");
        });
        
        // 주문 내역 조회 서비스 (외부 API 호출 시뮬레이션)
        CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("주문 내역 조회 중... (스레드: " + Thread.currentThread().getName() + ")");
            simulateDelay(700); // 외부 API 호출 지연 시뮬레이션
            return Arrays.asList(
                    new Order(101L, "상품A", 15000),
                    new Order(102L, "상품B", 25000)
            );
        });
        
        // 포인트 정보 조회 서비스 (외부 API 호출 시뮬레이션)
        CompletableFuture<Integer> pointsFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("포인트 정보 조회 중... (스레드: " + Thread.currentThread().getName() + ")");
            simulateDelay(300); // 외부 API 호출 지연 시뮬레이션
            return 5000; // 포인트 잔액
        });
        
        // 세 가지 정보를 모두 조합하여 사용자 대시보드 정보 생성
        CompletableFuture<Dashboard> dashboardFuture = userFuture.thenCombine(
                ordersFuture, (user, orders) -> {
                    // 사용자와 주문 정보를 결합
                    return new Dashboard(user, orders, 0); // 포인트는 나중에 설정
                }).thenCombine(pointsFuture, (dashboard, points) -> {
                    // 포인트 정보 추가
                    dashboard.setPoints(points);
                    return dashboard;
                });
        
        // 최종 결과 출력
        Dashboard dashboard = dashboardFuture.get();
        System.out.println("\n사용자 대시보드 정보:");
        System.out.println("사용자: " + dashboard.getUser().getName() + " (" + dashboard.getUser().getEmail() + ")");
        System.out.println("주문 내역:");
        for (Order order : dashboard.getOrders()) {
            System.out.println("  - " + order.getProductName() + ": " + order.getAmount() + "원");
        }
        System.out.println("포인트: " + dashboard.getPoints() + "점");
        
        // 총 소요 시간 계산 (병렬 처리로 인해 가장 오래 걸리는 작업 시간에 가까움)
        System.out.println("\n병렬 처리 효과:");
        System.out.println("순차 처리 예상 시간: 약 1500ms (500ms + 700ms + 300ms)");
        System.out.println("병렬 처리 예상 시간: 약 700ms (가장 오래 걸리는 작업 시간)");
    }
    
    // 지연 시간을 시뮬레이션하는 유틸리티 메서드
    private static void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // 예제용 클래스들
    static class User {
        private final Long id;
        private final String name;
        private final String email;
        
        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
    
    static class Order {
        private final Long id;
        private final String productName;
        private final int amount;
        
        public Order(Long id, String productName, int amount) {
            this.id = id;
            this.productName = productName;
            this.amount = amount;
        }
        
        public Long getId() { return id; }
        public String getProductName() { return productName; }
        public int getAmount() { return amount; }
    }
    
    static class Dashboard {
        private final User user;
        private final List<Order> orders;
        private int points;
        
        public Dashboard(User user, List<Order> orders, int points) {
            this.user = user;
            this.orders = orders;
            this.points = points;
        }
        
        public User getUser() { return user; }
        public List<Order> getOrders() { return orders; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
    }
}