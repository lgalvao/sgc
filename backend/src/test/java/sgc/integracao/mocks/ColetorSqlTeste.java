package sgc.integracao.mocks;

import org.hibernate.resource.jdbc.spi.*;

import java.util.*;

public class ColetorSqlTeste implements StatementInspector {
    private static final Set<String> VIEWS_ORGANIZACIONAIS = Set.of(
            "VW_UNIDADE",
            "VW_USUARIO",
            "VW_RESPONSABILIDADE",
            "VW_USUARIO_PERFIL_UNIDADE"
    );

    private static final ThreadLocal<List<String>> SQLS = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public String inspect(String sql) {
        SQLS.get().add(sql);
        return sql;
    }

    public static void limpar() {
        SQLS.get().clear();
    }

    public static long contarSqls() {
        return SQLS.get().size();
    }

    public static long contarSqlsViewsOrganizacionais() {
        return SQLS.get().stream()
                .map(String::toUpperCase)
                .filter(ColetorSqlTeste::contemViewOrganizacional)
                .count();
    }

    private static boolean contemViewOrganizacional(String sql) {
        return VIEWS_ORGANIZACIONAIS.stream().anyMatch(sql::contains);
    }
}
