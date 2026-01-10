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

@AnalyzeClasses(packages = "sgc", importOptions = { ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class })
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class ArchConsistencyTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories = noClasses()
            .that()
            .haveNameMatching(".*Controller")
            .should()
            .accessClassesThat()
            .haveNameMatching(".*Repo");

    @ArchTest
    static final ArchRule mapa_controller_should_only_access_mapa_service = classes()
            .that()
            .haveNameMatching("sgc.mapa.MapaController")
            .should()
            .onlyAccessClassesThat()
            .haveNameMatching("sgc.mapa.service.MapaFacade")
            .orShould()
            .accessClassesThat()
            .resideOutsideOfPackage("sgc.mapa..");

    @ArchTest
    static final ArchRule processo_controller_should_only_access_processo_service = classes()
            .that()
            .haveNameMatching("sgc.processo.ProcessoController")
            .should()
            .onlyAccessClassesThat()
            .haveNameMatching("sgc.processo.service.ProcessoService")
            .orShould()
            .accessClassesThat()
            .resideOutsideOfPackage("sgc.processo..");

    @ArchTest
    static final ArchRule comum_package_should_not_contain_business_logic = noClasses()
            .that()
            .resideInAPackage("sgc.comum..")
            .should()
            .haveNameMatching(".*Controller")
            .orShould()
            .haveNameMatching(".*Service");

    @ArchTest
    static final ArchRule services_should_not_access_other_modules_repositories = classes()
            .that()
            .haveSimpleNameEndingWith("Service")
            .should(new ArchCondition<JavaClass>("only access repositories of their own module") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    String itemPackage = item.getPackageName();
                    String itemModule = extractModule(itemPackage);

                    if (itemModule == null)
                        return;

                    for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                        JavaClass targetClass = dependency.getTargetClass();
                        if (targetClass.getSimpleName().endsWith("Repo")) {
                            String dependencyPackage = targetClass.getPackageName();
                            String dependencyModule = extractModule(dependencyPackage);

                            // Check if dependency is in a recognized module and if it matches the item's
                            // module
                            if (dependencyModule != null && !dependencyModule.equals(itemModule)) {
                                // EXCEÇÃO: O módulo seguranca (LoginService) pode acessar organizacao
                                // (UsuarioRepo, etc)

                                // TODO não gosto dessa exceção!
                                if ("seguranca".equals(itemModule) && "organizacao".equals(dependencyModule)) {
                                    continue;
                                }

                                String message = String.format(
                                        "Service %s (module %s) accesses Repository %s (module %s)",
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
    static final ArchRule controllers_e_services_devem_estar_em_pacotes_null_marked = classes()
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

    @ArchTest
    static final ArchRule controllers_should_only_use_facades_not_specialized_services = noClasses()
            .that()
            .haveNameMatching(".*Controller")
            .should()
            .dependOnClassesThat()
            .haveNameMatching(".*MapaVisualizacaoService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*ImpactoMapaService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*MapaSalvamentoService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*AtividadeService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*CompetenciaService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*ConhecimentoService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*CopiaMapaService")
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(".*Detector.*Service")
            .because("Controllers should use Facades (e.g., MapaFacade, AtividadeFacade) instead of specialized services");

    /**
     * Garante que todas as classes Facade tenham o sufixo "Facade" no nome.
     * Isso melhora a consistência e clareza arquitetural.
     */
    @ArchTest
    static final ArchRule facades_should_have_facade_suffix = classes()
            .that()
            .resideInAPackage("..service..")
            .and()
            .areAnnotatedWith(org.springframework.stereotype.Service.class)
            .and()
            .haveSimpleNameContaining("Facade")
            .should()
            .haveSimpleNameEndingWith("Facade")
            .because("Facade classes should have 'Facade' as suffix for consistency");

    /**
     * Garante que DTOs não sejam entidades JPA.
     * Entidades JPA nunca devem ser expostas diretamente nas APIs.
     */
    @ArchTest
    static final ArchRule dtos_should_not_be_jpa_entities = noClasses()
            .that()
            .haveSimpleNameEndingWith("Dto")
            .should()
            .beAnnotatedWith(jakarta.persistence.Entity.class)
            .because("DTOs should never be JPA entities - use separate entity classes");

    /**
     * Garante que entidades JPA não sejam retornadas diretamente pelos controllers.
     * Controllers devem sempre usar DTOs.
     */
    @ArchTest
    static final ArchRule controllers_should_not_return_jpa_entities = noClasses()
            .that()
            .areAnnotatedWith(jakarta.persistence.Entity.class)
            .should()
            .beInterfaces()
            .orShould()
            .beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .because("JPA entities should never be exposed directly - use DTOs instead");

    /**
     * Garante que Services não tenham lógica de controle de acesso direto.
     * Toda verificação de acesso deve ser feita via AccessControlService.
     */
    @ArchTest
    static final ArchRule services_should_not_throw_access_denied_directly = noClasses()
            .that()
            .haveSimpleNameEndingWith("Service")
            .and()
            .doNotHaveSimpleName("AccessControlService")
            .and()
            .doNotHaveSimpleName("AccessAuditService")
            .and()
            .resideOutsideOfPackage("sgc.seguranca.acesso..")
            .should(new ArchCondition<JavaClass>("throw ErroAccessoNegado directly - use AccessControlService instead") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    // Verificar se o service cria instâncias de ErroAccessoNegado
                    item.getCodeUnits().forEach(codeUnit -> {
                        codeUnit.getCallsFromSelf().stream()
                            .filter(call -> call.getTargetOwner().getSimpleName().equals("ErroAccessoNegado"))
                            .filter(call -> call.getName().equals("<init>"))
                            .forEach(call -> {
                                String message = String.format(
                                        "Service %s throws ErroAccessoNegado directly in method %s. " +
                                        "Use AccessControlService.verificarPermissao() instead.",
                                        item.getSimpleName(), codeUnit.getName());
                                events.add(SimpleConditionEvent.violated(call, message));
                            });
                    });
                }
            })
            .because("Access control should be centralized in AccessControlService");

    /**
     * Garante nomenclatura consistente de Controllers.
     */
    @ArchTest
    static final ArchRule controllers_should_have_controller_suffix = classes()
            .that()
            .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should()
            .haveSimpleNameEndingWith("Controller")
            .because("Controllers should have 'Controller' suffix for consistency");

    /**
     * Garante nomenclatura consistente de Repositories.
     */
    @ArchTest
    static final ArchRule repositories_should_have_repo_suffix = classes()
            .that()
            .areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
            .should()
            .haveSimpleNameEndingWith("Repo")
            .because("Repositories should have 'Repo' suffix for consistency");

    /**
     * Garante que eventos de domínio sigam o padrão de nomenclatura.
     */
    @ArchTest
    static final ArchRule domain_events_should_start_with_evento = classes()
            .that()
            .resideInAPackage("..eventos..")
            .or()
            .resideInAPackage("..evento..")
            .should()
            .haveSimpleNameStartingWith("Evento")
            .because("Domain events should start with 'Evento' prefix for consistency");
}
