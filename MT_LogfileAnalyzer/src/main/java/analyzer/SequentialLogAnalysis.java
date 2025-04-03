package analyzer;

import tools.LogGenerator;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SequentialLogAnalysis {

    public static void main(String[] args) {
        System.out.println("Starte Log-Generierung...");
        LogGenerator generator = new LogGenerator(5, "app", LocalDate.now().minusDays(4), 10, 100);
        generator.generateLogs();


        List<Path> logFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get("."))) {
            logFiles = paths.filter(path -> path.toString().endsWith(".log"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Gefundene Logdateien: " + logFiles);


        long startTime = System.nanoTime();


        Map<String, Integer> globalCount = new HashMap<>();
        for (String level : Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR")) {
            globalCount.put(level, 0);
        }


        for (Path file : logFiles) {
            Map<String, Integer> fileCount = analyzeLogFile(file);
            System.out.println("Ergebnis für " + file.getFileName() + ": " + fileCount);

            for (Map.Entry<String, Integer> entry : fileCount.entrySet()) {
                globalCount.put(entry.getKey(), globalCount.get(entry.getKey()) + entry.getValue());
            }
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("\nGesamtübersicht: " + globalCount);
        System.out.printf("Ausführungszeit: %.2f ms%n", duration / 1_000_000.0);
    }

    private static Map<String, Integer> analyzeLogFile(Path file) {
        Map<String, Integer> countMap = new HashMap<>();
        // Initialisiere alle LogLevel mit 0
        for (String level : Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR")) {
            countMap.put(level, 0);
        }
        try {
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {

                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String level = parts[1];
                    countMap.put(level, countMap.getOrDefault(level, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei " + file.getFileName() + ": " + e.getMessage());
        }
        return countMap;
    }
}
