package sgc.arquitetura;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;

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
                    .haveNameMatching("sgc.processo.service.ProcessoService")
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

    @ArchTest
    static final ArchRule services_should_not_access_other_modules_repositories =
            FreezingArchRule.freeze(classes()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .should(new ArchCondition<JavaClass>("only access repositories of their own module") {
                        @Override
                        public void check(JavaClass item, ConditionEvents events) {
                            String itemPackage = item.getPackageName();
                            String itemModule = extractModule(itemPackage);

                            if (itemModule == null) return;

                            for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                                JavaClass targetClass = dependency.getTargetClass();
                                if (targetClass.getSimpleName().endsWith("Repo")) {
                                    String dependencyPackage = targetClass.getPackageName();
                                    String dependencyModule = extractModule(dependencyPackage);

                                    // Check if dependency is in a recognized module and if it matches the item's module
                                    if (dependencyModule != null && !dependencyModule.equals(itemModule)) {
                                        String message = String.format("Service %s (module %s) accesses Repository %s (module %s)",
                                                item.getName(), itemModule, targetClass.getName(), dependencyModule);
                                        events.add(SimpleConditionEvent.violated(dependency, message));
                                    }
                                }
                            }
                        }

                        private String extractModule(String packageName) {
                            if (!packageName.startsWith("sgc.")) return null;
                            String[] parts = packageName.split("\\.");
                            if (parts.length < 2) return null;
                            return parts[1];
                        }
                    }));
}
