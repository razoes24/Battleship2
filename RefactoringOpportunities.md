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
| Ship::isInsideBoard | Condição complexa (4 condições num só return) | Introduce Variable | 129881 |
| Ship::getAdjacentPositions | Variável temporária desnecessária (adjacents) | Inline Variable | 129881 |
| Ship::buildShip | Nome pouco expressivo (variável s) | Rename | 129881 |
| Ship::buildShip | Dependência de classe concreta (Position em vez de IPosition) | Change Signature | 129881 |
| Barge, Caravel, Carrack, Frigate, Galleon | Magic String (categoria no super(...)) | Introduce Constant | 129881 |
| Caravel::Caravel | Duplicated Code (ciclos repetidos no switch) | Extract Method | 129881 |
| Caravel → Ship (fillVertical, fillHorizontal) | Duplicated Code entre subclasses | Pull Members Up | 129881 |
| Ship (campo positions) | Campo protected acessível às subclasses (falta de encapsulamento) | Encapsulate Fields | 129881 |
| Ship (campo size) | Uso desnecessário de Integer (wrapper) | Type Migration | 129881 |
| Tasks::readClassicPosition | Condição pouco expressiva (regex sem nome dentro do if) | Introduce Variable | 129854 |
| Tasks::readShip | Variável temporária desnecessária (c) | Inline Variable | 129854 |
| Tasks::exportPdf | Magic String ("jogadas.pdf" repetido) | Introduce Constant | 129854 |
| Tasks::menu, Tasks::exportPdf | Nome pouco expressivo e misto PT/EN (historico) | Rename | 129854 |
| Tasks::menu | Duplicated Code (início de jogo repetido em gerafrota e lefrota) | Extract Method | 129854 |
| Tasks::exportPdf | Dependência escondida de um ficheiro fixo | Introduce Parameter | 129854 |
