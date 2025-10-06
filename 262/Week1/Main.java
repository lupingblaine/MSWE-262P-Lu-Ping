import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {System.out.println("Empty Error"); return;} // test the command line empty or not

        String inputPath = args[0];

        Set<String> stopWords = new HashSet<String>();
        try (BufferedReader brStop = new BufferedReader(new FileReader("../stop_words.txt"))) {
            String line;
            while ((line = brStop.readLine()) != null) {
                String[] words = line.split("[,\\s]+");
                for (int i = 0; i < words.length; i++) {
                    String w = words[i].trim().toLowerCase();
                    if (w.length() > 0) {
                        stopWords.add(w);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error with stop words file, go on");
        } // read the stop words file and store in set

        Map<String, Integer> freqs = new HashMap<String, Integer>();
        try (BufferedReader brBook = new BufferedReader(new FileReader(inputPath))){
            String line;
            while ((line = brBook.readLine()) != null) {
                line = line.toLowerCase();
                line = line.replaceAll("[^a-z0-9]+", " ");
                String[] words = line.split("\\s+"); 
                for (int i=0; i < words.length; i++) {
                    String w = words[i];
                    if (w.length() > 1 && !stopWords.contains(w)) {
                        if (freqs.containsKey(w)) {
                            freqs.put(w, freqs.get(w) + 1);
                        } else {freqs.put(w, 1);}
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading input file.");
            return;
        }


        List<String> keys = new ArrayList<String>();
        for (String key : freqs.keySet()) {
            keys.add(key);
        }

        for (int i = 0; i < keys.size() - 1; i++) {
            for (int j = 0; j < keys.size() - 1 - i; j++) {
                String word1 = keys.get(j);
                String word2 = keys.get(j + 1);
                int count1 = freqs.get(word1);
                int count2 = freqs.get(word2);
                if (count1 < count2){
                    keys.set(j, word2);
                    keys.set(j + 1, word1);
                } else if (count1 == count2) {
                    if (word1.compareTo(word2) > 0) {
                        keys.set(j, word2);
                        keys.set(j + 1, word1);
                    }
                }
            }
        }

        int limit = Math.min(25, keys.size());
        for (int i = 0; i < limit; i++) {
            String word = keys.get(i);
            int count = freqs.get(word);
            System.out.println(word + " - " + count);
        }

    }
    
}
