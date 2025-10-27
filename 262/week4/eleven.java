import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

class WordFrequencyFramework {
    private List<Consumer<String>> loadEventHandlers = new ArrayList<>();
    private List<Runnable> doWorkEventHandlers = new ArrayList<>();
    private List<Runnable> endEventHandlers = new ArrayList<>();

    void registerForLoadEvent(Consumer<String> handler) { loadEventHandlers.add(handler); }
    void registerForDoWorkEvent(Runnable handler) { doWorkEventHandlers.add(handler); }
    void registerForEndEvent(Runnable handler) { endEventHandlers.add(handler); }

    void run(String pathToFile) {
        loadEventHandlers.forEach(h -> h.accept(pathToFile));
        doWorkEventHandlers.forEach(Runnable::run);
        endEventHandlers.forEach(Runnable::run);
    }
}

class StopWordFilter {
    private Set<String> stopWords = new HashSet<>();

    StopWordFilter(WordFrequencyFramework wfApp) {
        wfApp.registerForLoadEvent(this::load);
    }

    private void load(String ignore) {
        try {
            String stops = Files.readString(Path.of("../stop_words.txt"));
            stopWords.addAll(Arrays.asList((stops + ",abcdefghijklmnopqrstuvwxyz").split(",")));
        } catch (IOException e) { e.printStackTrace(); }
    }

    boolean isStopWord(String word) { return stopWords.contains(word); }
}

class DataStorage {
    private String data = "";
    private StopWordFilter stopWordFilter;
    private List<Consumer<String>> wordEventHandlers = new ArrayList<>();

    DataStorage(WordFrequencyFramework wfApp, StopWordFilter filter) {
        this.stopWordFilter = filter;
        wfApp.registerForLoadEvent(this::load);
        wfApp.registerForDoWorkEvent(this::produceWords);
    }

    private void load(String path) {
        try {
            data = Files.readString(Path.of(path)).replaceAll("[\\W_]+", " ").toLowerCase();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void produceWords() {
        for (String w : data.split("\\s+")) {
            if (!stopWordFilter.isStopWord(w)) {
                wordEventHandlers.forEach(h -> h.accept(w));
            }
        }
    }

    void registerForWordEvent(Consumer<String> handler) { wordEventHandlers.add(handler); }
}

class WordFrequencyCounter {
    private Map<String, Long> wordFreqs = new HashMap<>();

    WordFrequencyCounter(WordFrequencyFramework wfApp, DataStorage dataStorage) {
        dataStorage.registerForWordEvent(this::incrementCount);
        wfApp.registerForEndEvent(this::printFreqs);
    }

    private void incrementCount(String word) {
        wordFreqs.put(word, wordFreqs.getOrDefault(word, 0L) + 1);
    }

    private void printFreqs() {
        wordFreqs.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(25)
            .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
    }
}

public class eleven {
    public static void main(String[] args) {
        String textPath = "../pride-and-prejudice.txt";

        WordFrequencyFramework wfApp = new WordFrequencyFramework();
        StopWordFilter stopWordFilter = new StopWordFilter(wfApp);
        DataStorage dataStorage = new DataStorage(wfApp, stopWordFilter);
        WordFrequencyCounter counter = new WordFrequencyCounter(wfApp, dataStorage);

        wfApp.run(textPath);
    }
}
