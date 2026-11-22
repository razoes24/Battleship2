package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Move.
 * Author: Bruno Frasco - 129856
 * Date: 22/11/2026
 * Time: 18h12
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - toString: 1
 * - getNumber: 1
 * - getShots: 1
 * - getShotResults: 1
 * - processEnemyFire: 34
 */
class MoveTest {

    private IPosition pos1;
    private IPosition pos2;

    @BeforeEach
    void setUp() {
        pos1 = createMockPosition(0, 0);
        pos2 = createMockPosition(1, 1);
    }

    @AfterEach
    void tearDown() {
        pos1 = null;
        pos2 = null;
    }

    @Test
    @DisplayName("Getters e toString retornam os dados corretos da jogada")
    void testGettersAndToString() {
        List<IPosition> shots = List.of(pos1, pos2);
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );

        Move move = new Move(3, shots, results);

        assertEquals(3, move.getNumber());
        assertEquals(shots, move.getShots());
        assertEquals(results, move.getShotResults());
        assertEquals("Move{number=3, shots=2, results=2}", move.toString());
    }

    @Test
    @DisplayName("Processa apenas tiros repetidos com verbose ativo (singular e plural)")
    void testProcessEnemyFireOnlyRepeatedVerbose() {
        // Caso 1: 1 tiro repetido, 0 válidos, restantes exteriores
        Move moveSingle = new Move(1, List.of(pos1), List.of(
                new IGame.ShotResult(true, true, null, false)
        ));
        String jsonSingle = moveSingle.processEnemyFire(true);
        assertTrue(jsonSingle.contains("\"repeatedShots\" : 1"));
        assertTrue(jsonSingle.contains("\"validShots\" : 0"));

        // Caso 2: 2 tiros repetidos, 0 válidos
        Move movePlural = new Move(2, List.of(pos1, pos2), List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        ));
        String jsonPlural = movePlural.processEnemyFire(true);
        assertTrue(jsonPlural.contains("\"repeatedShots\" : 2"));
    }

    @Test
    @DisplayName("Processa múltiplos barcos afundados do mesmo tipo, acertos sem afundar e água (verbose = true)")
    void testProcessEnemyFireMixedHitsSunkAndMissedVerbose() {
        IShip barca1 = createMockShip("Barca");
        IShip barca2 = createMockShip("Barca");
        IShip fragata = createMockShip("Fragata");

        // 2 Barcas afundadas (count > 1), 2 tiros na Fragata sem afundar (hits > 1), 2 tiros na água (missed > 1)
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca1, true),
                new IGame.ShotResult(true, false, barca2, true),
                new IGame.ShotResult(true, false, fragata, false),
                new IGame.ShotResult(true, false, fragata, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );

        Move move = new Move(4, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 6"));
        assertTrue(json.contains("\"missedShots\" : 2"));
        assertTrue(json.contains("\"type\" : \"Barca\""));
        assertTrue(json.contains("\"count\" : 2"));
        assertTrue(json.contains("\"type\" : \"Fragata\""));
        assertTrue(json.contains("\"hits\" : 2"));
    }

    @Test
    @DisplayName("Processa navio afundado e acerto único sem tiros na água (remove '+' final e cobre singulares)")
    void testProcessEnemyFireSingleSunkAndSingleHitNoMissesVerbose() {
        IShip galeao = createMockShip("Galeao");
        IShip caravela = createMockShip("Caravela");

        // 1 Galeao afundado (count == 1), 1 tiro na Caravela (hits == 1), 0 água, 1 repetido
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, galeao, true),
                new IGame.ShotResult(true, false, caravela, false),
                new IGame.ShotResult(true, true, null, false)
        );

        Move move = new Move(5, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 2"));
        assertTrue(json.contains("\"repeatedShots\" : 1"));
        assertTrue(json.contains("\"missedShots\" : 0"));
    }

    @Test
    @DisplayName("Processa 1 tiro válido na água e 1 tiro inválido com verbose ativo e inativo")
    void testProcessEnemyFireSingleWaterAndInvalidShot() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false), // Inválido -> continue
                new IGame.ShotResult(true, false, null, false)   // 1 tiro válido na água
        );

        Move move = new Move(6, Collections.emptyList(), results);

        String jsonVerbose = move.processEnemyFire(true);
        String jsonSilent = move.processEnemyFire(false);

        assertEquals(jsonVerbose, jsonSilent);
        assertTrue(jsonSilent.contains("\"validShots\" : 1"));
        assertTrue(jsonSilent.contains("\"missedShots\" : 1"));
    }

    @Test
    @DisplayName("Processa apenas tiros inválidos e fora do tabuleiro sem gerar exceções de formatação")
    void testProcessEnemyFireAllInvalidWithVerbose() {
        // validShots == 0, repeatedShots == 0, outsideShots == Game.NUMBER_SHOTS (output inicialmente vazio)
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false)
        );

        Move move = new Move(7, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 0"));
        assertTrue(json.contains("\"repeatedShots\" : 0"));
        assertTrue(json.contains("\"outsideShots\" : " + Game.NUMBER_SHOTS));
    }

    @Test
    @DisplayName("Cobre ramo onde não há barcos afundados mas existem acertos sem água")
    void testProcessEnemyFireOnlyNonSunkHitsNoMissesVerbose() {
        IShip nau = createMockShip("Nau");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, nau, false)
        );

        Move move = new Move(8, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 1"));
        assertTrue(json.contains("\"missedShots\" : 0"));
    }

    @Test
    @DisplayName("Cobre ramo onde existem barcos afundados e nenhum acerto adicional sem água")
    void testProcessEnemyFireOnlySunkShipNoMissesVerbose() {
        IShip barca = createMockShip("Barca");
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barca, true)
        );

        Move move = new Move(9, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 1"));
        assertTrue(json.contains("\"count\" : 1"));
    }

    // ── Proxies dinâmicos para isolar a unidade Move ─────────────────────────

    private static IShip createMockShip(String category) {
        return (IShip) Proxy.newProxyInstance(
                IShip.class.getClassLoader(),
                new Class<?>[]{IShip.class},
                (proxy, method, args) -> {
                    if ("getCategory".equals(method.getName())) {
                        return category;
                    }
                    return null;
                }
        );
    }

    private static IPosition createMockPosition(int row, int col) {
        return (IPosition) Proxy.newProxyInstance(
                IPosition.class.getClassLoader(),
                new Class<?>[]{IPosition.class},
                (proxy, method, args) -> {
                    if ("getRow".equals(method.getName())) return row;
                    if ("getColumn".equals(method.getName())) return col;
                    return null;
                }
        );
    }

    @Test
    @DisplayName("Cobre plural de tiros repetidos quando existem simultaneamente tiros válidos (verbose = true)")
    void testProcessEnemyFireValidAndMultipleRepeatedShotsVerbose() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false), // 1 tiro válido na água
                new IGame.ShotResult(true, true, null, false),  // 1º tiro repetido
                new IGame.ShotResult(true, true, null, false)   // 2º tiro repetido (repeatedShots > 1)
        );

        Move move = new Move(11, Collections.emptyList(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 1"));
        assertTrue(json.contains("\"repeatedShots\" : 2"));
    }
}