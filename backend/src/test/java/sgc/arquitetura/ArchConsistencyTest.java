package sgc.arquitetura;

import com.fasterxml.jackson.annotation.*;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.importer.*;
import com.tngtech.archunit.junit.*;
import com.tngtech.archunit.lang.*;
import jakarta.persistence.*;
import org.jspecify.annotations.*;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.model.*;

import java.lang.reflect.*;
import java.util.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.*;

@Tag("integration")
@AnalyzeClasses(packages = "sgc", importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class}
)
public class ArchConsistencyTest {
    @ArchTest
    static final ArchRule controllers_should_not_access_repositories = noClasses()
            .that()
            .haveNameMatching("^(?!.*E2eController).*Controller$")
            .should()
            .accessClassesThat()
            .haveNameMatching(".*Repo");

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

    @ArchTest
    static final ArchRule production_code_should_not_use_facade_suffix = noClasses()
            .should()
            .haveSimpleNameEndingWith("Facade")
            .because("O backend atual do SGC não adota Facade como categoria arquitetural; use Service ou AplicacaoService");

    @ArchTest
    static final ArchRule dtos_should_not_be_jpa_entities = noClasses()
            .that()
            .haveSimpleNameEndingWith("Dto")
            .should()
            .beAnnotatedWith(Entity.class)
            .because("DTOs should never be JPA entities - use separate entity classes");

