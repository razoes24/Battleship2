// 129854 - Guilherme - LEI-129854
// Ficha 3 (2A, passo 9): cheiro Shotgun Surgery provocado e depois corrigido.
// Correcao: ShotHistory deixa de depender de IPosition (guarda as coordenadas como texto),
// pelo que IPosition volta a ter 25 classes dependentes (<= limite 25).
package battleship;

import java.util.ArrayList;
import java.util.List;

public class ShotHistory {
    private final List<String> shots = new ArrayList<>();

    public void register(String coordinate) {
        shots.add(coordinate);
    }

    public int count() {
        return shots.size();
    }
}
