package sgc.arquitetura;

import com.fasterxml.jackson.annotation.JsonView;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@Tag("integration")
@AnalyzeClasses(packages = "sgc", importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class}
)
public class ArchConsistencyTest {
    @ArchTest
    static final ArchRule controllers_should_not_access_repositories = noClasses()
            .that()
            .haveNameMatching(".*Controller")
            .should()
            .accessClassesThat()
            .haveNameMatching(".*Repo");

    @ArchTest
    static final ArchRule mapa_controllers_should_only_access_mapa_facade = classes()
            .that()
            .resideInAPackage("sgc.mapa")
            .should()
            .onlyAccessClassesThat()
            .haveNameMatching("MapaFacade")
            .orShould()
            .accessClassesThat()
            .resideOutsideOfPackage("sgc.mapa..");

    @ArchTest
    static final ArchRule processo_controllers_should_only_access_processo_service = classes()
            .that()
            .resideInAPackage("sgc.processo")
            .should()
            .onlyAccessClassesThat()
            .haveNameMatching("ProcessoService")
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
            .should(new ArchCondition<>("only access repositories of their own module") {
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

                            // Check if dependency is in a recognized module and if it matches the item's
                            // module
                            if (dependencyModule != null && !dependencyModule.equals(itemModule) && !dependencyModule.equals("comum")) {
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
            .should(new ArchCondition<>("residir em pacote anotado com @NullMarked") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    JavaPackage javaPackage = item.getPackage();
                    boolean pacoteNullMarked = javaPackage.isAnnotatedWith(NullMarked.class);

                    if (!pacoteNullMarked) {
                        String mensagem = String.format("%s não está em um pacote @NullMarked (pacote: %s)",
                                item.getSimpleName(), javaPackage.getName());
                        events.add(SimpleConditionEvent.violated(item, mensagem));
                    }
                }
            })
            .because("Controllers e Services devem estar em pacotes @NullMarked para garantir null-safety");

    /**
     * Garante que Controllers usem apenas Facades, nunca services especializados.
     * Isso força o padrão Facade (ADR-001) e garante encapsulamento adequado.
     *
     * <p><b>Implementado como parte da Fase 2 da proposta de arquitetura (ADR-006).</b>
     *
     * <p>Services especializados (que não são Facades) não devem ser acessados diretamente
     * por Controllers. Toda interação deve passar pela Facade apropriada.
     *
     * <p><b>Nota:</b> Esta regra substitui verificações anteriores específicas por serviço,
     * criando uma regra geral que se aplica a TODOS os services não-Facade.
     *
     * @see <a href="/proposta-arquitetura.md">Proposta de Arquitetura - Fase 2</a>
     * @see <a href="/docs/adr/ADR-001-facade-pattern.md">ADR-001: Facade Pattern</a>
     * @see <a href="/docs/adr/ADR-006-domain-aggregates-organization.md">ADR-006</a>
     */
    @ArchTest
    static final ArchRule controllers_should_only_use_facades_not_specialized_services = classes()
            .that()
            .haveNameMatching(".*Controller")
            .should(new ArchCondition<>("only depend on Facade services, not specialized services") {
                @Override
                public void check(JavaClass controller, ConditionEvents events) {
                    for (Dependency dependency : controller.getDirectDependenciesFromSelf()) {
                        JavaClass targetClass = dependency.getTargetClass();

                        // Verifica se é um @Service
                        boolean isService = targetClass.isAnnotatedWith(Service.class);

                        // Verifica se NÃO é um Facade
                        boolean isNotFacade = !targetClass.getSimpleName().endsWith("Facade");

                        if (isService && isNotFacade) {
                            String message = String.format(
                                    "Controller %s depends on specialized service %s. " +
                                            "Controllers should only use Facades (ADR-001, ADR-006 Phase 2)",
                                    controller.getSimpleName(), targetClass.getSimpleName());
                            events.add(SimpleConditionEvent.violated(dependency, message));
                        }
                    }
                }
            })
            .because("Controllers should only use Facades (ADR-001, ADR-006 Phase 2) - specialized services must be accessed through Facades");

    /**
     * Garante consistência no módulo consolidado de acompanhamento.
     * Controllers de análise/alerta/painel devem depender de AcompanhamentoFacade.
     */
    @ArchTest
    static final ArchRule acompanhamento_controllers_should_depend_on_acompanhamento_facade = classes()
            .that()
            .haveNameMatching(".*Controller")
            .and()
            .resideInAnyPackage("sgc.analise..", "sgc.alerta..", "sgc.painel..")
            .should(new ArchCondition<>("depend on AcompanhamentoFacade") {
                @Override
                public void check(JavaClass controller, ConditionEvents events) {
                    boolean dependeDaFacade = controller.getDirectDependenciesFromSelf().stream()
                            .map(Dependency::getTargetClass)
                            .anyMatch(target -> target.getSimpleName().equals("AcompanhamentoFacade"));
                    if (!dependeDaFacade) {
                        String mensagem = String.format(
                                "Controller %s deve depender de AcompanhamentoFacade no módulo consolidado de acompanhamento",
                                controller.getSimpleName());
                        events.add(SimpleConditionEvent.violated(controller, mensagem));
                    }
                }
            })
            .because("Controllers de analise/alerta/painel devem manter fronteira única via AcompanhamentoFacade");

    /**
     * Garante que todas as classes Facade tenham o sufixo "Facade" no nome.
     * Isso melhora a consistência e clareza arquitetural.
     */
    @ArchTest
    static final ArchRule facades_should_have_facade_suffix = classes()
            .that()
            .resideInAPackage("..service..")
            .and()
            .areAnnotatedWith(Service.class)
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
            .beAnnotatedWith(Entity.class)
            .because("DTOs should never be JPA entities - use separate entity classes");

    /**
     * Garante que entidades JPA só sejam retornadas por controllers quando houver
     * @JsonView explícito no método.
     */
    @ArchTest
    static final ArchRule controllers_should_not_return_jpa_entities_without_json_view = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .should(new ArchCondition<>("not return JPA entities without @JsonView") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    JavaClass retorno = method.getRawReturnType();
                    boolean retornaEntidade = retorno.isAnnotatedWith(Entity.class);
                    boolean possuiJsonView = method.isAnnotatedWith(JsonView.class);
                    if (retornaEntidade && !possuiJsonView) {
                        String mensagem = String.format(
                                "Método %s.%s retorna entidade JPA (%s) sem @JsonView",
                                method.getOwner().getSimpleName(), method.getName(), retorno.getSimpleName());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Entidades JPA só podem ser expostas em controllers com @JsonView explícito");

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
            .should(new ArchCondition<>("throw ErroAcessoNegado directly - use AccessControlService instead") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    // Verificar se o service cria instâncias de ErroAcessoNegado
                    item.getCodeUnits().forEach(codeUnit -> codeUnit.getCallsFromSelf().stream()
                            .filter(call -> call.getTargetOwner().getSimpleName().equals("ErroAcessoNegado"))
                            .filter(call -> call.getName().equals("<init>"))
                            .forEach(call -> {
                                String message = String.format(
                                        "Service %s throws ErroAcessoNegado directly in method %s. " +
                                                "Use AccessControlService.verificarPermissao() instead.",
                                        item.getSimpleName(), codeUnit.getName());
                                events.add(SimpleConditionEvent.violated(call, message));
                            }));
                }
            })
            .because("Access control should be centralized in AccessControlService");

    /**
     * Garante nomenclatura consistente de Controllers.
     */
    @ArchTest
    static final ArchRule controllers_should_have_controller_suffix = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .haveSimpleNameEndingWith("Controller")
            .because("Controllers should have 'Controller' suffix for consistency");

    /**
     * Garante nomenclatura consistente de Repositories.
     */
    @ArchTest
    static final ArchRule repositories_should_have_repo_suffix = classes()
            .that()
            .areAssignableTo(JpaRepository.class)
            .should()
            .haveSimpleNameEndingWith("Repo")
            .because("Repositories should have 'Repo' suffix for consistency");

    /**
     * Garante que eventos de domínio sigam o padrão de nomenclatura.
     * Exceções: Enums (como TipoTransicao) e Listeners (como EventoProcessoListener).
     */
    @ArchTest
    static final ArchRule domain_events_should_start_with_evento = classes()
            .that()
            .resideInAPackage("..eventos..")
            .or()
            .resideInAPackage("..evento..")
            .and()
            .areNotEnums()
            .and()
            .haveSimpleNameNotContaining("package-info")
            .and()
            .haveSimpleNameNotEndingWith("Listener")
            .should()
            .haveSimpleNameStartingWith("Evento")
            .because("Domain events should start with 'Evento' prefix for consistency");

    /**
     * Garante que Facades não acessem Repositories diretamente.
     * Facades devem delegar operações de dados para Services especializados,
     * que por sua vez acessam os Repositories (ADR-001).
     *
     * <p><b>Contexto:</b> Durante análise arquitetural foi descoberto que 8 facades
     * (62% do total) estavam injetando 17 repositórios diretamente, violando o
     * padrão Facade e criando acoplamento desnecessário.
     *
     * <p><b>Padrão Correto:</b>
     * <ul>
     *   <li>Controller → Facade → Service → Repository ✅</li>
     *   <li>Controller → Facade → Repository ❌ (violação)</li>
     * </ul>
     *
     * <p><b>Facades Identificadas com Violações:</b>
     * <ul>
     *   <li>UnidadeFacade (3 repos)</li>
     *   <li>UsuarioFacade (4 repos)</li>
     *   <li>SubprocessoFacade (2 repos)</li>
     *   <li>MapaFacade (2 repos)</li>
     *   <li>ProcessoFacade (1 repo)</li>
     *   <li>AnaliseFacade (1 repo)</li>
     *   <li>AlertaFacade (2 repos)</li>
     *   <li>ConfiguracaoFacade (1 repo)</li>
     * </ul>
     *
     * @see <a href="/simplification-plan.md">Plano de Simplificação - Seção 3</a>
     * @see <a href="/docs/adr/ADR-001-facade-pattern.md">ADR-001: Facade Pattern</a>
     */
    @ArchTest
    static final ArchRule facades_should_not_access_repositories_directly = noClasses()
            .that()
            .haveSimpleNameEndingWith("Facade")
            .should()
            .dependOnClassesThat()
            .areAssignableTo(JpaRepository.class)
            .orShould()
            .dependOnClassesThat()
            .haveSimpleName("ComumRepo")
            .because("Facades should delegate to Services, not access Repositories directly (ADR-001). " +
                    "See simplification-plan.md section 3 'Facades - Hierarquia Excessiva'");

    /**
     * Verifica ausência de ciclos internos nos pacotes workflow de cada módulo.
     *
     * <p>Garante que pacotes internos de service (workflow, crud, etc.) não criem
     * dependências circulares entre si dentro do mesmo módulo.
     *
     * <p><b>Nota:</b> @Lazy pode ser usado para quebrar ciclos internos quando
     * houver necessidade legítima de referências mútuas.
     */
    @ArchTest
    static final ArchRule no_cycles_within_service_packages = slices()
            .matching("sgc.(*).service.(**)")
            .should()
            .beFreeOfCycles();

}
