package restserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe BattleshipServer (ponto de entrada do servidor REST).
 *
 * Complexidade ciclomática:
 * - construtor (implícito): 1
 * - main: 1
 *
 * @author 129881
 */
@DisplayName("Testes da classe BattleshipServer")
class BattleshipServerTest {

    @Test
    @DisplayName("A classe está anotada com @SpringBootApplication")
    void hasSpringBootApplicationAnnotation() {
        assertTrue(BattleshipServer.class.isAnnotationPresent(SpringBootApplication.class),
                "Error: BattleshipServer should be annotated with @SpringBootApplication.");
    }

    @Test
    @DisplayName("O construtor cria uma instância")
    void constructor() {
        assertNotNull(new BattleshipServer(), "Error: BattleshipServer instance should not be null.");
    }

    @Test
    @DisplayName("main arranca a aplicação Spring sem erros (sem servidor web, para não ocupar a porta 8080)")
    void mainStartsApplication() {
        // "web-application-type=none" arranca o contexto Spring (cria os @Component do restserver)
        // mas não abre o Tomcat, por isso o teste é rápido e não entra em conflito com a porta 8080.
        assertDoesNotThrow(() -> BattleshipServer.main(new String[]{
                        "--spring.main.web-application-type=none"}),
                "Error: main should start the Spring application without throwing exceptions.");
    }
}