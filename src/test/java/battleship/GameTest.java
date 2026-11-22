package battleship;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 *
 * Casos de teste adicionais para aumentar a cobertura: 131834
 */
public class GameTest {

	private Game game;

	// guardo o System.out original para poder ler o que o Game imprime
	private PrintStream outOriginal;
	private ByteArrayOutputStream saida;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet

		outOriginal = System.out;
		saida = new ByteArrayOutputStream();
		System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void tearDown() {
		System.setOut(outOriginal);
		game = null;
	}

	/** Texto que foi escrito no System.out desde o início do teste. */
	private String consola() {
		return saida.toString(StandardCharsets.UTF_8);
	}

	/** Cria um jogo cuja frota tem os navios dados. */
	private Game jogoComNavios(IShip... navios) {
		Fleet frota = new Fleet();
		for (IShip navio : navios)
			frota.addShip(navio);
		return new Game(frota);
	}

	@Test
	@DisplayName("construtor inicializa o jogo com tudo a zero")
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	@DisplayName("getters devolvem a frota recebida e listas de jogadas vazias")
	void getters() {
		Fleet frota = new Fleet();
		Game g = new Game(frota);

		assertSame(frota, g.getMyFleet());
		assertNotNull(g.getAlienFleet());
		assertTrue(g.getAlienFleet().getShips().isEmpty());
		assertTrue(g.getMyMoves().isEmpty());
		assertTrue(g.getAlienMoves().isEmpty());
	}

	@Test
	@DisplayName("tiro fora do tabuleiro conta como inválido")
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	@DisplayName("tiro marcado como repetido conta como repetido")
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	@DisplayName("fireSingleShot fora do tabuleiro devolve resultado inválido")
	void fireSingleShotInvalido() {
		IGame.ShotResult r = game.fireSingleShot(new Position(10, 0), false);

		assertFalse(r.valid());
		assertFalse(r.repeated());
		assertNull(r.ship());
		assertFalse(r.sunk());
	}

	@Test
	@DisplayName("fireSingleShot na água não conta acertos")
	void fireSingleShotAgua() {
		IGame.ShotResult r = game.fireSingleShot(new Position(0, 0), false);

		assertTrue(r.valid());
		assertFalse(r.repeated());
		assertNull(r.ship());
		assertFalse(r.sunk());
		assertEquals(0, game.getHits());
	}

	@Test
	@DisplayName("fireSingleShot num navio com mais de uma posição acerta mas não afunda")
	void fireSingleShotAcerto() {
		Ship fragata = new Frigate(Compass.EAST, new Position(5, 5));
		Game g = jogoComNavios(fragata);

		IGame.ShotResult r = g.fireSingleShot(new Position(5, 6), false);

		assertTrue(r.valid());
		assertFalse(r.repeated());
		assertSame(fragata, r.ship());
		assertFalse(r.sunk());
		assertEquals(1, g.getHits());
		assertEquals(0, g.getSunkShips());
		assertEquals(1, g.getRemainingShips());
	}

	@Test
	@DisplayName("fireSingleShot no último pedaço de um navio afunda-o")
	void fireSingleShotAfunda() {
		Ship barca = new Barge(Compass.NORTH, new Position(5, 5));
		Game g = jogoComNavios(barca);

		IGame.ShotResult r = g.fireSingleShot(new Position(5, 5), false);

		assertTrue(r.sunk());
		assertSame(barca, r.ship());
		assertEquals(1, g.getHits());
		assertEquals(1, g.getSunkShips());
		assertEquals(0, g.getRemainingShips());
	}

