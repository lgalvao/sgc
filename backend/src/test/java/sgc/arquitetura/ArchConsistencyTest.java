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
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.*;

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
    static final ArchRule facades_should_have_facade_suffix = classes()
            .that()
            .resideInAPackage("..service..")
            .and()
            .areAnnotatedWith(Service.class)
            .and()
            .haveSimpleNameContaining("Facade")
            .should()
            .haveSimpleNameEndingWith("Facade")
            .allowEmptyShould(true)
            .because("Facade classes should have 'Facade' as suffix for consistency");

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
                        if (contémEntidadeJpa(method.reflect().getGenericReturnType())) {
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
                                    && contémEntidadeJpa(parametro.getParameterizedType())) {
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
                                "Não foi possível inspecionar os parâmetros de %s.%s: %s",
                                method.getOwner().getSimpleName(), method.getName(), e.getMessage());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Controllers devem receber DTOs/requests explícitos, não entidades JPA no corpo HTTP");

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
                                        .forEach(p -> events.add(SimpleConditionEvent.violated(p, "Parâmetro em " + c.getFullName() + " está anotado com @Lazy")));
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

    private static boolean contémEntidadeJpa(Type tipo) {
        if (tipo instanceof Class<?> classe) {
            return classe.isAnnotationPresent(Entity.class);
        }

        if (tipo instanceof ParameterizedType parameterizedType) {
            if (contémEntidadeJpa(parameterizedType.getRawType())) {
                return true;
            }
            for (Type argumento : parameterizedType.getActualTypeArguments()) {
                if (contémEntidadeJpa(argumento)) {
                    return true;
                }
            }
            return false;
        }

        if (tipo instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (contémEntidadeJpa(upperBound)) {
                    return true;
                }
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                if (contémEntidadeJpa(lowerBound)) {
                    return true;
                }
            }
            return false;
        }

        if (tipo instanceof GenericArrayType genericArrayType) {
            return contémEntidadeJpa(genericArrayType.getGenericComponentType());
        }

        return false;
    }
}
