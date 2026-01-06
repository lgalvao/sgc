package sgc.relatorio.service;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
public class PdfFactory {
    public Document createDocument() {
        return new Document();
    }

    public void createWriter(Document document, OutputStream outputStream) throws DocumentException {
        PdfWriter.getInstance(document, outputStream);
    }
}
