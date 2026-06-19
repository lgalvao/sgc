package sgc.relatorio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.relatorio.PdfFactory;

import static org.assertj.core.api.Assertions.assertThat;

class PdfFactoryTest {

    private final PdfFactory pdfFactory = new PdfFactory();

    @Test
    @DisplayName("Deve criar documento")
    void deveCriarDocumento() {
        var document = pdfFactory.createDocument();
        assertThat(document).isNotNull();
    }
}
