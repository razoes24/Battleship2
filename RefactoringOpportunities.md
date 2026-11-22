# Oportunidades de refabricação

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
| Game::randomEnemyFire | Long Method | Extract Method | 131834 |
| Game::fireShots | Long Method | Extract Method | 131834 |
| Game::getRemainingShips | Variável temporária desnecessária | Inline Variable | 131834 |
| GameSession | Magic String (literais repetidos) | Extract Constant | 131834 |
| Game::printBoard, Game::printMyBoard, Game::printAlienBoard, IGame | Nomes fora da convenção Java (snake_case) | Rename | 131834 |
| Game (contadores) | Uso desnecessário de Integer (wrapper) | Type Migration | 131834 |
| Game::fireSingleShot | Expressão duplicada (`!ship.stillFloating()`) | Introduce Variable | 131834 |
| GameSession::shotsPerTurn | Speculative Generality (campo redundante) | Inline Field | 131834 |


| Fleet::createRandom | Long Method / Lista de tipos acoplada ao método | Extract Method | 129856 |
| Fleet::createRandomShip | Variável temporária desnecessária (`ship`) | Inline Variable | 129856 |
| Fleet::addShip | Nome de parâmetro pouco descritivo (`s`) | Rename | 129856 |
| Fleet::printShips | Método de instância que não acede ao estado (`this`) | Make Static | 129856 |
| Position::Position(char, int) | Magic Char (`'A'`) | Introduce Constant | 129856 |
| Position::adjacentPositions | Matriz de direções criada a cada invocação | Introduce Field | 129856 |
| Position::isAdjacentTo | Expressão booleana longa | Introduce Variable | 129856 |