package sgc.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "sgc", importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class ArchConsistencyTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
            noClasses().that().haveNameMatching(".*Controle")
                    .should().accessClassesThat().haveNameMatching(".*Repo");

    @ArchTest
    static final ArchRule mapa_controller_should_only_access_mapa_service =
        classes().that().haveNameMatching("sgc.mapa.MapaControle")
                .should().onlyAccessClassesThat().haveNameMatching("sgc.mapa.MapaService")
                .orShould().accessClassesThat().resideOutsideOfPackage("sgc.mapa..");

    @ArchTest
    static final ArchRule processo_controller_should_only_access_processo_service =
        classes().that().haveNameMatching("sgc.processo.ProcessoControle")
                .should().onlyAccessClassesThat().haveNameMatching("sgc.processo.ProcessoService")
                .orShould().accessClassesThat().resideOutsideOfPackage("sgc.processo..");

    @ArchTest
    static final ArchRule comum_package_should_not_contain_business_logic =
            noClasses().that().resideInAPackage("sgc.comum..")
                    .should().haveNameMatching(".*Controle")
                    .orShould().haveNameMatching(".*Service");
}