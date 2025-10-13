package week2;

import java.io.*;
import java.util.*;

public class Four {
    public static void main(String[] args) throws IOException {
        List<String[]> wordFreqs = new ArrayList<>();

        BufferedReader brStop = new BufferedReader(new FileReader("stop_words.txt"));
        String[] stopWords = brStop.readLine().split(",");
        brStop.close();
        Set<String> stopSet = new HashSet<>(Arrays.asList(stopWords));
        for (char c = 'a'; c <= 'z'; c++) stopSet.add(String.valueOf(c));

        BufferedReader br = new BufferedReader(new FileReader("pride-and-prejudice.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            int startChar = -1;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (startChar == -1) {
                    if (Character.isLetterOrDigit(c)) {
                        startChar = i;
                    }
                } else {
                    if (!Character.isLetterOrDigit(c)) {
                        String word = line.substring(startChar, i).toLowerCase();
                        startChar = -1;
                        if (!stopSet.contains(word)) {
                            boolean found = false;
                            int pairIndex = 0;
                            for (int j = 0; j < wordFreqs.size(); j++) {
                                if (wordFreqs.get(j)[0].equals(word)) {
                                    int count = Integer.parseInt(wordFreqs.get(j)[1]) + 1;
                                    wordFreqs.set(j, new String[]{word, String.valueOf(count)});
                                    found = true;
                                    pairIndex = j;
                                    break;
                                }
                            }
                            if (!found) {
                                wordFreqs.add(new String[]{word, "1"});
                                pairIndex = wordFreqs.size() - 1;
                            }

                            for (int k = pairIndex - 1; k >= 0; k--) {
                                int freq1 = Integer.parseInt(wordFreqs.get(k + 1)[1]);
                                int freq2 = Integer.parseInt(wordFreqs.get(k)[1]);
                                if (freq1 > freq2) {
                                    String[] tmp = wordFreqs.get(k);
                                    wordFreqs.set(k, wordFreqs.get(k + 1));
                                    wordFreqs.set(k + 1, tmp);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        br.close();

        for (int i = 0; i < 25 && i < wordFreqs.size(); i++) {
            System.out.println(wordFreqs.get(i)[0] + " - " + wordFreqs.get(i)[1]);
        }
    }
}

