// week6/Twelve.java
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

class ActiveWFObject extends Thread {
    protected BlockingQueue<List<Object>> queue = new LinkedBlockingQueue<>();
    protected boolean stopMe = false;

    public void send(List<Object> message) {
        queue.add(message);
    }

    @Override
    public void run() {
        try {
            while (!stopMe) {
                List<Object> message = queue.take();
                dispatch(message);
                if ("die".equals(message.get(0))) {
                    stopMe = true;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void dispatch(List<Object> message) {}
}

class DataStorageManager extends ActiveWFObject {
    private String data = "";
    private StopWordManager stopWordManager;

    @Override
    protected void dispatch(List<Object> message) {
        String command = (String) message.get(0);
        switch (command) {
            case "init" -> init((String) message.get(1), (StopWordManager) message.get(2));
            case "send_word_freqs" -> processWords((WordFrequencyController) message.get(1));
            default -> stopWordManager.send(message);
        }
    }

    private void init(String filePath, StopWordManager stopWordManager) {
        this.stopWordManager = stopWordManager;
        try {
            data = Files.readString(Paths.get(filePath));
            data = data.replaceAll("[\\W_]+", " ").toLowerCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processWords(WordFrequencyController recipient) {
        for (String word : data.split("\\s+")) {
            stopWordManager.send(List.of("filter", word));
        }
        stopWordManager.send(List.of("top25", recipient));
    }
}


class StopWordManager extends ActiveWFObject {
    private Set<String> stopWords = new HashSet<>();
    private WordFrequencyManager wordFreqsManager;

    @Override
    protected void dispatch(List<Object> message) {
        String command = (String) message.get(0);
        switch (command) {
            case "init" -> init((WordFrequencyManager) message.get(1));
            case "filter" -> filter((String) message.get(1));
            default -> wordFreqsManager.send(message);
        }
    }

    private void init(WordFrequencyManager wordFreqsManager) {
        this.wordFreqsManager = wordFreqsManager;
        try {
            String content = Files.readString(Paths.get("../stop_words.txt"));
            stopWords.addAll(Arrays.asList(content.split(",")));
            for (char c = 'a'; c <= 'z'; c++) stopWords.add(String.valueOf(c));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filter(String word) {
        if (!stopWords.contains(word)) {
            wordFreqsManager.send(List.of("word", word));
        }
    }
}


class WordFrequencyManager extends ActiveWFObject {
    private Map<String, Integer> wordFreqs = new HashMap<>();

    @Override
    protected void dispatch(List<Object> message) {
        String command = (String) message.get(0);
        switch (command) {
            case "word" -> incrementCount((String) message.get(1));
            case "top25" -> top25((WordFrequencyController) message.get(1));
        }
    }

    private void incrementCount(String word) {
        wordFreqs.merge(word, 1, Integer::sum);
    }

    private void top25(WordFrequencyController recipient) {
        List<Map.Entry<String, Integer>> sorted = wordFreqs.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList();
        recipient.send(List.of("top25", sorted));
    }
}


class WordFrequencyController extends ActiveWFObject {
    @Override
    protected void dispatch(List<Object> message) {
        String command = (String) message.get(0);
        switch (command) {
            case "run" -> runController((DataStorageManager) message.get(1));
            case "top25" -> display((List<Map.Entry<String, Integer>>) message.get(1));
            default -> System.err.println("Unknown message: " + command);
        }
    }

    private void runController(DataStorageManager storageManager) {
        storageManager.send(List.of("send_word_freqs", this));
    }

    private void display(List<Map.Entry<String, Integer>> freqs) {
        freqs.stream().limit(25)
                .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
        stopMe = true;
    }
}


public class Twelve {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.out.println("Usage: java Twelve ../pride-and-prejudice.txt");
            return;
        }

        WordFrequencyManager wordFreqManager = new WordFrequencyManager();
        StopWordManager stopWordManager = new StopWordManager();
        DataStorageManager storageManager = new DataStorageManager();
        WordFrequencyController controller = new WordFrequencyController();

        stopWordManager.start();
        wordFreqManager.start();
        storageManager.start();
        controller.start();

        stopWordManager.send(List.of("init", wordFreqManager));
        storageManager.send(List.of("init", args[0], stopWordManager));
        controller.send(List.of("run", storageManager));

        controller.join();
        storageManager.send(List.of("die"));
        stopWordManager.send(List.of("die"));
        wordFreqManager.send(List.of("die"));
    }
}
