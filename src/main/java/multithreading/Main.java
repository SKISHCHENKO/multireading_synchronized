package multithreading;

import java.util.*;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        int maxThreads = 1000;
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < maxThreads; i++) {
            Runnable logic = () -> {
                String text = generateText("RLRFR", 100);
                int countR = (int) text.chars()
                        .filter(c -> c == 'R')
                        .count();
                synchronized (sizeToFreq) {
                    sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
                }
            };
            Thread thread = new Thread(logic);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join(); 
        }

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
        System.out.println("Самое частое количество повторений " + key + " (встретилось " + globalMax + " раз)");
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