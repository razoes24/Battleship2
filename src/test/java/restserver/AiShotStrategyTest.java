package restserver;

import battleship.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AiShotStrategy.
 * Author: Bruno Frasco - 129856
 * Date: 22/11/2026
 * Time:17h55
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - computeShots: 17
 * - getHitButNotSunkPositions: 10
 * - buildHuntPool: 6
 */

class AiShotStrategyTest {

    private AiShotStrategy strategy;
    private List<IMove> alienMoves;
    private IGame game;

    @BeforeEach
    void setUp() {
        strategy = new AiShotStrategy();
        alienMoves = new ArrayList<>();
        game = createMockGame(alienMoves);
    }

    @AfterEach
    void tearDown() {
        strategy = null;
        alienMoves = null;
        game = null;
    }

    @Test
    @DisplayName("Jogo sem jogadas anteriores escolhe tiros no padrão xadrez")
    void testComputeShotsHuntPhaseInitial() {
        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size(), "Deve retornar exatamente NUMBER_SHOTS tiros");
        assertEquals(Game.NUMBER_SHOTS, new HashSet<>(shots).size(), "Os tiros não devem ser repetidos");

        for (IPosition p : shots) {
            assertEquals(0, (p.getRow() + p.getColumn()) % 2,
                    "Na fase HUNT inicial, todas as posições devem respeitar o padrão xadrez (row + col) % 2 == 0");
        }
    }

    @Test
    @DisplayName("Prioriza posições adjacentes a navios atingidos mas não afundados")
    void testComputeShotsTargetPhasePrioritizesAdjacent() {
        IPosition hitPos = new Position(5, 5);
        IGame.ShotResult hitResult = new IGame.ShotResult(true, false, createMockShip(), false);

        alienMoves.add(createMockMove(List.of(hitPos), List.of(hitResult)));

        List<IPosition> shots = strategy.computeShots(game);
        List<IPosition> adjacentToHit = hitPos.adjacentPositions();

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        int expectedAdjacentCount = Math.min(Game.NUMBER_SHOTS, adjacentToHit.size());
        for (int i = 0; i < expectedAdjacentCount; i++) {
            assertTrue(adjacentToHit.contains(shots.get(i)),
                    "O tiro " + shots.get(i) + " deveria ser adjacente a " + hitPos);
        }
    }

    @Test
    @DisplayName("Hit no canto e hits com adjacentes partilhados cobrem fim de adjacentes, duplicados e buildHuntPool")
    void testTargetCornerAndSharedEvenAdjacents() {
        // 1. Hit em (0, 0) tem apenas 2 adjacentes: (0, 1) e (1, 0) -> o loop da linha 48 termina naturalmente!
        //    (Assumindo NUMBER_SHOTS >= 3; se NUMBER_SHOTS == 3, sobra 1 vaga para o segundo hit)
        // 2. Hit em (1, 1) também tem (0, 1) e (1, 0) como adjacentes -> '!chosen.contains(neighbour)' na linha 50 dá false!
        // 3. Hit em (0, 1) tem adjacente par (0, 0) -> entra em 'chosen' e faz '!alreadyChosen.contains(p)' na linha 126 dar false!
        IPosition cornerHit = new Position(0, 0);
        IPosition diagHit = new Position(1, 1);
        IPosition evenAdjHit = new Position(2, 1); // adjacente par (2, 0) ou (2, 2)
        IPosition alreadyShotAdj = new Position(0, 1);

        IGame.ShotResult hitResult = new IGame.ShotResult(true, false, createMockShip(), false);
        IGame.ShotResult missResult = new IGame.ShotResult(true, false, null, false);

        alienMoves.add(createMockMove(
                List.of(cornerHit, diagHit, evenAdjHit, alreadyShotAdj),
                List.of(hitResult, hitResult, hitResult, missResult)
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertFalse(shots.contains(alreadyShotAdj));
        assertEquals(shots.size(), new HashSet<>(shots).size());
    }

    @Test
    @DisplayName("Cobre limite de NUMBER_SHOTS quando existem muitos alvos ativos")
    void testComputeShotsTargetPhaseBreaksAtMaxShots() {
        List<IPosition> hits = List.of(new Position(1, 1), new Position(3, 3), new Position(7, 7));
        IGame.ShotResult hitResult = new IGame.ShotResult(true, false, createMockShip(), false);
        List<IGame.ShotResult> results = List.of(hitResult, hitResult, hitResult);

        alienMoves.add(createMockMove(hits, results));

        List<IPosition> shots = strategy.computeShots(game);
        assertEquals(Game.NUMBER_SHOTS, shots.size(), "Deve parar exatamente ao atingir NUMBER_SHOTS");
    }

    @Test
    @DisplayName("Ignora tiros na água, navios afundados, inválidos, repetidos ou sem metadados")
    void testGetHitButNotSunkEdgeCases() {
        IPosition pWater = new Position(0, 0);
        IPosition pSunk = new Position(1, 1);
        IPosition pInvalid = new Position(2, 2);
        IPosition pRepeated = new Position(3, 3);
        IPosition pMissingResult = new Position(4, 4);

        List<IPosition> move1Shots = List.of(pWater, pSunk, pInvalid, pRepeated, pMissingResult);
        List<IGame.ShotResult> move1Results = List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, createMockShip(), true),
                new IGame.ShotResult(false, false, createMockShip(), false),
                new IGame.ShotResult(true, true, createMockShip(), false)
        );

        IPosition pEmptyResults = new Position(6, 6);
        alienMoves.add(createMockMove(move1Shots, move1Results));
        alienMoves.add(createMockMove(List.of(pEmptyResults), Collections.emptyList()));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        for (IPosition p : shots) {
            assertEquals(0, (p.getRow() + p.getColumn()) % 2,
                    "Como não há hits válidos não afundados, deve recorrer apenas à fase HUNT");
        }
    }

    @Test
    @DisplayName("Preenche com células ímpares quando o padrão xadrez está esgotado")
    void testSafetyFallbackWhenCheckerboardIsExhausted() {
        List<IPosition> allEvenCells = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();
        IGame.ShotResult miss = new IGame.ShotResult(true, false, null, false);

        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                if ((r + c) % 2 == 0) {
                    allEvenCells.add(new Position(r, c));
                    results.add(miss);
                }
            }
        }
        alienMoves.add(createMockMove(allEvenCells, results));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size(), "Deve recorrer ao fallback para completar NUMBER_SHOTS");
        for (IPosition p : shots) {
            assertEquals(1, (p.getRow() + p.getColumn()) % 2,
                    "Com as células pares esgotadas, o fallback deve escolher células onde (r + c) % 2 == 1");
            assertFalse(allEvenCells.contains(p));
        }
    }

    @Test
    @DisplayName("Retorna lista vazia ou parcial se o tabuleiro estiver totalmente esgotado")
    void testBoardCompletelyExhausted() {
        List<IPosition> allCells = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();
        IGame.ShotResult miss = new IGame.ShotResult(true, false, null, false);

        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                allCells.add(new Position(r, c));
                results.add(miss);
            }
        }
        alienMoves.add(createMockMove(allCells, results));

        List<IPosition> shots = strategy.computeShots(game);
        assertTrue(shots.isEmpty(), "Se todas as células do tabuleiro já foram disparadas, deve retornar lista vazia");
    }

    // ── Proxies dinâmicos (evita erros de assinaturas nas interfaces) ──────────

    private static IGame createMockGame(List<IMove> moves) {
        return (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> {
                    if ("getAlienMoves".equals(method.getName())) {
                        return moves;
                    }
                    return null;
                }
        );
    }

    private static IMove createMockMove(List<IPosition> shots, List<IGame.ShotResult> results) {
        return (IMove) Proxy.newProxyInstance(
                IMove.class.getClassLoader(),
                new Class<?>[]{IMove.class},
                (proxy, method, args) -> {
                    if ("getShots".equals(method.getName())) {
                        return shots;
                    }
                    if ("getShotResults".equals(method.getName())) {
                        return results;
                    }
                    return null;
                }
        );
    }

    private static IShip createMockShip() {
        return (IShip) Proxy.newProxyInstance(
                IShip.class.getClassLoader(),
                new Class<?>[]{IShip.class},
                (proxy, method, args) -> null
        );
    }

    @Test
    @DisplayName("Transição parcial entre HUNT e Safety Fallback quando resta apenas uma célula par")
    void testHuntPoolSmallerThanNumberShotsTransitionsToFallback() {
        List<IPosition> shotCells = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();
        IGame.ShotResult miss = new IGame.ShotResult(true, false, null, false);

        // Disparar todas as células pares EXCEPTO a (0, 0)
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                if ((r + c) % 2 == 0 && !(r == 0 && c == 0)) {
                    shotCells.add(new Position(r, c));
                    results.add(miss);
                }
            }
        }
        alienMoves.add(createMockMove(shotCells, results));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size(), "Deve completar NUMBER_SHOTS combinando HUNT e Fallback");
        assertTrue(shots.contains(new Position(0, 0)), "A única célula par restante (0,0) deve ser escolhida na fase HUNT");
        assertEquals(shots.size(), new HashSet<>(shots).size(), "O fallback não deve duplicar a célula (0,0) já escolhida no HUNT");
    }

    @Test
    @DisplayName("Hit em posição com adjacentes pares evita duplicação no buildHuntPool e completa no fallback")
    void testTargetWithEvenAdjacentsAndLastRowFallback() {
        // Hit em (0, 1): os seus adjacentes válidos são (0, 0), (0, 2) e (1, 1) -> todos têm (r + c) % 2 == 0!
        // Assim, entram em 'chosen' na fase TARGET e forçam '!alreadyChosen.contains(p)' a ser false no buildHuntPool.
        IPosition hitPos = new Position(0, 1);
        IGame.ShotResult hitResult = new IGame.ShotResult(true, false, createMockShip(), false);

        alienMoves.add(createMockMove(List.of(hitPos), List.of(hitResult)));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertEquals(shots.size(), new HashSet<>(shots).size(), "Não deve duplicar posições pares entre TARGET e HUNT");
    }

    @Test
    @DisplayName("buildHuntPool exclui explicitamente posições já presentes em alreadyChosen")
    void testBuildHuntPoolExcludesAlreadyChosenDirectly() throws Exception {
        Method buildHuntPool = AiShotStrategy.class.getDeclaredMethod(
                "buildHuntPool", Set.class, List.class);
        buildHuntPool.setAccessible(true);

        IPosition alreadyChosenPos = new Position(0, 0);
        @SuppressWarnings("unchecked")
        List<IPosition> pool = (List<IPosition>) buildHuntPool.invoke(
                strategy, Collections.emptySet(), List.of(alreadyChosenPos));

        assertFalse(pool.contains(alreadyChosenPos),
                "A posição (0, 0) já presente em alreadyChosen não deve ser incluída no pool");
    }
}