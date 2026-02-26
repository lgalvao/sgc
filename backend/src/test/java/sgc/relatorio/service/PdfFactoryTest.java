package sgc.relatorio.service;

import org.junit.jupiter.api.*;
import sgc.relatorio.*;

import static org.junit.jupiter.api.Assertions.*;

class PdfFactoryTest {

    private final PdfFactory pdfFactory = new PdfFactory();

    @Test
    @DisplayName("Deve criar documento")
    void deveCriarDocumento() {
        var document = pdfFactory.createDocument();
        assertNotNull(document);
    }
}
