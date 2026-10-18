package battleship;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The type Tasks.
 */
public class Tasks {
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The constant GOODBYE_MESSAGE.
     */
    private static final String GOODBYE_MESSAGE = "Bons ventos!";

    /**
     * Strings to be used by the user
     */
    private static final String AJUDA = "ajuda";
    private static final String GERAFROTA = "gerafrota";
    private static final String LEFROTA = "lefrota";
    private static final String DESISTIR = "desisto";
    private static final String RAJADA = "rajada";
    private static final String TIROS = "tiros";
    private static final String MAPA = "mapa";
    private static final String STATUS = "estado";
    private static final String SIMULA = "simula";
    private static final String EXPORTAPDF = "exportapdf";
    private static final String SCOREBOARD = "scoreboard";
    private static final String HISTORICOBD = "historicobd";

    /**
     * This task also tests the fighting element of a round of three shots
     */
    public static void menu() {

        IFleet myFleet = null;
        IGame game = null;

        // Lista para guardar o histórico das jogadas
        List<String> historico = new ArrayList<>();

        // Relógio que mede o tempo gasto em cada jogada
        GameTimer timer = new GameTimer();

        menuHelp();

        System.out.print("> ");
        Scanner in = new Scanner(System.in);
        String command = in.next();
        while (!command.equals(DESISTIR)) {

            switch (command) {
                case GERAFROTA:
                    myFleet = Fleet.createRandom();
                    game = new Game(myFleet);
                    JogadasRepository.novoJogo();
                    timer.startGame();
                    game.printMyBoard(false, true);
                    break;
                case LEFROTA:
                    myFleet = buildFleet(in);
                    game = new Game(myFleet);
                    JogadasRepository.novoJogo();
                    timer.startGame();
                    game.printMyBoard(false, true);
                    break;
                case STATUS:
                    if (myFleet != null)
                        myFleet.printStatus();
                    break;
                case MAPA:
                    if (myFleet != null)
                        game.printMyBoard(false, true);
                    break;
                case RAJADA:
                    if (game != null) {
                        // Guarda o JSON da jogada retornado pela readEnemyFire
                        String jsonJogada = game.readEnemyFire(in);
                        historico.add(jsonJogada);
                        timer.endMove();

                        myFleet.printStatus();
                        game.printMyBoard(true, false);

                        if (game.getRemainingShips() == 0) {
                            // Guarda o resultado do jogo no scoreboard (JSON, com Gson)
                            Scoreboard.add(game.getAlienMoves().size());
                            // O programa termina a seguir, por isso exporta o PDF automaticamente
                            exportPdf(historico);
                            timer.printTotal();
                            game.over();
                            System.exit(0);
                        }
                    }
                    break;
                case SIMULA:
                    if (game != null) {
                        while (game.getRemainingShips() > 0) {
                            // Guarda o JSON da jogada retornado pela randomEnemyFire
                            String jsonJogada = game.randomEnemyFire();
                            historico.add(jsonJogada);
                            JogadasRepository.guardar(historico.size(), null, jsonJogada);

                            myFleet.printStatus();
                            game.printMyBoard(true, false);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        if (game.getRemainingShips() == 0) {
                            // Guarda o resultado do jogo no scoreboard (JSON, com Gson)
                            Scoreboard.add(game.getAlienMoves().size());
                            // O programa termina a seguir, por isso exporta o PDF automaticamente
                            exportPdf(historico);
                            game.over();
                            System.exit(0);
                        }
                    }
                    break;
                case TIROS:
                    if (game != null)
                        game.printMyBoard(true, true);
                    break;
                case SCOREBOARD:
                    Scoreboard.show();
                    break;
                case HISTORICOBD:
                    JogadasRepository.listar().forEach(System.out::println);
                    break;
                case AJUDA:
                    menuHelp();
                    break;
                case EXPORTAPDF:
                    exportPdf(historico);
                    break;
                default:
                    System.out.println("Que comando é esse??? Repete ...");
            }
            System.out.print("> ");
            command = in.next();
        }
        timer.printTotal();
        System.out.println(GOODBYE_MESSAGE);
    }

    /**
     * Exports the move history to a PDF file.
     *
     * @param historico the list of moves (JSON strings)
     */
    private static void exportPdf(List<String> historico) {
        try {
            PdfExporter.export(historico, "jogadas.pdf");
            System.out.println("PDF gerado com sucesso: jogadas.pdf");
        } catch (Exception e) {
            System.out.println("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    /**
     * This function provides help information about the menu commands.
     */
    public static void menuHelp() {
        System.out.println("======================= AJUDA DO MENU =========================");
        System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
        System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
        System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
        System.out.println("- " + STATUS + ": Mostra o status atual da frota.)");
        System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
        System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
        System.out.println("- " + SIMULA + ": Simula um jogo completo.");
        System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
        System.out.println("- " + SCOREBOARD + ": Mostra o scoreboard dos jogos anteriores.");
        System.out.println("- " + HISTORICOBD + ": Lista as jogadas guardadas na base de dados.");
        System.out.println("- " + EXPORTAPDF + ": Exporta o histórico de jogadas para um ficheiro PDF.");
        System.out.println("- " + DESISTIR + ": Encerra o jogo.");
        System.out.println("===============================================================");
    }

    /**
     * This operation allows the build up of a fleet, given user data
     *
     * @param in The scanner to read from
     * @return The fleet that has been built
     */
    public static Fleet buildFleet(Scanner in) {

        assert in != null;

        Fleet fleet = new Fleet();
        int i = 0;
        while (i < Fleet.FLEET_SIZE) {
            IShip s = readShip(in);
            if (s != null) {
                boolean success = fleet.addShip(s);
                if (success)
                    i++;
                else
                    LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
            } else {
                LOGGER.info("Navio desconhecido!");
            }
        }
        LOGGER.info("{} navios adicionados com sucesso!", i);
        return fleet;
    }

    /**
     * This operation reads data about a ship, build it and returns it
     *
     * @param in The scanner to read from
     * @return The created ship based on the data that has been read
     */
    public static Ship readShip(Scanner in) {

        assert in != null;

        String shipKind = in.next();
        Position pos = readPosition(in);
        char c = in.next().charAt(0);
        Compass bearing = Compass.charToCompass(c);
        return Ship.buildShip(shipKind, bearing, pos);
    }

    /**
     * This operation allows reading a position in the map
     *
     * @param in The scanner to read from
     * @return The position that has been read
     */
    public static Position readPosition(Scanner in) {

        assert in != null;

        int row = in.nextInt();
        int column = in.nextInt();
        return new Position(row, column);
    }

    /**
     * This operation allows reading a position in the map
     *
     * @param in The scanner to read from
     * @return The classic position that has been read
     */
    public static IPosition readClassicPosition(Scanner in) {
        if (!in.hasNext()) {
            throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
        }

        String part1 = in.next();
        String part2 = null;

        if (in.hasNextInt()) {
            part2 = in.next();
        }

        String input = (part2 != null) ? part1 + part2 : part1;
        input = input.toUpperCase();

        if (input.matches("[A-Z]\\d+")) {
            char column = input.charAt(0);
            int row = Integer.parseInt(input.substring(1));
            return new Position(column, row);
        } else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
            char column = part1.charAt(0);
            int row = Integer.parseInt(part2);
            return new Position(column, row);
        } else {
            throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
        }
    }
}