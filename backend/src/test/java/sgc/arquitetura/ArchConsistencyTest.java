package sgc.arquitetura;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.jspecify.annotations.NullMarked;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "sgc", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
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
            classes()
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
                            int firstDot = packageName.indexOf('.');
                            if (firstDot == -1) return null;
                            int secondDot = packageName.indexOf('.', firstDot + 1);
                            if (secondDot == -1) return packageName.substring(firstDot + 1);
                            return packageName.substring(firstDot + 1, secondDot);
                        }
                    });

    @ArchTest
    static final ArchRule controllers_e_services_devem_estar_em_pacotes_null_marked =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .or().haveSimpleNameEndingWith("Service")
                    .should(new ArchCondition<JavaClass>("residir em pacote anotado com @NullMarked") {
                        @Override
                        public void check(JavaClass item, ConditionEvents events) {
                            JavaPackage javaPackage = item.getPackage();
                            boolean pacoteNullMarked = javaPackage.isAnnotatedWith(NullMarked.class);
                            
                            if (!pacoteNullMarked) {
                                String mensagem = String.format(
                                        "%s não está em um pacote @NullMarked (pacote: %s)",
                                        item.getSimpleName(), javaPackage.getName());
                                events.add(SimpleConditionEvent.violated(item, mensagem));
                            }
                        }
                    })
                    .because("Controllers e Services devem estar em pacotes @NullMarked para garantir null-safety");
}

