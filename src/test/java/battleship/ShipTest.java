package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Ship.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getCategory: 1
 * - getSize: 1
 * - getBearing: 1
 * - getPositions: 1
 * - stillFloating: 2
 * - shoot: 2
 * - occupies: 2
 * - tooCloseTo (IShip): 2
 * - tooCloseTo (IPosition): 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 */
@DisplayName("Testes da classe Ship")
public class ShipTest {

    private Ship ship, shipBorder;

    @BeforeEach
    void setUp() {
        // Since Ship is abstract, instantiate it with a concrete subclass (e.g., Barge)
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        ship = null;
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("Construtor guarda categoria, direção, tamanho e posições")
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
    }

    /**
     * Test for the getCategory method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getCategory devolve 'Barca'")
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    /**
     * Test for the getSize method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getSize devolve 1")
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    /**
     * Test for the getBearing method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getBearing devolve NORTH")
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    /**
     * Test for the getPositions method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getPositions devolve a única posição (5,5)")
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("stillFloating: navio intacto flutua")
    void testStillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @Test
    @DisplayName("stillFloating: navio totalmente atingido afunda")
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    /**
     * Test for the shoot method (valid position).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("shoot: tiro na posição do navio marca-a como atingida")
    void testShoot1() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    /**
     * Test for the shoot method (invalid position).
     */
    @Test
    @DisplayName("shoot: tiro fora do navio não o afeta")
    void testShoot2() {
        Position target = new Position(0, 0);
        ship.shoot(target); // No exception expected
        assertFalse(ship.getPositions().get(0).isHit(), "Error: Position should not be marked as hit for an invalid target.");
    }

