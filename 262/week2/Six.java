package week2;

import java.io.*;
import java.util.*;

public class Six {

    static String readFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line).append(" ");
        br.close();
        return sb.toString();
    }

    static String filterCharsAndNormalize(String strData) {
        return strData.replaceAll("[\\W_]+", " ").toLowerCase();
    }

    static List<String> scan(String strData) {
        return Arrays.asList(strData.split("\\s+"));
    }

    static List<String> removeStopWords(List<String> wordList) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("stop_words.txt"));
        String[] stopWords = br.readLine().split(",");
        br.close();
        Set<String> stopSet = new HashSet<>(Arrays.asList(stopWords));
        for (char c = 'a'; c <= 'z'; c++) stopSet.add(String.valueOf(c));

        List<String> filtered = new ArrayList<>();
        for (String w : wordList)
            if (!stopSet.contains(w))
                filtered.add(w);
        return filtered;
    }

    static Map<String, Integer> frequencies(List<String> words) {
        Map<String, Integer> wordFreqs = new HashMap<>();
        for (String w : words)
            wordFreqs.put(w, wordFreqs.getOrDefault(w, 0) + 1);
        return wordFreqs;
    }

    static List<Map.Entry<String, Integer>> sort(Map<String, Integer> wordFreqs) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(wordFreqs.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        return sorted;
    }

    static void printAll(List<Map.Entry<String, Integer>> freqs, int index) {
        if (index >= freqs.size() || index >= 25) return;
        System.out.println(freqs.get(index).getKey() + " - " + freqs.get(index).getValue());
        printAll(freqs, index + 1);
    }

    public static void main(String[] args) throws IOException {
        printAll(
            sort(
                frequencies(
                    removeStopWords(
                        scan(
                            filterCharsAndNormalize(
                                readFile("pride-and-prejudice.txt")
                            )
                        )
                    )
                )
            ),
            0
        );
    }
}
