package sgc.relatorio;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
public class PdfFactory {
    public Document createDocument() {
        return new Document(PageSize.A4, 36f, 36f, 54f, 42f);
    }

    public void createWriter(Document document, OutputStream outputStream) throws DocumentException {
        PdfWriter.getInstance(document, outputStream);
    }
}
