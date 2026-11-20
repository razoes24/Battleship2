package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários da classe {@link Tasks}.
 * <p>
 * Os ramos de {@code menu()} que terminam o programa com {@code System.exit(0)}
 * (fim do jogo em "rajada" e o ciclo completo de "simula") não são executados,
 * porque terminariam a própria JVM dos testes.
 *
 * @author 129854
 */
@DisplayName("Testes da classe Tasks")
class TasksTest {

    /** Frota válida de 11 navios (igual a data/exemploTabuleiro.txt). */
    private static final String VALID_FLEET = String.join("\n",
            "caravela 0 0 s",
            "galeao 0 3 o",
            "barca 0 9 n",
            "fragata 8 6 e",
            "nau 9 0 e",
            "caravela 5 2 o",
            "nau 3 7 n",
            "barca 3 1 s",
            "barca 3 5 e",
            "caravela 7 0 e",
            "barca 7 4 o") + "\n";

    private static final Path PDF = Path.of("jogadas.pdf");
    private static final Path PDF_BACKUP = Path.of("jogadas.pdf.bak-test");

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() throws IOException {
        // jogadas.pdf está no repositório: guardar e repor no fim
        if (Files.exists(PDF) && !Files.isDirectory(PDF)) {
            Files.move(PDF, PDF_BACKUP, StandardCopyOption.REPLACE_EXISTING);
        }
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setIn(originalIn);
        if (Files.isDirectory(PDF)) {
            Files.delete(PDF);
        } else {
            Files.deleteIfExists(PDF);
        }
        if (Files.exists(PDF_BACKUP)) {
            Files.move(PDF_BACKUP, PDF, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String out() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private static Scanner scanner(String text) {
        return new Scanner(text);
    }

    private static void provideInput(String text) {
        System.setIn(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Nested
    @DisplayName("menuHelp()")
    class MenuHelp {

        @Test
        @DisplayName("mostra todos os comandos disponíveis")
        void menuHelpListsCommands() {
            Tasks.menuHelp();
            String text = out();
            assertTrue(text.contains("AJUDA DO MENU"));
            for (String cmd : new String[]{"gerafrota", "lefrota", "estado", "mapa", "rajada",
                    "simula", "tiros", "scoreboard", "historicobd", "exportapdf", "desisto"}) {
                assertTrue(text.contains("- " + cmd + ":"), "Falta o comando " + cmd);
            }
        }
    }

    @Nested
    @DisplayName("readPosition()")
    class ReadPosition {

        @Test
        @DisplayName("lê linha e coluna numéricas")
        void readPositionReadsRowAndColumn() {
            Position p = Tasks.readPosition(scanner("3 7"));
            assertEquals(3, p.getRow());
            assertEquals(7, p.getColumn());
        }
    }

    @Nested
    @DisplayName("readShip()")
    class ReadShip {

        @Test
        @DisplayName("constrói o navio indicado com a posição e a orientação lidas")
        void readShipValid() {
            Ship s = Tasks.readShip(scanner("barca 1 2 n"));
            assertNotNull(s);
            assertInstanceOf(Barge.class, s);
            assertEquals(new Position(1, 2), s.getPosition());
            assertEquals(Compass.NORTH, s.getBearing());
        }

        @Test
        @DisplayName("devolve null para um tipo de navio desconhecido")
        void readShipUnknownKind() {
            assertNull(Tasks.readShip(scanner("submarino 1 2 s")));
        }
    }

    @Nested
    @DisplayName("buildFleet()")
    class BuildFleet {

        @Test
        @DisplayName("constrói uma frota completa a partir de dados válidos")
        void buildFleetValid() {
            Fleet fleet = Tasks.buildFleet(scanner(VALID_FLEET));
            assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
        }

        @Test
        @DisplayName("ignora navios desconhecidos e navios que não podem ser colocados")
        void buildFleetSkipsInvalidShips() {
            String input = "submarino 0 0 n\n"   // navio desconhecido -> null
                    + "barca 0 9 n\n"
                    + "barca 0 9 n\n"            // colisão com o anterior -> falha
                    + VALID_FLEET.replaceFirst("barca 0 9 n\n", "");
            Fleet fleet = Tasks.buildFleet(scanner(input));
            assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
        }
    }

    @Nested
    @DisplayName("readClassicPosition()")
    class ReadClassicPosition {

        @Test
        @DisplayName("lê o formato junto 'A3'")
        void readClassicJoined() {
            IPosition p = Tasks.readClassicPosition(scanner("A3"));
            assertEquals(new Position('A', 3), p);
        }

        @Test
        @DisplayName("lê o formato separado 'A 3'")
        void readClassicSeparated() {
            IPosition p = Tasks.readClassicPosition(scanner("A 3"));
            assertEquals(new Position('A', 3), p);
        }

        @Test
        @DisplayName("aceita letras minúsculas")
        void readClassicLowerCase() {
            IPosition p = Tasks.readClassicPosition(scanner("c10"));
            assertEquals(new Position('C', 10), p);
        }

        @Test
        @DisplayName("lança exceção quando não há nada para ler")
        void readClassicEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> Tasks.readClassicPosition(scanner("")));
        }

        @Test
        @DisplayName("lança exceção quando a coluna não é uma letra")
        void readClassicInvalidColumn() {
            assertThrows(IllegalArgumentException.class,
                    () -> Tasks.readClassicPosition(scanner("1 3")));
        }

        @Test
        @DisplayName("lança exceção quando a linha é negativa")
        void readClassicNegativeRow() {
            assertThrows(IllegalArgumentException.class,
                    () -> Tasks.readClassicPosition(scanner("A -3")));
        }

        @Test
        @DisplayName("lança exceção quando falta a linha")
        void readClassicMissingRow() {
            assertThrows(IllegalArgumentException.class,
                    () -> Tasks.readClassicPosition(scanner("AB")));
        }
    }

    @Nested
    @DisplayName("menu()")
    class Menu {

        @Test
        @DisplayName("termina logo com 'desisto' e despede-se")
        void menuQuitImmediately() {
            provideInput("desisto\n");
            Tasks.menu();
            assertTrue(out().contains("Bons ventos!"));
        }

        @Test
        @DisplayName("comandos sem jogo criado não fazem nada e comando inválido mostra aviso")
        void menuCommandsWithoutGame() {
            provideInput("xpto\nestado\nmapa\ntiros\nrajada\nsimula\nscoreboard\nhistoricobd\najuda\ndesisto\n");
            Tasks.menu();
            String text = out();
            assertTrue(text.contains("Que comando é esse??? Repete ..."));
            assertTrue(text.contains("Bons ventos!"));
        }

        @Test
        @DisplayName("gerafrota cria um jogo e permite usar estado, mapa, tiros e rajada")
        void menuRandomFleetGame() {
            provideInput("gerafrota\nestado\nmapa\ntiros\nrajada A1 B2 C3\ndesisto\n");
            Tasks.menu();
            String text = out();
            assertTrue(text.contains("Tempo da jogada 1"));
            assertTrue(text.contains("Tempo total do jogo"));
            assertTrue(text.contains("Bons ventos!"));
        }

        @Test
        @DisplayName("lefrota lê a frota do utilizador e permite jogar uma rajada")
        void menuUserFleetGame() {
            provideInput("lefrota\n" + VALID_FLEET + "rajada J10 J9 J8\ndesisto\n");
            Tasks.menu();
            String text = out();
            assertTrue(text.contains("Tempo da jogada 1"));
            assertTrue(text.contains("Bons ventos!"));
        }

        @Test
        @DisplayName("exportapdf gera o ficheiro jogadas.pdf")
        void menuExportPdf() {
            provideInput("exportapdf\ndesisto\n");
            Tasks.menu();
            assertTrue(out().contains("PDF gerado com sucesso: jogadas.pdf"));
            assertTrue(Files.exists(PDF));
        }

        @Test
        @DisplayName("exportapdf mostra erro quando não consegue gravar o PDF")
        void menuExportPdfError() throws IOException {
            // Um diretório com o nome do PDF impede a gravação
            Files.createDirectory(PDF);
            provideInput("exportapdf\ndesisto\n");
            Tasks.menu();
            assertTrue(out().contains("Erro ao gerar PDF"));
        }
    }
}
