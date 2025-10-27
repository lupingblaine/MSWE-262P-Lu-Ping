import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class Nine {

    public static void main(String[] args) {
        String textPath = "../pride-and-prejudice.txt";
        readFile(textPath, Nine::filterChars);
    }


    static void readFile(String path, Consumer<String> next) {
        try {
            String data = Files.readString(Path.of(path));
            next.accept(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void filterChars(String data) {
        String cleaned = data.replaceAll("[\\W_]+", " ");
        normalize(cleaned);
    }


    static void normalize(String data) {
        scan(data.toLowerCase());
    }


    static void scan(String data) {
        List<String> words = Arrays.asList(data.split("\\s+"));
        removeStopWords(words);
    }


    static void removeStopWords(List<String> words) {
        try {
            List<String> stopWords = new ArrayList<>(Arrays.asList(
                    Files.readString(Path.of("../stop_words.txt")).trim().split(",")));
            stopWords.addAll(Arrays.asList("a","b","c","d","e","f","g","h","i","j","k",
                    "l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"));

            List<String> filtered = words.stream()
                    .filter(w -> !stopWords.contains(w))
                    .toList();

            frequencies(filtered);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void frequencies(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            freq.put(w, freq.getOrDefault(w, 0) + 1);
        }
        sortAndPrint(freq);
    }

    static void sortAndPrint(Map<String, Integer> freq) {
        freq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(25)
            .forEach(e -> System.out.printf("%-15s - %d%n", e.getKey(), e.getValue()));
    }
}
