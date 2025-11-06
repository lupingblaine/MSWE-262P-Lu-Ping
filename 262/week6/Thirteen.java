
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class Thirteen {

    private static final BlockingQueue<String> wordSpace = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Map<String, Integer>> freqSpace = new LinkedBlockingQueue<>();

    // Stop words set
    private static final Set<String> stopWords = new HashSet<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java Thirteen ../pride-and-prejudice.txt");
            return;
        }

        String stopText = Files.readString(Paths.get("../stop_words.txt"));
        stopWords.addAll(Arrays.asList(stopText.split(",")));

        String data = Files.readString(Paths.get(args[0])).toLowerCase();
        Matcher m = Pattern.compile("[a-z]{2,}").matcher(data);
        while (m.find()) {
            wordSpace.put(m.group());
        }

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> processWords());
            workers.add(t);
            t.start();
        }

        // Wait for all workers to finish
        for (Thread t : workers) {
            t.join();
        }

        Map<String, Integer> wordFreqs = new HashMap<>();
        while (!freqSpace.isEmpty()) {
            Map<String, Integer> freqs = freqSpace.take();
            for (Map.Entry<String, Integer> e : freqs.entrySet()) {
                wordFreqs.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }

        // Sort and print top 25
        wordFreqs.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(25)
                .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
    }

    // Worker function that consumes words and emits partial frequencies
    private static void processWords() {
        Map<String, Integer> localFreqs = new HashMap<>();
        while (true) {
            String word;
            try {
                word = wordSpace.poll(1, TimeUnit.SECONDS);
                if (word == null) break;
            } catch (InterruptedException e) {
                return;
            }

            if (!stopWords.contains(word)) {
                localFreqs.merge(word, 1, Integer::sum);
            }
        }
        freqSpace.add(localFreqs);
    }
}