    @ArchTest
    static final ArchRule controllers_should_not_expose_jpa_entities_in_http_responses = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .and()
            .areDeclaredInClassesThat()
            .doNotHaveSimpleName("E2eController")
            .should(new ArchCondition<>("not expose JPA entities in HTTP responses") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    try {
                        if (contemEntidadeJpa(method.reflect().getGenericReturnType())) {
                            String mensagem = String.format(
                                    "Método %s.%s expõe entidade JPA no retorno HTTP: %s",
                                    method.getOwner().getSimpleName(), method.getName(), method.getDescription());
                            events.add(SimpleConditionEvent.violated(method, mensagem));
                        }
                    } catch (Exception e) {
                        String mensagem = String.format(
                                "Não foi possível inspecionar o retorno de %s.%s: %s",
                                method.getOwner().getSimpleName(), method.getName(), e.getMessage());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Controllers devem responder com DTOs e nunca expor entidades JPA, mesmo dentro de ResponseEntity, List ou Page");

    @ArchTest
    static final ArchRule controllers_should_not_receive_jpa_entities_in_request_body = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .and()
            .areDeclaredInClassesThat()
            .doNotHaveSimpleName("E2eController")
            .should(new ArchCondition<>("not receive JPA entities in @RequestBody parameters") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    try {
                        Method metodoRefletido = method.reflect();
                        java.lang.reflect.Parameter[] parametros = metodoRefletido.getParameters();
                        for (java.lang.reflect.Parameter parametro : parametros) {
                            if (parametro.isAnnotationPresent(RequestBody.class)
                                    && contemEntidadeJpa(parametro.getParameterizedType())) {
                                String mensagem = String.format(
                                        "Método %s.%s recebe entidade JPA em @RequestBody: %s",
                                        method.getOwner().getSimpleName(),
                                        method.getName(),
                                        parametro.getParameterizedType().getTypeName());
                                events.add(SimpleConditionEvent.violated(method, mensagem));
                            }
                        }
                    } catch (Exception e) {
                        String mensagem = String.format(
                                "Não foi possível inspecionar os Configuraçãos de %s.%s: %s",
                                method.getOwner().getSimpleName(), method.getName(), e.getMessage());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Controllers devem receber DTOs/requests explícitos, não entidades JPA no corpo HTTP");

    @ArchTest
    static final ArchRule controllers_should_not_expose_internal_model_types_in_http_contracts = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .and()
            .areDeclaredInClassesThat()
            .doNotHaveSimpleName("E2eController")
            .should(new ArchCondition<>("not expose internal model types in HTTP contracts") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    try {
                        if (contemModeloInternoAplicacao(method.reflect().getGenericReturnType())) {
                            String mensagem = String.format(
                                    "Método %s.%s expõe tipo interno de model no retorno HTTP: %s",
                                    method.getOwner().getSimpleName(), method.getName(), method.getDescription());
                            events.add(SimpleConditionEvent.violated(method, mensagem));
                        }

                        for (java.lang.reflect.Parameter parametro : method.reflect().getParameters()) {
                            if (parametro.isAnnotationPresent(RequestBody.class)
                                    && contemModeloInternoAplicacao(parametro.getParameterizedType())) {
                                String mensagem = String.format(
                                        "Método %s.%s recebe tipo interno de model em @RequestBody: %s",
                                        method.getOwner().getSimpleName(),
                                        method.getName(),
                                        parametro.getParameterizedType().getTypeName());
                                events.add(SimpleConditionEvent.violated(method, mensagem));
                            }
                        }
                    } catch (Exception e) {
                        String mensagem = String.format(
                                "Não foi possível inspecionar contrato HTTP de %s.%s: %s",
                                method.getOwner().getSimpleName(), method.getName(), e.getMessage());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Controllers da aplicação devem expor DTOs explícitos e não tipos internos de model no contrato HTTP");

    @ArchTest
    static final ArchRule controllers_should_not_use_json_view_except_e2e = noMethods()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .and()
            .areDeclaredInClassesThat()
            .doNotHaveSimpleName("E2eController")
            .should()
            .beAnnotatedWith(JsonView.class)
            .because("Controllers da aplicação devem usar DTOs explícitos; @JsonView fica restrito ao adapter interno de E2E");

    @ArchTest
    static final ArchRule dto_packages_with_explicit_http_contracts_should_not_use_json_view = classes()
            .that()
            .resideInAnyPackage(
                    "sgc.organizacao.dto..",
                    "sgc.mapa.dto..",
                    "sgc.processo.dto..",
                    "sgc.subprocesso.dto..",
                    "sgc.seguranca.dto..",
                    "sgc.alerta.dto..")
            .should(new ArchCondition<>("não usar @JsonView em DTOs de contratos HTTP explícitos") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    if (item.isAnnotatedWith(JsonView.class)) {
                        events.add(SimpleConditionEvent.violated(
                                item,
                                "Classe " + item.getName() + " usa @JsonView em DTO de contrato HTTP explícito"));
                    }

                    item.getFields().stream()
                            .filter(field -> field.isAnnotatedWith(JsonView.class))
                            .forEach(field -> events.add(SimpleConditionEvent.violated(
                                    field,
                                    "Campo " + field.getFullName() + " usa @JsonView em DTO de contrato HTTP explícito")));

                    item.getMethods().stream()
                            .filter(method -> method.isAnnotatedWith(JsonView.class))
                            .forEach(method -> events.add(SimpleConditionEvent.violated(
                                    method,
                                    "Método " + method.getFullName() + " usa @JsonView em DTO de contrato HTTP explícito")));
                }
            })
            .because("Nos módulos já migrados, a borda HTTP deve ser descrita por DTOs explícitos e estáveis, sem serialização condicional por view");

    @ArchTest
    static final ArchRule only_models_and_e2e_adapter_should_depend_on_json_views = noClasses()
            .that()
            .resideOutsideOfPackages("sgc..model..", "sgc.e2e..")
            .should()
            .accessClassesThat()
            .haveSimpleNameEndingWith("Views")
            .because("Classes *Views e JsonView ficam contidas no legado de entidades/model e no adapter de E2E, sem voltar a contaminar contratos, services ou controllers da aplicação");

    @ArchTest
    static final ArchRule controllers_should_not_use_authentication_principal = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .should(new ArchCondition<>("não receber Configuraçãos com @AuthenticationPrincipal") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    try {
                        for (java.lang.reflect.Parameter parametro : method.reflect().getParameters()) {
                            if (parametro.isAnnotationPresent(AuthenticationPrincipal.class)) {
                                String mensagem = String.format(
                                        "Método %s.%s usa @AuthenticationPrincipal em vez de contexto autenticado centralizado",
                                        method.getOwner().getSimpleName(),
                                        method.getName());
                                events.add(SimpleConditionEvent.violated(method, mensagem));
                            }
                        }
                    } catch (Exception e) {
                        String mensagem = String.format(
                                "Não foi possível inspecionar os Configuraçãos de %s.%s: %s",
                                method.getOwner().getSimpleName(), method.getName(), e.getMessage());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Controllers devem obter o contexto autenticado pelo ponto centralizado, sem repassar @AuthenticationPrincipal");

    @ArchTest
    static final ArchRule application_public_methods_should_not_receive_usuario_param = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Controller")
            .or()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Service")
            .or()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("AplicacaoService")
            .should(new ArchCondition<>("não receber Usuario como Configuração de aplicação") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    String nomeClasse = method.getOwner().getName();
                    if (nomeClasse.startsWith("sgc.organizacao.")
                            || nomeClasse.startsWith("sgc.seguranca.")
                            || nomeClasse.startsWith("sgc.e2e.")) {
                        return;
                    }

                    try {
                        for (java.lang.reflect.Parameter parametro : method.reflect().getParameters()) {
                            if (parametro.getType().equals(Usuario.class)) {
                                String mensagem = String.format(
                                        "Método público %s.%s recebe Usuario como Configuração; o usuário atual deve vir do contexto autenticado centralizado",
                                        method.getOwner().getSimpleName(),
                                        method.getName());
                                events.add(SimpleConditionEvent.violated(method, mensagem));
                            }
                        }
                    } catch (Exception e) {
                        String mensagem = String.format(
                                "Não foi possível inspecionar os Configuraçãos de %s.%s: %s",
                                method.getOwner().getSimpleName(), method.getName(), e.getMessage());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Controller e services de aplicação não devem receber Usuario para representar o usuário autenticado atual");

    @ArchTest
    static final ArchRule only_security_organization_and_e2e_should_access_security_context_holder = noClasses()
            .that()
            .resideOutsideOfPackages("sgc.organizacao..", "sgc.seguranca..", "sgc.e2e..")
            .should()
            .accessClassesThat()
            .haveFullyQualifiedName("org.springframework.security.core.context.SecurityContextHolder")
            .because("Acesso direto ao SecurityContextHolder deve ficar restrito à infraestrutura de autenticação e ao adapter de E2E");

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

    @ArchTest
    static final ArchRule repositories_with_query_should_not_use_derived_method_prefixes = classes()
            .that()
            .areAssignableTo(JpaRepository.class)
            .should(new ArchCondition<>("não declarar @Query com nome derivado do Spring Data") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    for (JavaMethod method : item.getMethods()) {
                        if (!method.getOwner().equals(item) || !method.isAnnotatedWith(org.springframework.data.jpa.repository.Query.class)) {
                            continue;
                        }

                        if (usaPrefixoDerivadoSpringData(method.getName())) {
                            String mensagem = String.format(
                                    "Repositório %s declara @Query no método %s com prefixo derivado do Spring Data",
                                    item.getSimpleName(), method.getName());
                            events.add(SimpleConditionEvent.violated(method, mensagem));
                        }
                    }
                }
            })
            .because("@Query manual deve usar nome semântico em português, e não simular query derivada");

    @ArchTest
    static final ArchRule repositories_should_not_have_default_methods_with_derived_prefixes = classes()
            .that()
            .areAssignableTo(JpaRepository.class)
            .should(new ArchCondition<>("não declarar métodos default com prefixo derivado artificial") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    for (JavaMethod method : item.getMethods()) {
                        if (!method.getOwner().equals(item)) {
                            continue;
                        }

                        try {
                            Method metodoRefletido = method.reflect();
                            boolean delegaParaMetodoBaseJpa = method.getMethodCallsFromSelf().stream()
                                    .map(call -> call.getTarget().getName())
                                    .anyMatch(ArchConsistencyTest::ehMetodoBaseJpaPorChavePrimaria);

                            if (metodoRefletido.isDefault()
                                    && usaPrefixoDerivadoSpringData(method.getName())
                                    && delegaParaMetodoBaseJpa) {
                                String mensagem = String.format(
                                        "Repositório %s declara método default %s com prefixo derivado artificial",
                                        item.getSimpleName(), method.getName());
                                events.add(SimpleConditionEvent.violated(method, mensagem));
                            }
                        } catch (Exception e) {
                            String mensagem = String.format(
                                    "Não foi possível inspecionar o método default %s.%s: %s",
                                    item.getSimpleName(), method.getName(), e.getMessage());
                            events.add(SimpleConditionEvent.violated(method, mensagem));
                        }
                    }
                }
            })
            .because("Wrappers default em Repo que simulam métodos derivados devem ser removidos");

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
    static final ArchRule no_lazy_annotation_allowed = classes()
            .should(new ArchCondition<>("não utilizar @Lazy") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    String lazyAnnotation = "org.springframework.context.annotation.Lazy";
                    if (item.isAnnotatedWith(lazyAnnotation)) {
                        events.add(SimpleConditionEvent.violated(item, item.getName() + " está anotada com @Lazy"));
                    }
                    item.getFields().stream()
                            .filter(f -> f.isAnnotatedWith(lazyAnnotation))
                            .forEach(f -> events.add(SimpleConditionEvent.violated(f, f.getFullName() + " está anotada com @Lazy")));

                    item.getMethods().stream()
                            .filter(m -> m.isAnnotatedWith(lazyAnnotation))
                            .forEach(m -> events.add(SimpleConditionEvent.violated(m, m.getFullName() + " está anotada com @Lazy")));

                    item.getConstructors()
                            .forEach(c -> {
                                if (c.isAnnotatedWith(lazyAnnotation)) {
                                    events.add(SimpleConditionEvent.violated(c, c.getFullName() + " está anotada com @Lazy"));
                                }
                                c.getParameters().stream()
                                        .filter(p -> p.isAnnotatedWith(lazyAnnotation))
                                        .forEach(p -> events.add(SimpleConditionEvent.violated(p, "Configuração em " + c.getFullName() + " está anotado com @Lazy")));
                            });
                }
            })
            .because("O uso de @Lazy deve ser evitado em favor de injeção por construtor e quebra de dependências circulares");

    @ArchTest
    static final ArchRule no_cycles_within_service_packages = slices()
            .matching("sgc.(*).service.(**)")
            .should()
            .beFreeOfCycles()
            .allowEmptyShould(true);

    private static boolean contemEntidadeJpa(Type tipo) {
        if (tipo instanceof Class<?> classe) {
            return classe.isAnnotationPresent(Entity.class);
        }

        if (tipo instanceof ParameterizedType parameterizedType) {
            if (contemEntidadeJpa(parameterizedType.getRawType())) {
                return true;
            }
            for (Type argumento : parameterizedType.getActualTypeArguments()) {
                if (contemEntidadeJpa(argumento)) {
                    return true;
                }
            }
            return false;
        }

        if (tipo instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (contemEntidadeJpa(upperBound)) {
                    return true;
                }
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                if (contemEntidadeJpa(lowerBound)) {
                    return true;
                }
            }
            return false;
        }

        if (tipo instanceof GenericArrayType genericArrayType) {
            return contemEntidadeJpa(genericArrayType.getGenericComponentType());
        }

        return false;
    }

    private static boolean contemModeloInternoAplicacao(Type tipo) {
        if (tipo instanceof Class<?> classe) {
            Package pacote = classe.getPackage();
            if (pacote == null) {
                return false;
            }
            String nomePacote = pacote.getName();
            return nomePacote.startsWith("sgc.")
                    && nomePacote.contains(".model")
                    && !classe.isEnum();
        }

        if (tipo instanceof ParameterizedType parameterizedType) {
            if (contemModeloInternoAplicacao(parameterizedType.getRawType())) {
                return true;
            }
            for (Type argumento : parameterizedType.getActualTypeArguments()) {
                if (contemModeloInternoAplicacao(argumento)) {
                    return true;
                }
            }
            return false;
        }

        if (tipo instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (contemModeloInternoAplicacao(upperBound)) {
                    return true;
                }
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                if (contemModeloInternoAplicacao(lowerBound)) {
                    return true;
                }
            }
            return false;
        }

        if (tipo instanceof GenericArrayType genericArrayType) {
            return contemModeloInternoAplicacao(genericArrayType.getGenericComponentType());
        }

        return false;
    }

    private static boolean usaPrefixoDerivadoSpringData(String nomeMetodo) {
        return nomeMetodo.matches("^(find|exists|count|delete)(By|AllBy|FirstBy|TopBy).*");
    }

    private static boolean ehMetodoBaseJpaPorChavePrimaria(String nomeMetodo) {
        return Set.of("findById", "existsById", "findAllById", "deleteById", "getReferenceById").contains(nomeMetodo);
    }
}
