package sgc.e2e;

import lombok.*;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.dto.*;

import java.time.*;
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
        String tituloAnterior = jdbcTemplate.query(
                "SELECT usuario_titulo FROM sgc.vw_responsabilidade WHERE unidade_codigo = ?",
                rs -> rs.next() ? rs.getString("usuario_titulo") : null,
                codUnidade
        );

        String matriculaUsuario = jdbcTemplate.queryForObject(
                "SELECT matricula FROM sgc.vw_usuario WHERE titulo = ?",
                String.class,
                request.tituloEleitoralUsuario()
        );

        String tipoUnidade = jdbcTemplate.queryForObject(
                "SELECT tipo FROM sgc.vw_unidade WHERE codigo = ?",
                String.class,
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

    private void removerPerfisDeResponsabilidadeAnteriores(String tituloAnterior, String novoTitulo, Long codUnidade) {
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
}
