import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

class TheOne<T> {
    private T value;
    TheOne(T v) { value = v; }

    <R> TheOne<R> bind(Function<T, R> func) {
        return new TheOne<>(func.apply(value));
    }

    void printme() {
        System.out.println(value);
    }
}

public class Ten {
    static String read_file(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String filter_chars(String data) {
        return data.replaceAll("[\\W_]+", " ");
    }

    static String normalize(String data) {
        return data.toLowerCase();
    }

    static List<String> scan(String data) {
        return Arrays.asList(data.split("\\s+"));
    }

    static List<String> remove_stop_words(List<String> words) {
        try {
            String stops = Files.readString(Path.of("../stop_words.txt"));
            Set<String> stopWords = new HashSet<>(Arrays.asList((stops + ",abcdefghijklmnopqrstuvwxyz").split(",")));
            List<String> result = new ArrayList<>();
            for (String w : words)
                if (!stopWords.contains(w))
                    result.add(w);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Map<String, Long> frequencies(List<String> words) {
        Map<String, Long> freq = new HashMap<>();
        for (String w : words)
            freq.put(w, freq.getOrDefault(w, 0L) + 1);
        return freq;
    }

    static List<Map.Entry<String, Long>> sort(Map<String, Long> freq) {
        return freq.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .toList();
    }

    static String top25_freqs(List<Map.Entry<String, Long>> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25 && i < list.size(); i++)
            sb.append(list.get(i).getKey()).append(" - ").append(list.get(i).getValue()).append("\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        new TheOne<>("../pride-and-prejudice.txt")
            .bind(Ten::read_file)
            .bind(Ten::filter_chars)
            .bind(Ten::normalize)
            .bind(Ten::scan)
            .bind(Ten::remove_stop_words)
            .bind(Ten::frequencies)
            .bind(Ten::sort)
            .bind(Ten::top25_freqs)
            .printme();
    }
}
