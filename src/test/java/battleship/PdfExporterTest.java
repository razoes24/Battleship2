package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class PdfExporter.
 * Author: Bruno Frasco - 129856
 * Date: 2026-11-19 16:47
 *
 * Cyclomatic Complexity:
 * PdfExporter(): 1
 * export(): 4
 */
class PdfExporterTest {

    private Path tempPdf;

    @BeforeEach
    void setUp() throws IOException {
        tempPdf = Files.createTempFile("test_jogadas_", ".pdf");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempPdf);
        tempPdf = null;
    }

    @Test
    @DisplayName("PdfExporter(): Deve instanciar a classe utilitária corretamente")
    void PdfExporter() {
        PdfExporter exporter = new PdfExporter();
        assertNotNull(exporter, "Erro: esperava-se uma instância não nula de PdfExporter, mas obteve-se null");
    }

    @Test
    @DisplayName("export1(): Deve criar um PDF válido apenas com o título quando a lista de jogadas está vazia")
    void export1() throws IOException {
        PdfExporter.export(List.of(), tempPdf.toString());

        assertAll("Validação do PDF gerado com lista vazia",
                () -> assertTrue(Files.exists(tempPdf),
                        "Erro: esperava-se que o ficheiro PDF existisse, mas não foi encontrado"),
                () -> assertTrue(Files.size(tempPdf) > 0,
                        "Erro: esperava-se que o ficheiro PDF tivesse conteúdo (> 0 bytes), mas está vazio")
        );
    }

    @Test
    @DisplayName("export2(): Deve exportar jogadas curtas e limpar quebras de linha e acentos")
    void export2() throws IOException {
        List<String> jogadas = List.of("Tiro em A1\nÁgua", "Tiro em B2\tAcertou");
        PdfExporter.export(jogadas, tempPdf.toString());

        assertAll("Validação do PDF com jogadas normais",
                () -> assertTrue(Files.exists(tempPdf),
                        "Erro: esperava-se que o ficheiro PDF existisse, mas não foi encontrado"),
                () -> assertTrue(Files.size(tempPdf) > 0,
                        "Erro: esperava-se que o ficheiro PDF tivesse conteúdo (> 0 bytes), mas está vazio")
        );
    }

    @Test
    @DisplayName("export3(): Deve cortar linhas com mais de 110 caracteres")
    void export3() throws IOException {
        List<String> jogadas = List.of("A".repeat(120));
        PdfExporter.export(jogadas, tempPdf.toString());

        assertAll("Validação do corte de linhas longas (> 110 caracteres)",
                () -> assertTrue(Files.exists(tempPdf),
                        "Erro: esperava-se que o ficheiro PDF existisse, mas não foi encontrado"),
                () -> assertTrue(Files.size(tempPdf) > 0,
                        "Erro: esperava-se que o ficheiro PDF tivesse conteúdo (> 0 bytes), mas está vazio")
        );
    }

    @Test
    @DisplayName("export4(): Deve criar uma nova página quando y < 50 (51 ou mais jogadas)")
    void export4() throws IOException {
        List<String> jogadas = Collections.nCopies(51, "Tiro em A1");
        PdfExporter.export(jogadas, tempPdf.toString());

        assertAll("Validação da paginação automática no PDF",
                () -> assertTrue(Files.exists(tempPdf),
                        "Erro: esperava-se que o ficheiro PDF existisse, mas não foi encontrado"),
                () -> assertTrue(Files.size(tempPdf) > 0,
                        "Erro: esperava-se que o ficheiro PDF tivesse conteúdo (> 0 bytes), mas está vazio")
        );
    }

    @Test
    @DisplayName("export5(): Deve lançar IOException quando o caminho do ficheiro de destino é inválido")
    void export5() {
        assertThrows(IOException.class, () -> PdfExporter.export(List.of("Tiro em A1"), "."),
                "Erro: esperava-se IOException ao tentar guardar num caminho de diretoria inválido");
    }
}