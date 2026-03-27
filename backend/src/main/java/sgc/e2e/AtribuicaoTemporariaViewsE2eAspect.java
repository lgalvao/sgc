package sgc.e2e;

import lombok.*;
import org.jspecify.annotations.*;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.dto.*;

import java.util.*;

@Aspect
@Component
@Profile("e2e")
@RequiredArgsConstructor
public class AtribuicaoTemporariaViewsE2eAspect {
    private static final Set<String> TIPOS_COM_PERFIL_CHEFE = Set.of("OPERACIONAL", "INTEROPERACIONAL");
    private static final Set<String> TIPOS_COM_PERFIL_GESTOR = Set.of("INTERMEDIARIA", "INTEROPERACIONAL");

    private final JdbcTemplate jdbcTemplate;

    @AfterReturning(
            value = "execution(* sgc.organizacao.service.ResponsavelUnidadeService.criarAtribuicaoTemporaria(..)) && args(codUnidade, request)",
            argNames = "codUnidade,request"
    )
    @Transactional
    public void sincronizarViews(Long codUnidade, CriarAtribuicaoRequest request) {
        String tituloAnterior = jdbcTemplate.queryForList(
                "SELECT usuario_titulo FROM sgc.vw_responsabilidade WHERE unidade_codigo = ?",
                String.class,
                codUnidade
        ).stream().findFirst().orElse(null);

        String matriculaUsuario = buscarValorObrigatorio(
                "SELECT matricula FROM sgc.vw_usuario WHERE titulo = ?",
                request.tituloEleitoralUsuario()
        );

        String tipoUnidade = buscarValorObrigatorio(
                "SELECT tipo FROM sgc.vw_unidade WHERE codigo = ?",
                codUnidade
        );

        jdbcTemplate.update("""
                MERGE INTO sgc.vw_responsabilidade
                (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio, data_fim)
                KEY(unidade_codigo)
                VALUES (?, ?, ?, 'ATRIBUICAO_TEMPORARIA', ?, ?)
                """,
                codUnidade,
                request.tituloEleitoralUsuario(),
                matriculaUsuario,
                request.dataInicio().atStartOfDay(),
                request.dataTermino().atTime(23, 59, 59)
        );

        removerPerfisDeResponsabilidadeAnteriores(tituloAnterior, request.tituloEleitoralUsuario(), codUnidade);
        sincronizarPerfisDaUnidade(request.tituloEleitoralUsuario(), codUnidade, tipoUnidade);
    }

    private void removerPerfisDeResponsabilidadeAnteriores(@Nullable String tituloAnterior, String novoTitulo, Long codUnidade) {
        if (tituloAnterior == null || tituloAnterior.equals(novoTitulo)) {
            return;
        }

        jdbcTemplate.update("""
                DELETE FROM sgc.vw_usuario_perfil_unidade
                WHERE usuario_titulo = ?
                  AND unidade_codigo = ?
                  AND perfil IN ('CHEFE', 'GESTOR')
                """,
                tituloAnterior,
                codUnidade
        );
    }

    private void sincronizarPerfisDaUnidade(String usuarioTitulo, Long codUnidade, String tipoUnidade) {
        jdbcTemplate.update("""
                DELETE FROM sgc.vw_usuario_perfil_unidade
                WHERE usuario_titulo = ?
                  AND unidade_codigo = ?
                  AND perfil IN ('CHEFE', 'GESTOR')
                """,
                usuarioTitulo,
                codUnidade
        );

        if (TIPOS_COM_PERFIL_CHEFE.contains(tipoUnidade)) {
            inserirPerfil(usuarioTitulo, codUnidade, "CHEFE");
        }

        if (TIPOS_COM_PERFIL_GESTOR.contains(tipoUnidade)) {
            inserirPerfil(usuarioTitulo, codUnidade, "GESTOR");
        }
    }

    private void inserirPerfil(String usuarioTitulo, Long codUnidade, String perfil) {
        jdbcTemplate.update("""
                MERGE INTO sgc.vw_usuario_perfil_unidade
                (usuario_titulo, unidade_codigo, perfil)
                KEY(usuario_titulo, unidade_codigo, perfil)
                VALUES (?, ?, ?)
                """,
                usuarioTitulo,
                codUnidade,
                perfil
        );
    }

    private String buscarValorObrigatorio(String sql, Object... args) {
        String valor = jdbcTemplate.queryForObject(sql, String.class, args);
        if (valor == null) {
            throw new IllegalStateException("Fixture E2E não encontrou valor obrigatório para SQL: " + sql);
        }
        return valor;
    }
}
