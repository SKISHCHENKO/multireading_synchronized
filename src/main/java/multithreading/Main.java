package multithreading;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object lock = new Object();
    private static volatile boolean finished = false;

    public static void main(String[] args) throws InterruptedException {
        int maxThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        // Submit counting tasks
        for (int i = 0; i < maxThreads; i++) {
            executor.submit(createTask());
        }

        // Submit printer thread
        new Thread(new PrinterThread((key, value) -> System.out.println("Самое частое количество повторений " + key + " (встретилось " + value + " раз)"))).start();
        new Thread(new PrinterThread((key, value) -> System.out.println("Лидер: " + key + " (встретилось " + value + " раз)"))).start();

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // Signal printer threads to finish
        synchronized (lock) {
            finished = true;
            lock.notifyAll();
        }
    }

    private static Callable<Void> createTask() {
        return () -> {
            String text = generateText("RLRFR", 100);
            int maxSize = 0;
            int count = 0;

            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == 'R') {
                    count++;
                }
                for (int j = i + 1; j < text.length(); j++) {
                    boolean bFound = false;
                    for (int k = i; k < j; k++) {
                        if (text.charAt(k) == 'R') {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound && maxSize < j - i) {
                        maxSize = j - i;
                    }
                }
            }

            synchronized (lock) {
                sizeToFreq.put(count, maxSize);
                lock.notifyAll();
            }

            return null;
        };
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static class PrinterThread implements Runnable {
        private final BiConsumer<Integer, Integer> printAction;
        private int globalMax;
        private int key;
        private int leaderKey;
        private int leaderValue;

        public PrinterThread(BiConsumer<Integer, Integer> printAction) {
            this.printAction = printAction;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    if (sizeToFreq.isEmpty() && !finished) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else {
                        if (finished) {
                            return;
                        }
                        
                        lock.notifyAll();

                        globalMax = 0;
                        key = 0;
                        leaderKey = 0;
                        leaderValue = 0;

                        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                            if (entry.getValue() > globalMax) {
                                globalMax = entry.getValue();
                                key = entry.getKey();
                            }
                            if (entry.getValue() >= leaderValue) {
                                leaderValue = entry.getValue();
                                leaderKey = entry.getKey();
                            }
                        }
                    }
                }

                printAction.accept(key, globalMax);
                printAction.accept(leaderKey, leaderValue);
            }
        }
    }
}