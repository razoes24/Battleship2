package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe JogadasRepository.
 * A classe grava numa base de dados H2 local (data/jogadas.mv.db, já no .gitignore).
 * Como a base de dados guarda as jogadas de execuções anteriores, cada teste usa um
 * texto único (marcador) para encontrar apenas as linhas que ele próprio gravou.
 *
 * Complexidade ciclomática:
 * - novoJogo: 1
 * - guardar: 2 (sucesso / SQLException)
 * - listar: 4 (ciclo while + coordenadas null ou não + SQLException)
 *
 * @author 129881
 */
@DisplayName("Testes da classe JogadasRepository")
class JogadasRepositoryTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private String marker;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(output));
        marker = "TESTE-" + System.nanoTime();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    /** Devolve a linha de listar() que contém o texto indicado, ou null. */
    private String findLine(String text) {
        List<String> lines = JogadasRepository.listar();
        for (String line : lines)
            if (line.contains(text))
                return line;
        return null;
    }

    @Test
    @DisplayName("guardar com coordenadas: listar mostra jogo, rajada, coordenadas e resultado")
    void guardarWithCoordinates() {
        JogadasRepository.guardar(7, "A1 B2 C3", marker);
        String line = findLine(marker);
        assertNotNull(line, "Error: the saved move should appear in listar().");
        assertTrue(line.startsWith("Jogo "), "Error: the line should start with the game id.");
        assertTrue(line.contains(" | Rajada 7 | A1 B2 C3 | " + marker),
                "Error: the line should contain the move number, the coordinates and the result.");
    }

    @Test
    @DisplayName("guardar com coordenadas null: listar não mostra a parte das coordenadas")
    void guardarWithNullCoordinates() {
        JogadasRepository.guardar(8, null, marker);
        String line = findLine(marker);
        assertNotNull(line, "Error: the saved move should appear in listar().");
        assertTrue(line.contains(" | Rajada 8 | " + marker),
                "Error: without coordinates, the result should come right after the move number.");
    }

    @Test
    @DisplayName("listar junta espaços e quebras de linha do resultado num só espaço")
    void listarNormalizesWhitespace() {
        JogadasRepository.guardar(1, "J10", marker + "\n   {\n  \"validShots\" : 3\n}");
        String line = findLine(marker);
        assertNotNull(line, "Error: the saved move should appear in listar().");
        assertTrue(line.endsWith(marker + " { \"validShots\" : 3 }"),
                "Error: whitespace in the result should be collapsed to single spaces.");
        assertFalse(line.contains("\n"), "Error: the listed line should not contain line breaks.");
    }

    @Test
    @DisplayName("listar devolve várias jogadas pela ordem em que foram guardadas")
    void listarKeepsOrder() {
        JogadasRepository.guardar(1, "A1", marker + "-primeira");
        JogadasRepository.guardar(2, "A2", marker + "-segunda");
        List<String> lines = JogadasRepository.listar();
        int first = -1, second = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(marker + "-primeira")) first = i;
            if (lines.get(i).contains(marker + "-segunda")) second = i;
        }
        assertTrue(first >= 0 && second >= 0, "Error: both moves should be listed.");
        assertTrue(first < second, "Error: moves should be listed in the order they were saved.");
    }

    @Test
    @DisplayName("novoJogo gera um id de jogo com o formato data-hora (yyyyMMdd-HHmmss)")
    void novoJogoGeneratesId() {
        JogadasRepository.novoJogo();
        JogadasRepository.guardar(1, "B5", marker);
        String line = findLine(marker);
        assertNotNull(line, "Error: the saved move should appear in listar().");
        assertTrue(line.matches("Jogo \\d{8}-\\d{6} \\| .*"),
                "Error: the game id should have the format yyyyMMdd-HHmmss.");
    }

    @Test
    @DisplayName("guardar com resultado null mostra erro (SQLException) e não grava nada")
    void guardarWithNullResultShowsError() {
        int before = JogadasRepository.listar().size();
        JogadasRepository.guardar(9, marker, null); // a coluna resultado é NOT NULL
        assertTrue(output.toString().contains("Erro ao guardar a jogada"),
                "Error: an SQL error message should be printed.");
        assertEquals(before, JogadasRepository.listar().size(), "Error: nothing should be saved when the result is null.");
        assertNull(findLine(marker), "Error: the invalid move must not be listed.");
    }

    @Test
    @DisplayName("O construtor (implícito) cria uma instância")
    void constructor() {
        assertNotNull(new JogadasRepository(), "Error: JogadasRepository instance should not be null.");
    }
}