package sgc.integracao;

import org.junit.jupiter.api.*;
import org.junit.platform.suite.api.*;

@Suite
@Tag("integration")
@DisplayName("Suíte paralela de regras de negócio extraídas")
@SelectClasses({
        CDU01IntegrationTest.class,
        CDU02IntegrationTest.class,
        CDU03IntegrationTest.class,
        CDU04IntegrationTest.class,
        CDU05IntegrationTest.class,
        CDU06IntegrationTest.class,
        CDU07IntegrationTest.class,
        CDU08IntegrationTest.class,
        CDU09IntegrationTest.class,
        CDU10IntegrationTest.class,
        CDU11IntegrationTest.class,
        CDU12IntegrationTest.class,
        CDU13IntegrationTest.class,
        CDU14IntegrationTest.class,
        CDU15IntegrationTest.class,
        CDU16IntegrationTest.class,
        CDU17IntegrationTest.class,
        CDU18IntegrationTest.class,
        CDU19IntegrationTest.class,
        CDU20IntegrationTest.class,
        CDU21IntegrationTest.class,
        CDU22IntegrationTest.class,
        CDU23IntegrationTest.class,
        CDU24IntegrationTest.class,
        CDU25IntegrationTest.class,
        CDU26IntegrationTest.class,
        CDU27IntegrationTest.class,
        CDU28IntegrationTest.class,
        CDU29IntegrationTest.class,
        CDU30IntegrationTest.class,
        CDU31IntegrationTest.class,
        CDU32IntegrationTest.class,
        CDU33IntegrationTest.class,
        CDU34IntegrationTest.class,
        CDU35IntegrationTest.class,
        CDU36IntegrationTest.class
})
class RegrasNegocioExtraidasSuiteTest {
}
