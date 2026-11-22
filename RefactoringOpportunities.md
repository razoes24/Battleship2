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
| Tasks::readClassicPosition | Condição pouco expressiva (regex sem nome dentro do if) | Introduce Variable | 129854 |
| Tasks::readShip | Variável temporária desnecessária (c) | Inline Variable | 129854 |
| Tasks::exportPdf | Magic String ("jogadas.pdf" repetido) | Introduce Constant | 129854 |
| Tasks::menu, Tasks::exportPdf | Nome pouco expressivo e misto PT/EN (historico) | Rename | 129854 |
| Tasks::menu | Duplicated Code (início de jogo repetido em gerafrota e lefrota) | Extract Method | 129854 |
| Tasks::exportPdf | Dependência escondida de um ficheiro fixo | Introduce Parameter | 129854 |
