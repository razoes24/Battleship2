package battleship;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scoreboard {

    private static final Path FILE = Path.of("data", "scoreboard.json");
    private static final Gson GSON = new Gson();

    // Lê os resultados guardados (lista vazia se o ficheiro não existir)
    public static List<ScoreEntry> load() {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }
            Type type = new TypeToken<List<ScoreEntry>>() {}.getType();
            List<ScoreEntry> list = GSON.fromJson(Files.readString(FILE), type);
            return list == null ? new ArrayList<>() : list;
        } catch (IOException e) {
            System.out.println("Erro a ler o scoreboard: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Acrescenta o resultado de um jogo e grava o ficheiro JSON
    public static void add(int moves) {
        List<ScoreEntry> list = load();
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        list.add(new ScoreEntry(date, moves));
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(list));
        } catch (IOException e) {
            System.out.println("Erro a gravar o scoreboard: " + e.getMessage());
        }
    }

    // Mostra os jogos ordenados por menor nº de jogadas
    public static void show() {
        List<ScoreEntry> list = load();
        if (list.isEmpty()) {
            System.out.println("Ainda não há jogos no scoreboard.");
            return;
        }
        list.sort(Comparator.comparingInt(ScoreEntry::getMoves));
        System.out.println("===== SCOREBOARD =====");
        int pos = 1;
        for (ScoreEntry e : list) {
            System.out.println(pos++ + ". " + e);
        }
    }
}