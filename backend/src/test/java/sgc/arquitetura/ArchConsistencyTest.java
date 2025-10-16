package sgc.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "sgc", importOptions = ImportOption.DoNotIncludeTests.class)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class ArchConsistencyTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
            noClasses().that().haveNameMatching(".*Controle")
                    .should().accessClassesThat().haveNameMatching(".*Repo");
}