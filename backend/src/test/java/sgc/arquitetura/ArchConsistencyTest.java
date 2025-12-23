package sgc.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "sgc", importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class ArchConsistencyTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
            noClasses()
                    .that()
                    .haveNameMatching(".*Controller")
                    .should()
                    .accessClassesThat()
                    .haveNameMatching(".*Repo");

    @ArchTest
    static final ArchRule mapa_controller_should_only_access_mapa_service =
            classes()
                    .that()
                    .haveNameMatching("sgc.mapa.MapaController")
                    .should()
                    .onlyAccessClassesThat()
                    .haveNameMatching("sgc.mapa.service.MapaService")
                    .orShould()
                    .accessClassesThat()
                    .resideOutsideOfPackage("sgc.mapa..");

    @ArchTest
    static final ArchRule processo_controller_should_only_access_processo_service =
            classes()
                    .that()
                    .haveNameMatching("sgc.processo.ProcessoController")
                    .should()
                    .onlyAccessClassesThat()
                    .haveNameMatching("sgc.processo.internal.service.ProcessoService")
                    .orShould()
                    .accessClassesThat()
                    .resideOutsideOfPackage("sgc.processo..");

    @ArchTest
    static final ArchRule comum_package_should_not_contain_business_logic =
            noClasses()
                    .that()
                    .resideInAPackage("sgc.comum..")
                    .should()
                    .haveNameMatching(".*Controller")
                    .orShould()
                    .haveNameMatching(".*Service");
}
