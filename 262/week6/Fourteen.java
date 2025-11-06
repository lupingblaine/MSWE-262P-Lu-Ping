// week6/Fourteen.java
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

public class Fourteen {

    private static List<String> partition(String data, int nLines) {
        String[] lines = data.split("\n");
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < lines.length; i += nLines) {
            int end = Math.min(i + nLines, lines.length);
            chunks.add(String.join("\n", Arrays.copyOfRange(lines, i, end)));
        }
        return chunks;
    }


    private static List<Map.Entry<String, Integer>> splitWords(String text, Set<String> stopWords) {
        List<Map.Entry<String, Integer>> pairs = new ArrayList<>();
        Pattern pattern = Pattern.compile("[\\W_]+");
        String cleaned = pattern.matcher(text).replaceAll(" ").toLowerCase();
        for (String w : cleaned.split("\\s+")) {
            if (w.length() > 1 && !stopWords.contains(w)) {
                pairs.add(Map.entry(w, 1));
            }
        }
        return pairs;
    }


    private static Map<String, List<Map.Entry<String, Integer>>> regroup(
            List<List<Map.Entry<String, Integer>>> mapped) {
        Map<String, List<Map.Entry<String, Integer>>> grouped = new HashMap<>();
        for (List<Map.Entry<String, Integer>> chunk : mapped) {
            for (Map.Entry<String, Integer> pair : chunk) {
                grouped.computeIfAbsent(pair.getKey(), k -> new ArrayList<>()).add(pair);
            }
        }
        return grouped;
    }


    private static Map.Entry<String, Integer> countWords(Map.Entry<String, List<Map.Entry<String, Integer>>> entry) {
        int total = entry.getValue().stream().mapToInt(Map.Entry::getValue).sum();
        return Map.entry(entry.getKey(), total);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java Fourteen ../pride-and-prejudice.txt");
            return;
        }

        // Load stop words
        Set<String> stopWords = new HashSet<>();
        String stopText = Files.readString(Paths.get("../stop_words.txt"));
        stopWords.addAll(Arrays.asList(stopText.split(",")));
        for (char c = 'a'; c <= 'z'; c++) stopWords.add(String.valueOf(c));

        // Read and partition text
        String data = Files.readString(Paths.get(args[0]));
        List<String> partitions = partition(data, 200);


        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<List<Map.Entry<String, Integer>>>> futures = new ArrayList<>();
        for (String chunk : partitions) {
            futures.add(executor.submit(() -> splitWords(chunk, stopWords)));
        }

        List<List<Map.Entry<String, Integer>>> mapped = new ArrayList<>();
        for (Future<List<Map.Entry<String, Integer>>> f : futures) {
            mapped.add(f.get());
        }


        Map<String, List<Map.Entry<String, Integer>>> regrouped = regroup(mapped);

        List<Future<Map.Entry<String, Integer>>> reduceFutures = new ArrayList<>();
        for (Map.Entry<String, List<Map.Entry<String, Integer>>> entry : regrouped.entrySet()) {
            reduceFutures.add(executor.submit(() -> countWords(entry)));
        }

        List<Map.Entry<String, Integer>> reduced = new ArrayList<>();
        for (Future<Map.Entry<String, Integer>> f : reduceFutures) {
            reduced.add(f.get());
        }
        executor.shutdown();


        reduced.stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(25)
                .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
    }
}