	@Test
	@DisplayName("fireSingleShot numa posição já disparada numa rajada anterior é repetido")
	void fireSingleShotRepetidoDoHistorico() {
		game.fireShots(List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5)));

		IGame.ShotResult r = game.fireSingleShot(new Position(2, 3), false);

		assertTrue(r.valid());
		assertTrue(r.repeated());
		assertNull(r.ship());
		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	@DisplayName("fireShots com menos de 3 tiros lança exceção")
	void fireShotsPoucosTiros() {
		List<IPosition> tiros = List.of(new Position(1, 1), new Position(2, 2));

		assertThrows(IllegalArgumentException.class, () -> game.fireShots(tiros));
		assertTrue(game.getAlienMoves().isEmpty());
	}

	@Test
	@DisplayName("fireShots com mais de 3 tiros lança exceção")
	void fireShotsDemasiadosTiros() {
		List<IPosition> tiros = List.of(new Position(1, 1), new Position(2, 2),
				new Position(3, 3), new Position(4, 4));

		assertThrows(IllegalArgumentException.class, () -> game.fireShots(tiros));
	}

	@Test
	@DisplayName("fireShots regista a rajada e vai incrementando o número da jogada")
	void fireShotsNumeroDaJogada() {
		game.fireShots(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)));
		game.fireShots(List.of(new Position(1, 0), new Position(1, 1), new Position(1, 2)));

		assertEquals(2, game.getAlienMoves().size());
		assertEquals(1, game.getAlienMoves().get(0).getNumber());
		assertEquals(2, game.getAlienMoves().get(1).getNumber());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("fireShots com a mesma posição duas vezes conta um tiro repetido")
	void fireShotsRepetidoNaMesmaRajada() {
		game.fireShots(List.of(new Position(1, 1), new Position(1, 1), new Position(2, 2)));

		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	@DisplayName("fireShots com uma posição fora do tabuleiro conta um tiro inválido")
	void fireShotsComTiroInvalido() {
		game.fireShots(List.of(new Position(-1, 0), new Position(0, 0), new Position(0, 1)));

		assertEquals(1, game.getInvalidShots());
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	@DisplayName("repeatedShot é verdadeiro depois de a posição ser disparada")
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	@DisplayName("repeatedShot é falso antes de qualquer tiro")
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	@DisplayName("repeatedShot é falso para uma posição que ainda não foi disparada")
	void repeatedShotOutraPosicao() {
		game.fireShots(List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5)));

		assertFalse(game.repeatedShot(new Position(9, 9)));
	}

	@Test
	@DisplayName("getAlienMoves tem uma jogada depois de uma rajada")
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	@DisplayName("getRemainingShips diminui quando um navio é afundado")
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	// ------------------------------------------------------------------
	// randomEnemyFire

	@Test
	@DisplayName("randomEnemyFire dispara uma rajada de 3 tiros e devolve o JSON")
	void randomEnemyFire() {
		String json = game.randomEnemyFire();

		assertNotNull(json);
		assertTrue(json.contains("\"row\""));
		assertTrue(json.contains("\"column\""));
		assertEquals(1, game.getAlienMoves().size());
		assertEquals(Game.NUMBER_SHOTS, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("randomEnemyFire não dispara para posições já disparadas")
	void randomEnemyFireSemRepetir() {
		game.randomEnemyFire();
		game.randomEnemyFire();

		assertEquals(0, game.getRepeatedShots());
		assertEquals(2, game.getAlienMoves().size());
	}

	@Test
	@DisplayName("randomEnemyFire não dispara para as posições à volta de um navio afundado")
	void randomEnemyFireComNavioAfundado() {
		Ship barca = new Barge(Compass.NORTH, new Position(5, 5));
		Game g = jogoComNavios(barca);
		g.fireSingleShot(new Position(5, 5), false); // afunda a barca

		g.randomEnemyFire();

		List<IPosition> adjacentes = barca.getAdjacentPositions();
		for (IPosition tiro : g.getAlienMoves().get(0).getShots())
			assertFalse(adjacentes.contains(tiro), "Disparou para uma posição adjacente: " + tiro);
	}

	@Test
	@DisplayName("randomEnemyFire quando restam menos de 3 posições repete tiros para completar a rajada")
	void randomEnemyFireComPoucasPosicoes() {
		// disparo em 98 das 100 posições do tabuleiro, ficam só 2 por usar
		for (int i = 0; i < 96; i += 3)
			game.fireShots(List.of(posicao(i), posicao(i + 1), posicao(i + 2)));
		game.fireShots(List.of(posicao(96), posicao(97), posicao(97)));

		int rajadasAntes = game.getAlienMoves().size();
		String json = game.randomEnemyFire();

		assertNotNull(json);
		assertEquals(rajadasAntes + 1, game.getAlienMoves().size());

		List<IPosition> ultimos = game.getAlienMoves().get(rajadasAntes).getShots();
		assertEquals(Game.NUMBER_SHOTS, ultimos.size());
		// só podem ser as posições 98 e 99 (J9 e J10)
		for (IPosition tiro : ultimos)
			assertTrue(tiro.equals(posicao(98)) || tiro.equals(posicao(99)));
	}

	/** Posição número i do tabuleiro, a contar linha a linha (0 a 99). */
	private Position posicao(int i) {
		return new Position(i / Game.BOARD_SIZE, i % Game.BOARD_SIZE);
	}

	// ------------------------------------------------------------------
	// readEnemyFire

	@Test
	@DisplayName("readEnemyFire aceita posições escritas como A1 B2 C3")
	void readEnemyFireJuntas() {
		String json = game.readEnemyFire(new Scanner("A1 B2 C3\n"));

		assertNotNull(json);
		assertEquals(1, game.getAlienMoves().size());
		List<IPosition> tiros = game.getAlienMoves().get(0).getShots();
		assertEquals(new Position('A', 1), tiros.get(0));
		assertEquals(new Position('B', 2), tiros.get(1));
		assertEquals(new Position('C', 3), tiros.get(2));
	}

	@Test
	@DisplayName("readEnemyFire aceita posições escritas como A 1 B 2 C 3")
	void readEnemyFireSeparadas() {
		game.readEnemyFire(new Scanner("A 1 b 2 C 10\n"));

		List<IPosition> tiros = game.getAlienMoves().get(0).getShots();
		assertEquals(new Position('A', 1), tiros.get(0));
		assertEquals(new Position('B', 2), tiros.get(1));
		assertEquals(new Position('C', 10), tiros.get(2));
	}

	@Test
	@DisplayName("readEnemyFire ignora as posições a mais para lá das 3 primeiras")
	void readEnemyFireTokensAMais() {
		game.readEnemyFire(new Scanner("A1 B2 C3 D4\n"));

		assertEquals(Game.NUMBER_SHOTS, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("readEnemyFire com menos de 3 posições lança exceção")
	void readEnemyFirePoucasPosicoes() {
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(new Scanner("A1 B2\n")));
		assertTrue(game.getAlienMoves().isEmpty());
	}

	@Test
	@DisplayName("readEnemyFire com uma letra sem número a seguir lança exceção")
	void readEnemyFireLetraSemNumero() {
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(new Scanner("A B2 C3\n")));
	}

	@Test
	@DisplayName("readEnemyFire com uma posição mal escrita lança exceção")
	void readEnemyFireFormatoInvalido() {
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(new Scanner("A1 B2 33\n")));
	}

	// ------------------------------------------------------------------
	// jsonShots

	@Test
	@DisplayName("jsonShots devolve o JSON com a linha e a coluna de cada tiro")
	void jsonShots() {
		List<IPosition> tiros = new ArrayList<>();
		tiros.add(new Position('A', 1));
		tiros.add(new Position('B', 2));

		String json = Game.jsonShots(tiros);

		assertTrue(json.contains("\"row\""));
		assertTrue(json.contains("\"column\""));
		assertTrue(json.contains("\"A\""));
		assertTrue(json.contains("\"B\""));
		// também é impresso na consola
		assertTrue(consola().contains(json));
	}

	@Test
	@DisplayName("jsonShots com lista vazia devolve um array sem tiros")
	void jsonShotsVazio() {
		String json = Game.jsonShots(new ArrayList<>());

		assertTrue(json.startsWith("["));
		assertFalse(json.contains("row"));
	}

	// ------------------------------------------------------------------
	// impressão dos tabuleiros

	@Test
	@DisplayName("printMyBoard sem tiros mostra os navios e não mostra a legenda")
	void printMyBoardSoNavios() {
		Game g = jogoComNavios(new Barge(Compass.NORTH, new Position(5, 5)));

		g.printMyBoard(false, false);

		assertTrue(consola().contains(" F | . . . . . # . . . . |"));
		assertFalse(consola().contains("LEGENDA"));
	}

	@Test
	@DisplayName("printMyBoard com legenda escreve a legenda")
	void printMyBoardComLegenda() {
		game.printMyBoard(false, true);

		assertTrue(consola().contains("LEGENDA"));
	}

	@Test
	@DisplayName("printMyBoard com tiros marca acertos, água e adjacentes do navio afundado")
	void printMyBoardComTiros() {
		Game g = jogoComNavios(new Barge(Compass.NORTH, new Position(5, 5)));
		// um acerto que afunda a barca, um tiro na água e um fora do tabuleiro
		g.fireShots(List.of(new Position(5, 5), new Position(0, 0), new Position(-1, 0)));
		saida.reset();

		g.printMyBoard(true, false);

		String out = consola();
		assertTrue(out.contains(" A | o . . . . . . . . . |"), "tiro na água em A1");
		assertTrue(out.contains(" E | . . . . - - - . . . |"), "adjacentes da barca afundada");
		assertTrue(out.contains(" F | . . . . - * - . . . |"), "acerto na barca");
	}

	@Test
	@DisplayName("printMyBoard marca como água um tiro numa posição adjacente a um navio afundado")
	void printMyBoardTiroAdjacente() {
		Game g = jogoComNavios(new Barge(Compass.NORTH, new Position(5, 5)));
		g.fireShots(List.of(new Position(5, 5), new Position(5, 4), new Position(0, 0)));
		saida.reset();

		g.printMyBoard(true, false);

		assertTrue(consola().contains(" F | . . . . o * - . . . |"));
	}

	// Os asserts só disparam com as assertions ativas (-ea), que é o que o IntelliJ e o Maven fazem nos testes.

	@Test
	@DisplayName("printBoard com frota nula falha o assert")
	void printBoardFrotaNula() {
		assertThrows(AssertionError.class, () -> Game.printBoard(null, new ArrayList<>(), false, false));
	}

	@Test
	@DisplayName("printBoard com lista de jogadas nula falha o assert")
	void printBoardJogadasNulas() {
		assertThrows(AssertionError.class, () -> Game.printBoard(new Fleet(), null, false, false));
	}

	@Test
	@DisplayName("jsonShots com lista nula falha o assert")
	void jsonShotsNulo() {
		assertThrows(AssertionError.class, () -> Game.jsonShots(null));
	}

	@Test
	@DisplayName("readEnemyFire com scanner nulo falha o assert")
	void readEnemyFireScannerNulo() {
		assertThrows(AssertionError.class, () -> game.readEnemyFire(null));
	}

	@Test
	@DisplayName("fireShots com lista nula falha o assert")
	void fireShotsNulo() {
		assertThrows(AssertionError.class, () -> game.fireShots(null));
	}

	@Test
	@DisplayName("fireSingleShot com posição nula falha o assert")
	void fireSingleShotNulo() {
		assertThrows(AssertionError.class, () -> game.fireSingleShot(null, false));
	}

	@Test
	@DisplayName("repeatedShot com posição nula falha o assert")
	void repeatedShotNulo() {
		assertThrows(AssertionError.class, () -> game.repeatedShot(null));
	}

	@Test
	@DisplayName("printMyBoard sem mostrar tiros não marca os tiros no tabuleiro")
	void printMyBoardEscondeTiros() {
		game.fireShots(List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2)));
		saida.reset();

		game.printMyBoard(false, false);

		assertTrue(consola().contains(" A | . . . . . . . . . . |"));
	}

	@Test
	@DisplayName("printAlienBoard imprime o tabuleiro do adversário, ainda vazio")
	void printAlienBoard() {
		game.printAlienBoard(true, false);

		String out = consola();
		assertTrue(out.contains(" A | . . . . . . . . . . |"));
		assertFalse(out.contains("#"));
		assertFalse(out.contains("LEGENDA"));
	}

	@Test
	@DisplayName("printBoard escreve os números das colunas e a moldura")
	void printBoardMoldura() {
		Game.printBoard(new Fleet(), new ArrayList<>(), false, false);

		String out = consola();
		assertTrue(out.contains("     1 2 3 4 5 6 7 8 9 10"));
		assertTrue(out.contains(" J | . . . . . . . . . . |"));
	}

	@Test
	@DisplayName("over escreve a mensagem de fim de jogo")
	void over() {
		game.over();

		assertTrue(consola().contains("glub glub glub"));
	}
}
