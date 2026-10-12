package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.List;

public class PdfExporter {

    public static void export(List<String> jogadas, String ficheiro) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            cs.setLeading(14f);
            cs.newLineAtOffset(50, 750);
            cs.showText("Historico de jogadas");
            cs.newLine();

            float y = 750 - 14;
            int n = 1;
            for (String j : jogadas) {
                // Nova pagina quando chega ao fim da atual
                if (y < 50) {
                    cs.endText();
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    cs.setLeading(14f);
                    cs.newLineAtOffset(50, 750);
                    y = 750;
                }
                // O JSON pode ter quebras de linha e acentos: limpa antes de escrever
                String linha = ("Rajada " + n++ + ": " + j)
                        .replaceAll("[\\r\\n\\t]+", " ")
                        .replaceAll("[^\\x20-\\x7E]", "?");
                // Corta linhas muito longas para caberem na pagina
                if (linha.length() > 110) {
                    linha = linha.substring(0, 110);
                }
                cs.showText(linha);
                cs.newLine();
                y -= 14;
            }
            cs.endText();
            cs.close();

            doc.save(ficheiro);
        }
    }
}