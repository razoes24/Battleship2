// 129854 - Guilherme - LEI-129854
// Ficha 3 (2A, passo 9): cheiro provocado de proposito (Shotgun Surgery).
// Esta classe passa a depender de IPosition, que fica com 26 classes dependentes (> limite 25):
// qualquer alteracao a IPosition obriga a mexer em ainda mais classes.
package battleship;

import java.util.ArrayList;
import java.util.List;

public class ShotHistory {
    private final List<IPosition> shots = new ArrayList<>();

    public void register(IPosition position) {
        shots.add(position);
    }

    public int count() {
        return shots.size();
    }
}
