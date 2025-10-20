package week3;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Eight {

    static void parseBlock(String text, int index, List<String> words, Set<String> stops) {
        int n = text.length();

        while (index < n && !Character.isLetter(text.charAt(index))) index++;

        if (index >= n) return; 

        StringBuilder sb = new StringBuilder();
        while (index < n && Character.isLetter(text.charAt(index))) {
            sb.append(Character.toLowerCase(text.charAt(index)));
            index++;
        }

        String word = sb.toString();
        if (!stops.contains(word)) {
            words.add(word);
        }

        parseBlock(text, index, words, stops);
    }

    public static void main(String[] args) throws Exception {
        String bookPath = "../262/pride-and-prejudice.txt";
        String stopPath = "../262/stop_words.txt";

        Set<String> stops = Arrays.stream(
                        new String(Files.readAllBytes(Paths.get(stopPath))).split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        for (char ch = 'a'; ch <= 'z'; ch++) stops.add(String.valueOf(ch));

        List<String> words = new ArrayList<>();

        try (Reader reader = new FileReader(bookPath)) {
            char[] buffer = new char[100]; 
            int read;
            while ((read = reader.read(buffer)) != -1) {
                String block = new String(buffer, 0, read);
                parseBlock(block, 0, words, stops);
            }
        }

        Map<String, Long> freqs = words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        freqs.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(25)
                .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
    }
}
