package multithreading;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args)  {
        int maxThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        Callable<Void> task = createTask();

        for (int i = 0; i < maxThreads; i++) {
            executor.submit(task);
        }
        executor.shutdown();
        int globalMax = 0;
        int key = 0;
        System.out.println("Другие размеры:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            System.out.println("- " + entry.getKey() + "(" + entry.getValue()+" раз)");
            if(entry.getValue() > globalMax){
                globalMax = entry.getValue();
                key = entry.getKey();
            }
        }
        System.out.println("Самое частое количество повторений " + key + " (встретилось " + globalMax + "раз)");
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
            synchronized (sizeToFreq) {
                sizeToFreq.put(count, maxSize);
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
}