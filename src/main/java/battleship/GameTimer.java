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

    // Ficha 3 – Long Method provocado de propósito para testar o quality gate (129881)
    public String relatorioLongo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Relatório de tempos\n");
        sb.append("-------------------\n");
        sb.append("Linha 1\n");
        sb.append("Linha 2\n");
        sb.append("Linha 3\n");
        sb.append("Linha 4\n");
        sb.append("Linha 5\n");
        sb.append("Linha 6\n");
        sb.append("Linha 7\n");
        sb.append("Linha 8\n");
        sb.append("Linha 9\n");
        sb.append("Linha 10\n");
        sb.append("Linha 11\n");
        sb.append("Linha 12\n");
        sb.append("Linha 13\n");
        sb.append("Linha 14\n");
        sb.append("Linha 15\n");
        sb.append("Linha 16\n");
        sb.append("Linha 17\n");
        sb.append("Linha 18\n");
        sb.append("Linha 19\n");
        sb.append("Linha 20\n");
        sb.append("Linha 21\n");
        sb.append("Linha 22\n");
        sb.append("Linha 23\n");
        sb.append("Linha 24\n");
        sb.append("Linha 25\n");
        sb.append("Linha 26\n");
        sb.append("Linha 27\n");
        sb.append("Linha 28\n");
        sb.append("Linha 29\n");
        sb.append("Linha 30\n");
        sb.append("Linha 31\n");
        sb.append("Linha 32\n");
        sb.append("Linha 33\n");
        sb.append("Linha 34\n");
        sb.append("Linha 35\n");
        sb.append("Linha 36\n");
        sb.append("Linha 37\n");
        sb.append("Linha 38\n");
        sb.append("Linha 39\n");
        sb.append("Linha 40\n");
        sb.append("Linha 41\n");
        sb.append("Linha 42\n");
        sb.append("Linha 43\n");
        sb.append("Linha 44\n");
        sb.append("Linha 45\n");
        return sb.toString();
    }
}
