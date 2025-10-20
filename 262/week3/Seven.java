package week3;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Seven {
    public static void main(String[] a) throws Exception {
        var s = new HashSet<>(Arrays.asList(Files.readString(Path.of("../262/stop_words.txt")).split(",")));
        Arrays.stream(Files.readString(Path.of("../262/pride-and-prejudice.txt")).toLowerCase().split("[^a-z]+"))
              .filter(w -> w.length() > 1 && !s.contains(w))
              .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
              .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
              .limit(25).forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
    }
}
