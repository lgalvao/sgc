package sgc.relatorio;

import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.*;

import java.io.*;

@Component
public class PdfFactory {
    public Document createDocument() {
        return new Document();
    }

    public void createWriter(Document document, OutputStream outputStream) throws DocumentException {
        PdfWriter.getInstance(document, outputStream);
    }
}
