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

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.setLeading(16f);
                cs.newLineAtOffset(50, 750);
                cs.showText("Historico de jogadas");
                cs.newLine();
                int n = 1;
                for (String j : jogadas) {
                    // Limpa as quebras de linha (\r e \n) que o JSON cria
                    String textoLimpo = j.replace("\r", " ").replace("\n", " ");

                    cs.showText("Rajada " + n++ + ": " + textoLimpo);
                    cs.newLine();
                }
                cs.endText();
            }
            doc.save(ficheiro);
        }
    }
}