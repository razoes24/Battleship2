package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários da classe {@link Scoreboard}.
 * <p>
 * O Scoreboard grava sempre em data/scoreboard.json. Para não estragar os dados
 * reais, o ficheiro é guardado antes de cada teste e reposto no fim.
 *
 * @author 129854
 */
@DisplayName("Testes da classe Scoreboard")
class ScoreboardTest {

    private static final Path FILE = Path.of("data", "scoreboard.json");
    private static final Path BACKUP = Path.of("data", "scoreboard.json.bak-test");

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(FILE.getParent());
        if (Files.exists(FILE) && !Files.isDirectory(FILE)) {
            Files.move(FILE, BACKUP, StandardCopyOption.REPLACE_EXISTING);
        }
        deleteRecursively(FILE);
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        deleteRecursively(FILE);
        if (Files.exists(BACKUP)) {
            Files.move(BACKUP, FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }

    private String out() {
        return output.toString(StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("load()")
    class Load {

        @Test
        @DisplayName("devolve lista vazia quando o ficheiro não existe")
        void loadWithoutFile() {
            List<ScoreEntry> list = Scoreboard.load();
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("devolve lista vazia quando o ficheiro contém 'null'")
        void loadNullContent() throws IOException {
            Files.writeString(FILE, "null");
            List<ScoreEntry> list = Scoreboard.load();
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("lê corretamente os resultados gravados em JSON")
        void loadValidContent() throws IOException {
            Files.writeString(FILE,
                    "[{\"date\":\"2026-01-01 10:00\",\"moves\":30},"
                            + "{\"date\":\"2026-01-02 11:00\",\"moves\":12}]");
            List<ScoreEntry> list = Scoreboard.load();
            assertEquals(2, list.size());
            assertEquals("2026-01-01 10:00", list.get(0).getDate());
            assertEquals(30, list.get(0).getMoves());
            assertEquals(12, list.get(1).getMoves());
        }

        @Test
        @DisplayName("devolve lista vazia e mostra erro quando não consegue ler o ficheiro")
        void loadIOException() throws IOException {
            // Um diretório com o nome do ficheiro faz Files.readString lançar IOException
            Files.createDirectories(FILE);
            List<ScoreEntry> list = Scoreboard.load();
            assertTrue(list.isEmpty());
            assertTrue(out().contains("Erro a ler o scoreboard"));
        }
    }

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("cria o ficheiro e grava o primeiro resultado")
        void addFirstEntry() {
            Scoreboard.add(25);
            assertTrue(Files.exists(FILE));
            List<ScoreEntry> list = Scoreboard.load();
            assertEquals(1, list.size());
            assertEquals(25, list.get(0).getMoves());
            assertTrue(list.get(0).getDate().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
        }

        @Test
        @DisplayName("acrescenta resultados sem apagar os anteriores")
        void addKeepsPreviousEntries() {
            Scoreboard.add(25);
            Scoreboard.add(10);
            List<ScoreEntry> list = Scoreboard.load();
            assertEquals(2, list.size());
            assertEquals(25, list.get(0).getMoves());
            assertEquals(10, list.get(1).getMoves());
        }

        @Test
        @DisplayName("mostra erro quando não consegue gravar o ficheiro")
        void addIOException() throws IOException {
            // Com um diretório no lugar do ficheiro, a escrita falha
            Files.createDirectories(FILE);
            Scoreboard.add(5);
            assertTrue(out().contains("Erro a gravar o scoreboard"));
            assertTrue(Files.isDirectory(FILE));
        }
    }

    @Nested
    @DisplayName("show()")
    class Show {

        @Test
        @DisplayName("avisa quando ainda não há jogos")
        void showEmpty() {
            Scoreboard.show();
            assertTrue(out().contains("Ainda não há jogos no scoreboard."));
            assertFalse(out().contains("SCOREBOARD ====="));
        }

        @Test
        @DisplayName("lista os jogos ordenados do menor para o maior nº de jogadas")
        void showSorted() throws IOException {
            Files.writeString(FILE,
                    "[{\"date\":\"d1\",\"moves\":30},"
                            + "{\"date\":\"d2\",\"moves\":12},"
                            + "{\"date\":\"d3\",\"moves\":20}]");
            Scoreboard.show();
            String text = out();
            assertTrue(text.contains("===== SCOREBOARD ====="));
            assertTrue(text.contains("1. d2 - 12 jogadas"));
            assertTrue(text.contains("2. d3 - 20 jogadas"));
            assertTrue(text.contains("3. d1 - 30 jogadas"));
        }
    }
}
