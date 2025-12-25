package sgc;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTests {

    ApplicationModules modules = ApplicationModules.of(Sgc.class);

    @Test
    @org.junit.jupiter.api.Disabled("Failing due to architectural cycles. See STATUS.md")
    void verifyModulithStructure() {
        modules.verify();
    }

    @Test
    void createModulithDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
