package multithreading;

import java.util.*;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object lock = new Object();
    private static int completedThreads = 0;
    private static final int TOTAL_THREADS = 1000;

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < TOTAL_THREADS; i++) {
            Runnable logic = () -> {
                String text = generateText("RLRFR", 100);
                int countR = (int) text.chars()
                        .filter(c -> c == 'R')
                        .count();

                synchronized (lock) {
                    sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
                    completedThreads++;
                    lock.notify();
                }
            };

            Thread thread = new Thread(logic);
            threads.add(thread);
            thread.start();
        }

        Runnable printer = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    synchronized (lock) {
                        while (completedThreads < TOTAL_THREADS) {
                            lock.wait(); // Ожидание сигнала о завершении потока
                        }
                        int globalMax = 0;
                        int key = 0;
                        System.out.println("Другие размеры:");
                        synchronized (sizeToFreq) {
                            for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                                System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
                                if (entry.getValue() > globalMax) {
                                    globalMax = entry.getValue();
                                    key = entry.getKey();
                                }
                            }
                        }
                        System.out.println("Самое частое количество повторений " + key + " (встретилось " + globalMax + " раз)");
                        completedThreads = 0;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread threadPrinter = new Thread(printer);
        threadPrinter.start();

        for (Thread thread : threads) {
            thread.join();
        }

        threadPrinter.interrupt();
        threadPrinter.join();
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}