package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Test class for Fleet.
 * Author: Bruno Frasco - 129856
 * Date: 18/11/2026
 * Time: 20h39
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - createRandom: 4
 * - addShip: 5
 * - getShips: 1
 * - getShipsLike: 3
 * - getFloatingShips: 3
 * - getSunkShips: 3
 * - shipAt: 3
 * - colisionRisk: 3
 * - printShips: 2
 * - printStatus: 1
 * - printShipsByCategory: 1
 * - printFloatingShips: 1
 * - printAllShips: 1
 */
	public class FleetTest {

		private Fleet fleet;

		@BeforeEach
		void setUp() {
			fleet = new Fleet();
		}

		@AfterEach
		void tearDown() {
			fleet = null;
		}

		/**
		 * Test for the Fleet constructor.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testConstructor() {
			assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
		}

		@Test
		void testCreateRandom(){
				IFleet r = Fleet.createRandom();
				assertNotNull(r, "Error: createRandom should return a non-null fleet instance.");
				assertEquals(Fleet.FLEET_SIZE, r.getShips().size(), "Error: createRandom should create a fleet with the correct number of ships.");

		}

		/**
		 * Test for the addShip method (all conditions true).
		 * Cyclomatic Complexity: 3
		 */
		@Test
		void testAddShip1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
		}

		/**
		 * Test for the addShip method (fleet size limit reached).
		 */
		@Test
		@DisplayName("Não deve adicionar navio quando o limite da frota for atingido")
		void testAddShip2() {
			for (int i = 0; i <= Fleet.FLEET_SIZE; i++) {
				fleet.addShip(new Barge(Compass.NORTH,new Position((i / 5) * 2, (i % 5) * 2)));
			}
			IShip anotherShip = new Barge(Compass.NORTH, new Position(8, 8));
			assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
		}

		/**
		 * Test for the addShip method (ship outside the board).
		 */
		@Test
		void testAddShip3() {
			IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
			assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
		}

		/**
		 * Test for the addShip method (collision risk).
		 */
		@Test
		void testAddShip4() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);
			assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
		}

		@Test
		@DisplayName("Deve lançar AssertionError ao tentar adicionar um navio nulo")
		void testAddShip5() {
				assertThrows(AssertionError.class, () -> fleet.addShip(null), "Error: Adicionar um ship null deve devolver AssertionError.");
		}

		/**
		 * Test for the getShips method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testGetShips() {
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
			assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
		}

		/**
		 * Test for the getShipsLike method (ships of specific category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("Deve devolver apenas os navios da categoria especificada")
		void testGetShipsLike() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> barges = fleet.getShipsLike("Barca");
			assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
			assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
		}

		@Test
		@DisplayName("Deve lançar AssertionError ao pesquisar navios com categoria nula")
		void testGetShipsLikeNull() {
			assertThrows(AssertionError.class, () -> fleet.getShipsLike(null), "Error: Adicionar uma categoria null deve devolver AssertionError.");
		}

		@Test
		@DisplayName("Deve devolver apenas os navios afundados da frota")
		void testGetSunkShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			ship1.getPositions().get(0).shoot();
			List<IShip> sunkShips = fleet.getSunkShips();
			assertEquals(1, sunkShips.size(), "Erro: Deveria existir exatamente 1 navio afundado na lista.");
			assertEquals(ship1, sunkShips.get(0), "Erro: O navio afundado devolvido não corresponde ao navio afundado.");
		}

		/**
		 * Test for the getFloatingShips method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetFloatingShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> floatingShips = fleet.getFloatingShips();
			assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

			ship1.getPositions().get(0).shoot();  // Sink ship1
			floatingShips = fleet.getFloatingShips();
			assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
			assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
		}

		/**
		 * Test for the shipAt method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testShipAt() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
			assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
		}

		@Test
		@DisplayName("Deve lançar AssertionError se a posicao for null")
		void testShipAtNULL() {
			assertThrows(AssertionError.class, () -> fleet.shipAt(null), "Error: Adicionar uma posicao null deve devolver AssertionError.");
		}

		/**
		 * Test for private method colisionRisk.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testColisionRisk() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);

			assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
			assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
					"Error: Ships at non-overlapping positions should not have a collision risk.");
		}

		@Test
		@DisplayName("Deve lançar AssertionError se o ship for null")
		void testColisionRiskNULL() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);
			InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> method.invoke(fleet, (IShip) null), "Erro: Era esperada uma InvocationTargetException ao invocar colisionRisk com null.");
			assertInstanceOf(AssertionError.class, ex.getCause(), "Erro: Era esperado que a causa fosse AssertionError quando o navio é null.");
		}

		/**
		 * Test for the printStatus method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testPrintStatus() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
		}

		@Test
		@DisplayName("Deve lançar AssertionError se a lista de ships for null")
		void testPrintShipsNull() {
			assertThrows(AssertionError.class, () -> Fleet.printShips(null), "Error: Adicionar uma posicao null deve devolver AssertionError.");
		}

		@Test
		@DisplayName("Deve imprimir a lista de navios fornecida sem lançar exceções")
		void testPrintShips() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertDoesNotThrow(() -> Fleet.printShips(fleet.getShips()), "Erro: Imprimir uma lista válida de navios não deve lançar exceção.");
		}

		@Test
		@DisplayName("Deve imprimir os navios por categoria, a flutuar e todos os navios sem lançar exceções")
		void testPrintALL() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertAll(
					() -> assertDoesNotThrow(() -> fleet.printShipsByCategory("Barca"), "Erro: printShipsByCategory não deveria lançar exceção."),
					() -> assertDoesNotThrow(() -> fleet.printFloatingShips(), "Erro: printFloatingShips não deveria lançar exceção."),
					() -> assertDoesNotThrow(() -> fleet.printAllShips(), "Erro: printAllShips não deveria lançar exceção.")
			);
		}

		@Test
		@DisplayName("Deve lançar AssertionError se a categoria for null ao imprimir navios por categoria")
		void testPrintShipsByCategoryNull() {
			assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null), "Erro: Imprimir navios com uma categoria nula deve lançar AssertionError.");
		}
	}