package battleship;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Mede o tempo gasto em cada jogada (rajada) e o tempo total do jogo,
 * usando o StopWatch da biblioteca Apache Commons Lang.
 */
public class GameTimer {

    private final StopWatch stopWatch = new StopWatch();
    private final List<Long> moveTimes = new ArrayList<>();

    /**
     * Começa a contar um novo jogo: apaga os tempos anteriores e arranca o relógio.
     */
    public void startGame() {
        moveTimes.clear();
        restart();
    }

    /**
     * Termina a jogada atual, mostra o seu tempo e arranca o relógio para a seguinte.
     */
    public void endMove() {
        if (!stopWatch.isStarted()) {
            return;
        }
        stopWatch.stop();
        long millis = stopWatch.getTime();
        moveTimes.add(millis);
        System.out.println("Tempo da jogada " + moveTimes.size() + ": " + format(millis));
        restart();
    }

    /**
     * Mostra o tempo total gasto nas jogadas (só se houve alguma jogada).
     */
    public void printTotal() {
        if (moveTimes.isEmpty()) {
            return;
        }
        long total = 0;
        for (long t : moveTimes) {
            total += t;
        }
        System.out.println("Tempo total do jogo: " + format(total)
                + " em " + moveTimes.size() + " jogadas");
    }

    private void restart() {
        stopWatch.reset();
        stopWatch.start();
    }

    private static String format(long millis) {
        return String.format("%.1f s", millis / 1000.0);
    }
}
