package week2;

import java.io.*;
import java.util.*;

public class Five {
    static List<Character> data = new ArrayList<>();
    static List<String> words = new ArrayList<>();
    static List<String[]> wordFreqs = new ArrayList<>();

    static void readFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        int c;
        while ((c = br.read()) != -1) {
            data.add((char) c);
        }
        br.close();
    }

    static void filterCharsAndNormalize() {
        for (int i = 0; i < data.size(); i++) {
            char c = data.get(i);
            if (!Character.isLetterOrDigit(c))
                data.set(i, ' ');
            else
                data.set(i, Character.toLowerCase(c));
        }
    }

    static void scan() {
        StringBuilder sb = new StringBuilder();
        for (char c : data) sb.append(c);
        words.addAll(Arrays.asList(sb.toString().split("\\s+")));
    }

    static void removeStopWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("stop_words.txt"));
        String[] stopWords = br.readLine().split(",");
        br.close();
        Set<String> stopSet = new HashSet<>(Arrays.asList(stopWords));
        for (char c = 'a'; c <= 'z'; c++) stopSet.add(String.valueOf(c));

        List<String> filtered = new ArrayList<>();
        for (String w : words) {
            if (!stopSet.contains(w))
                filtered.add(w);
        }
        words = filtered;
    }

    static void frequencies() {
        for (String w : words) {
            boolean found = false;
            for (String[] pair : wordFreqs) {
                if (pair[0].equals(w)) {
                    pair[1] = String.valueOf(Integer.parseInt(pair[1]) + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                wordFreqs.add(new String[]{w, "1"});
            }
        }
    }

    static void sort() {
        Collections.sort(wordFreqs, (a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));
    }

    public static void main(String[] args) throws IOException {
        readFile("pride-and-prejudice.txt");
        filterCharsAndNormalize();
        scan();
        removeStopWords();
        frequencies();
        sort();
        for (int i = 0; i < 25 && i < wordFreqs.size(); i++)
            System.out.println(wordFreqs.get(i)[0] + " - " + wordFreqs.get(i)[1]);
    }
}
