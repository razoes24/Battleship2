package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda e consulta as jogadas (rajadas) numa base de dados H2 local.
 */
public class JogadasRepository {

    private static final String URL = "jdbc:h2:./data/jogadas";

    private static String jogoAtual = gerarIdJogo();

    static {
        criarTabela();
    }

    private static String gerarIdJogo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    /** Chamar quando começa um jogo novo. */
    public static void novoJogo() {
        jogoAtual = gerarIdJogo();
    }

    private static void criarTabela() {
        String sql = """
                CREATE TABLE IF NOT EXISTS jogadas (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    jogo_id VARCHAR(30) NOT NULL,
                    numero_rajada INT NOT NULL,
                    coordenadas VARCHAR(200),
                    resultado VARCHAR(10000) NOT NULL,
                    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Erro ao preparar a base de dados: " + e.getMessage());
        }
    }

    /** Grava uma rajada. As coordenadas podem ser null. */
    public static void guardar(int numeroRajada, String coordenadas, String resultado) {
        String sql = "INSERT INTO jogadas (jogo_id, numero_rajada, coordenadas, resultado) VALUES (?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, jogoAtual);
            ps.setInt(2, numeroRajada);
            ps.setString(3, coordenadas);
            ps.setString(4, resultado);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao guardar a jogada: " + e.getMessage());
        }
    }

    /** Devolve todas as jogadas guardadas, em texto. */
    public static List<String> listar() {
        List<String> linhas = new ArrayList<>();
        String sql = "SELECT jogo_id, numero_rajada, coordenadas, resultado FROM jogadas ORDER BY id";
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String coords = rs.getString("coordenadas");
                linhas.add("Jogo " + rs.getString("jogo_id")
                        + " | Rajada " + rs.getInt("numero_rajada")
                        + (coords != null ? " | " + coords : "")
                        + " | " + rs.getString("resultado").replaceAll("\\s+", " "));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar as jogadas: " + e.getMessage());
        }
        return linhas;
    }
}