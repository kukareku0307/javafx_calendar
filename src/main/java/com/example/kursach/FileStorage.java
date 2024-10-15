package com.example.kursach;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;


public class FileStorage {

    private static final String NOTES_DIR = "Notes";
    private static final String NOTE_ID_FILE = "Notes/NoteId.txt";
    private static int noteId;

    static {
        try {
            File dir = new File(NOTES_DIR);
            if (!dir.exists()) {
                System.out.println("Создаем директорию");
                var res = dir.mkdirs();
            }

            File noteIdFile = new File(NOTE_ID_FILE);
            if (!noteIdFile.exists()) {
                var res = noteIdFile.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(noteIdFile))) {
                    writer.write("1");
                }
                noteId = 1;
            } else {
                try (BufferedReader reader = new BufferedReader(new FileReader(noteIdFile))) {
                    noteId = Integer.parseInt(reader.readLine());
                }
            }
        } catch (IOException e) {
            CalendarController.logger.error(e.getMessage());
//            e.printStackTrace();
        }
    }

    public static void saveNote(String date, String note) {
        try {
            File dateDir = new File(NOTES_DIR + File.separator + date);
            if (!dateDir.exists()) {
                var res = dateDir.mkdirs();
            }

            File noteFile = new File(dateDir, noteId + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(noteFile))) {
                writer.write(note);
            }

            noteId++;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOTE_ID_FILE))) {
                writer.write(Integer.toString(noteId));
            }
        } catch (IOException e) {
//            e.printStackTrace();
            CalendarController.logger.error(e.getMessage());
        }
    }

    public static List<SimpleEntry<Integer, String>> findDate(String date) {
        List<SimpleEntry<Integer, String>> notes = new ArrayList<>();
        File dateDir = new File(NOTES_DIR + File.separator + date);
        if (dateDir.exists() && dateDir.isDirectory()) {
            File[] files = dateDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                notes = Arrays.stream(files)
                        .map(file -> {
                            try {
                                int fileId = Integer.parseInt(file.getName().replace(".txt", ""));
                                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                    String note = reader.lines().collect(Collectors.joining());
                                    return new SimpleEntry<>(fileId, note);
                                }
                            } catch (IOException e) {
                                CalendarController.logger.error(e.getMessage());
//                                e.printStackTrace();
                                return null;
                            }
                        })
                        .collect(Collectors.toList());
            }
        }
        return notes;
    }

    public static void deleteNote(String date, int noteId) {
        File noteFile = new File(NOTES_DIR + File.separator + date + File.separator + noteId + ".txt");
        if (noteFile.exists()) {
            var res = noteFile.delete();
        }
    }

    public static void deleteDate(String date) {
        File directory = new File(NOTES_DIR + File.separator + date);
        if (directory.exists() && directory.isDirectory()) {
            // Получаем список всех файлов и папок внутри директории
            File[] files = directory.listFiles();

            // Удаляем все содержимое в директории
            if (files != null) {
                for (File file : files) {

                    file.delete(); // Удаляем файл
                }
            }


            // Удаляем саму директорию
            directory.delete();
        }

    }
}