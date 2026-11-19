package battleship;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Position.
 * Author: Bruno Frasco - 129856
 * Date: 19/11/2026 13h19
 * Cyclomatic Complexity for each method:
 * - randomPosition(): 1
 * - Position(char, int): 1
 * - Position(int, int): 1
 * - getRow(): 1
 * - getColumn(): 1
 * - getClassicRow(): 1
 * - getClassicColumn(): 1
 * - isInside(): 4
 * - isAdjacentTo(): 2
 * - adjacentPositions(): 3
 * - isOccupied(): 1
 * - isHit(): 1
 * - occupy(): 1
 * - shoot(): 1
 * - equals(): 4
 * - hashCode(): 1
 * - toString(): 1
 */
public class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	//	position = new Position('C', 4);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}


	@Test
	@DisplayName("randomPosition(): Deve gerar uma posição não nula dentro dos limites válidos do tabuleiro")
	void randomPosition() {
		Position randomPos = Position.randomPosition();

		assertAll("Validação da posição aleatória gerada",
				() -> assertNotNull(randomPos, "Error: expected a non-null Position instance but got null"),
				() -> assertTrue(randomPos.getRow() >= 0 && randomPos.getRow() < Game.BOARD_SIZE,
						"Error: expected row within [0, " + (Game.BOARD_SIZE - 1) + "] but got " + randomPos.getRow()),
				() -> assertTrue(randomPos.getColumn() >= 0 && randomPos.getColumn() < Game.BOARD_SIZE,
						"Error: expected column within [0, " + (Game.BOARD_SIZE - 1) + "] but got " + randomPos.getColumn())
		);
	}

	@Test
	@DisplayName("Position(char, int): Deve converter coordenadas clássicas e inicializar isOccupied e isHit a false")
	void Position1() {
		// 1. Chama especificamente o construtor (char, int)
		Position classicPos = new Position('c', 4);

		// 2. Verifica todas as atribuições feitas dentro desse construtor
		assertAll("Validação do construtor Position(char, int)",
				() -> assertEquals(2, classicPos.getRow(),
						"Erro: esperava-se a linha 2 para 'c', mas obteve-se " + classicPos.getRow()),
				() -> assertEquals(3, classicPos.getColumn(),
						"Erro: esperava-se a coluna 3 para a coluna clássica 4, mas obteve-se " + classicPos.getColumn()),
				() -> assertFalse(classicPos.isOccupied(),
						"Erro: esperava-se que a posição não estivesse ocupada, mas estava"),
				() -> assertFalse(classicPos.isHit(),
						"Erro: esperava-se que a posição não tivesse sido atingida, mas foi")
		);
	}

	@Test
	@DisplayName("getClassicColumn(): Deve devolver o índice da coluna acrescido de 1")
	void getClassicColumn() {
		Position pos = new Position(2, 4);
		int actual = pos.getClassicColumn();

		assertEquals(5, actual, "Erro: esperava-se a coluna clássica 5, mas obteve-se " + actual);
	}

	@Test
	void constructor() {
		Position pos = new Position(1, 1);
		assertNotNull(pos, "Failed to create Position: object is null");
		assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow());
		assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn());
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

	@Test
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	void isValid1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be valid");
	}

	@Test
	void isValid2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should be invalid");
	}

	@Test
	void isValid3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should be invalid");
	}

	@Test
	void isValid4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should be invalid");
	}

	@Test
	void isValid5() {
		position = new Position(5, Game.BOARD_SIZE);
		assertFalse(position.isInside(), "Position with column >= BOARD_SIZE should be invalid");
	}

	@Test
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@Test
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@Test
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@Test
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo5(): Deve devolver false quando a linha é igual mas a diferença de colunas é superior a 1")
	void isAdjacentTo5() {
		Position other = new Position(2, 5);
		assertFalse(position.isAdjacentTo(other),
				"Erro: esperava-se false para posição não adjacente na coluna, mas obteve-se true");
	}

	@Test
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"isAdjacentTo should throw NullPointerException for null input");
	}

	@Test
	@DisplayName("adjacentPositions1(): Deve devolver 8 posições adjacentes para uma posição no interior do tabuleiro")
	void adjacentPositions1() {
		// Assumindo que this.position no @BeforeEach é (2, 3) (interior do tabuleiro)
		List<IPosition> adjacents = position.adjacentPositions();

		assertAll("Validação das 8 posições adjacentes no interior",
				() -> assertEquals(8, adjacents.size(),
						"Erro: esperava-se 8 posições adjacentes, mas obteve-se " + adjacents.size()),
				() -> assertTrue(adjacents.contains(new Position(1, 3)),
						"Erro: esperava-se que a lista contivesse a posição norte (1, 3)"),
				() -> assertTrue(adjacents.contains(new Position(3, 4)),
						"Erro: esperava-se que a lista contivesse a posição sudeste (3, 4)")
		);
	}

	@Test
	@DisplayName("adjacentPositions2(): Deve devolver apenas as 3 posições válidas para uma posição num canto")
	void adjacentPositions2() {
		Position corner = new Position(0, 0);
		List<IPosition> adjacents = corner.adjacentPositions();

		assertAll("Validação das 3 posições adjacentes no canto (0, 0)",
				() -> assertEquals(3, adjacents.size(),
						"Erro: esperava-se 3 posições adjacentes no canto, mas obteve-se " + adjacents.size()),
				() -> assertTrue(adjacents.contains(new Position(0, 1)),
						"Erro: esperava-se que a lista contivesse (0, 1)"),
				() -> assertTrue(adjacents.contains(new Position(1, 0)),
						"Erro: esperava-se que a lista contivesse (1, 0)"),
				() -> assertTrue(adjacents.contains(new Position(1, 1)),
						"Erro: esperava-se que a lista contivesse (1, 1)")
		);
	}

	@Test
	@DisplayName("adjacentPositions3(): Deve devolver uma lista vazia para uma posição fora do tabuleiro")
	void adjacentPositions3() {
		Position outside = new Position(-5, -5);
		List<IPosition> adjacents = outside.adjacentPositions();

		assertTrue(adjacents.isEmpty(),
				"Erro: esperava-se uma lista vazia para posição fora do tabuleiro, mas obteve-se " + adjacents.size() + " elementos");
	}

	@Test
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@Test
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@Test
	void equals1() {
		Position same = new Position(2, 3);
		assertTrue(position.equals(same), "Equal positions not identified as equal");
	}

	@Test
	void equals2() {
		assertFalse(position.equals(null), "Position should not equal null");
	}

	@Test
	void equals3() {
		Object other = new Object();
		assertFalse(position.equals(other), "Position should not equal non-Position object");
	}

	@Test
	void equals4() {
		Position other = new Position(2, 4);
		assertFalse(position.equals(other), "Positions with the same row but different column should not be equal");
	}

	@Test
	void equals5() {
		assertTrue(position.equals(position), "A position should be equal to itself");
	}

	@Test
	@DisplayName("equals6(): Deve devolver false quando as posições têm linhas diferentes")
	void equals6() {
		Position other = new Position(5, 3);
		assertFalse(position.equals(other), "Erro: esperava-se false para posições com linhas diferentes, mas obteve-se true");
	}

	@Test
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

	@Test
	void toStringFormat() {
//		String expected = "Row = C, Column = 4";
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
	}
}