    /**
     * Test for the occupies method (position occupied).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("occupies: navio ocupa a sua posição")
    void testOccupies1() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    /**
     * Test for the occupies method (position not occupied).
     */
    @Test
    @DisplayName("occupies: navio não ocupa outra posição")
    void testOccupies2() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships too close).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("tooCloseTo(navio): navio adjacente está demasiado perto")
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships not close).
     */
    @Test
    @DisplayName("tooCloseTo(navio): navio distante não está demasiado perto")
    void testTooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions adjacent).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("tooCloseTo(posição): posição adjacente está demasiado perto")
    void testTooCloseToPosition1() {
        Position pos = new Position(5, 6); // Adjacent position
        assertTrue(ship.tooCloseTo(pos), "Error: Ship should be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions not adjacent).
     */
    @Test
    @DisplayName("tooCloseTo(posição): posição distante não está demasiado perto")
    void testTooCloseToPosition2() {
        Position pos = new Position(7, 7); // Non-adjacent position
        assertFalse(ship.tooCloseTo(pos), "Error: Ship should not be too close to the given position.");
    }

    /**
     * Test for the getTopMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getTopMostPos de navio com 1 posição")
    void testGetTopMostPos() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
    }

    /**
     * Test for the getBottomMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getBottomMostPos de navio com 1 posição")
    void testGetBottomMostPos() {
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
    }

    /**
     * Test for the getLeftMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getLeftMostPos de navio com 1 posição")
    void testGetLeftMostPos() {
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getRightMostPos de navio com 1 posição")
    void testGetRightMostPos() {
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getAdjacentPositions: 8 vizinhos no meio, 3 no canto")
    void testGetAdjacentPositions() {
        assertEquals(8, ship.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
        assertEquals(3, shipBorder.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
    }

    // ------------------------------------------------------------------
    // Novos testes (129881) para maximizar a cobertura de ramos da Ship
    // ------------------------------------------------------------------

    @Test
    @DisplayName("buildShip cria o tipo de navio certo para cada categoria")
    void testBuildShipAllKinds() {
        Position pos = new Position(2, 2);
        assertInstanceOf(Barge.class, Ship.buildShip("barca", Compass.NORTH, pos), "Error: 'barca' should build a Barge.");
        assertInstanceOf(Caravel.class, Ship.buildShip("caravela", Compass.NORTH, pos), "Error: 'caravela' should build a Caravel.");
        assertInstanceOf(Carrack.class, Ship.buildShip("nau", Compass.NORTH, pos), "Error: 'nau' should build a Carrack.");
        assertInstanceOf(Frigate.class, Ship.buildShip("fragata", Compass.NORTH, pos), "Error: 'fragata' should build a Frigate.");
        assertInstanceOf(Galleon.class, Ship.buildShip("galeao", Compass.NORTH, pos), "Error: 'galeao' should build a Galleon.");
    }

    @Test
    @DisplayName("buildShip devolve null para uma categoria desconhecida")
    void testBuildShipUnknownKind() {
        assertNull(Ship.buildShip("submarino", Compass.NORTH, new Position(2, 2)),
                "Error: an unknown ship kind should return null.");
    }

    @Test
    @DisplayName("buildShip com argumentos null falha nas asserções")
    void testBuildShipNullArguments() {
        Position pos = new Position(2, 2);
        assertThrows(AssertionError.class, () -> Ship.buildShip(null, Compass.NORTH, pos),
                "Error: null kind should fail the assertion.");
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", null, pos),
                "Error: null bearing should fail the assertion.");
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", Compass.NORTH, null),
                "Error: null position should fail the assertion.");
    }

    @Test
    @DisplayName("Construtor lança NullPointerException com categoria, direção ou posição null")
    void testConstructorNullArguments() {
        Position pos = new Position(2, 2);
        Exception e1 = assertThrows(NullPointerException.class, () -> new Barge(null, pos),
                "Error: null bearing should throw NullPointerException.");
        assertEquals("Ship's bearing must not be null", e1.getMessage(), "Error: wrong exception message for null bearing.");
        Exception e2 = assertThrows(NullPointerException.class, () -> new Barge(Compass.NORTH, null),
                "Error: null position should throw NullPointerException.");
        assertEquals("Ship's position must not be null", e2.getMessage(), "Error: wrong exception message for null position.");
    }

    @Test
    @DisplayName("getPosition devolve a posição inicial do navio")
    void testGetPosition() {
        assertEquals(new Position(5, 5), ship.getPosition(), "Error: the initial position should be (5,5).");
    }

    @Test
    @DisplayName("Extremos de navio com posições desordenadas (ramos verdadeiro e falso dos ciclos)")
    void testMostPositionsWithUnorderedPositions() {
        // Fragata com 4 posições colocadas de propósito fora de ordem, para que
        // dentro de cada ciclo a comparação seja umas vezes verdadeira e outras falsa.
        Ship frigate = new Frigate(Compass.NORTH, new Position(5, 5));
        List<IPosition> positions = frigate.getPositions();
        positions.set(0, new Position(5, 5));
        positions.set(1, new Position(3, 3));
        positions.set(2, new Position(7, 7));
        positions.set(3, new Position(4, 4));

        assertEquals(3, frigate.getTopMostPos(), "Error: the topmost row should be 3.");
        assertEquals(7, frigate.getBottomMostPos(), "Error: the bottommost row should be 7.");
        assertEquals(3, frigate.getLeftMostPos(), "Error: the leftmost column should be 3.");
        assertEquals(7, frigate.getRightMostPos(), "Error: the rightmost column should be 7.");
    }

    @Test
    @DisplayName("Extremos de um Galeão real a ESTE")
    void testMostPositionsGalleon() {
        Ship galleon = new Galleon(Compass.EAST, new Position(5, 5));
        assertEquals(5, galleon.getTopMostPos(), "Error: the topmost row should be 5.");
        assertEquals(7, galleon.getBottomMostPos(), "Error: the bottommost row should be 7.");
        assertEquals(3, galleon.getLeftMostPos(), "Error: the leftmost column should be 3.");
        assertEquals(5, galleon.getRightMostPos(), "Error: the rightmost column should be 5.");
    }

    @Test
    @DisplayName("stillFloating: navio grande com só uma posição atingida continua a flutuar")
    void testStillFloatingPartiallyHit() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(2, 2));
        frigate.getPositions().get(0).shoot();
        assertTrue(frigate.stillFloating(), "Error: a partially hit ship should still be floating.");
    }

    @Test
    @DisplayName("occupies: navio grande ocupa a sua última posição")
    void testOccupiesLastPosition() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(2, 2));
        assertTrue(frigate.occupies(new Position(5, 2)), "Error: the frigate should occupy (5,2).");
    }

    @Test
    @DisplayName("occupies, tooCloseTo e shoot com argumentos null falham nas asserções")
    void testNullArgumentsAssertions() {
        assertThrows(AssertionError.class, () -> ship.occupies(null), "Error: occupies(null) should fail the assertion.");
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null), "Error: tooCloseTo(null ship) should fail the assertion.");
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null), "Error: tooCloseTo(null position) should fail the assertion.");
        assertThrows(AssertionError.class, () -> ship.shoot(null), "Error: shoot(null) should fail the assertion.");
    }

    @Test
    @DisplayName("shoot fora do tabuleiro falha na asserção isInside")
    void testShootOutsideBoard() {
        assertThrows(AssertionError.class, () -> ship.shoot(new Position(-1, 5)),
                "Error: shooting outside the board should fail the assertion.");
    }

    @Test
    @DisplayName("tooCloseTo(navio): navio grande perto apenas pela última posição")
    void testTooCloseToShipByLastPosition() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(1, 5)); // ocupa (1,5)..(4,5)
        assertTrue(ship.tooCloseTo(frigate), "Error: (4,5) is adjacent to (5,5), so ships should be too close.");
    }

    @Test
    @DisplayName("getAdjacentPositions de uma Caravela não repete posições nem inclui as suas")
    void testGetAdjacentPositionsCaravel() {
        Ship caravel = new Caravel(Compass.NORTH, new Position(5, 5)); // ocupa (5,5) e (6,5)
        List<IPosition> adjacents = caravel.getAdjacentPositions();
        assertEquals(10, adjacents.size(), "Error: a vertical caravel in the middle should have 10 adjacent positions.");
        assertFalse(adjacents.contains(new Position(5, 5)), "Error: the ship's own positions must not be adjacent.");
        assertFalse(adjacents.contains(new Position(6, 5)), "Error: the ship's own positions must not be adjacent.");
    }

    @Test
    @DisplayName("isInsideBoard: navio dentro do tabuleiro")
    void testIsInsideBoardTrue() {
        assertTrue(ship.isInsideBoard(), "Error: a ship at (5,5) should be inside the board.");
        assertTrue(shipBorder.isInsideBoard(), "Error: a ship at (0,0) should be inside the board.");
    }

    @Test
    @DisplayName("isInsideBoard: navio fora por cada um dos 4 lados")
    void testIsInsideBoardFalse() {
        assertFalse(new Barge(Compass.NORTH, new Position(5, -1)).isInsideBoard(), "Error: column -1 is outside (left).");
        assertFalse(new Barge(Compass.NORTH, new Position(5, 10)).isInsideBoard(), "Error: column 10 is outside (right).");
        assertFalse(new Barge(Compass.NORTH, new Position(-1, 5)).isInsideBoard(), "Error: row -1 is outside (top).");
        assertFalse(new Barge(Compass.NORTH, new Position(10, 5)).isInsideBoard(), "Error: row 10 is outside (bottom).");
    }

    @Test
    @DisplayName("sink atinge todas as posições e o navio afunda")
    void testSink() {
        Ship frigate = new Frigate(Compass.EAST, new Position(2, 2));
        frigate.sink();
        for (IPosition p : frigate.getPositions())
            assertTrue(p.isHit(), "Error: every position should be hit after sink().");
        assertFalse(frigate.stillFloating(), "Error: the ship should not float after sink().");
    }

    @Test
    @DisplayName("toString mostra categoria, direção e posição")
    void testToString() {
        assertEquals("[Barca n F6]", ship.toString(), "Error: toString should be '[Barca n F6]'.");
    }
}