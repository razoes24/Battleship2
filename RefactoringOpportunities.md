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
| Ship::isInsideBoard | Condição complexa (4 condições num só return) | Introduce Variable | 129881 |
| Ship::getAdjacentPositions | Variável temporária desnecessária (adjacents) | Inline Variable | 129881 |
| Ship::buildShip | Nome pouco expressivo (variável s) | Rename | 129881 |
| Ship::buildShip | Dependência de classe concreta (Position em vez de IPosition) | Change Signature | 129881 |
| Barge, Caravel, Carrack, Frigate, Galleon | Magic String (categoria no super(...)) | Introduce Constant | 129881 |
| Caravel::Caravel | Duplicated Code (ciclos repetidos no switch) | Extract Method | 129881 |
| Caravel → Ship (fillVertical, fillHorizontal) | Duplicated Code entre subclasses | Pull Members Up | 129881 |
| Ship (campo positions) | Campo protected acessível às subclasses (falta de encapsulamento) | Encapsulate Fields | 129881 |
| Ship (campo size) | Uso desnecessário de Integer (wrapper) | Type Migration | 129881 |