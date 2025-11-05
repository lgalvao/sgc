package sgc.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.comum.model.EntidadeBase;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.UnidadeProcesso;
import sgc.processo.model.UnidadeProcessoRepo;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;
import java.util.Map;

/**
 * Controller com endpoints auxiliares para testes E2E.
 * <p>
 * ⚠️ ATENÇÃO: Ativo APENAS no perfil 'e2e'.
 * Estes endpoints permitem operações destrutivas para facilitar testes idempotentes.
 */
@RestController
@RequestMapping("/api/e2e")
@RequiredArgsConstructor
@Profile("e2e")
// TODO verificar se precisamos mesmo desse controller
public class E2eTestController {
    private final ProcessoRepo processoRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;

    /**
     * Remove FORÇADAMENTE um processo, independente da situação.
     * Deleta alertas relacionados antes de deletar o processo.
     */
    @PostMapping("/processos/{codigo}/forcar-exclusao")
    @Transactional
    public ResponseEntity<Void> forcarExclusaoProcesso(@PathVariable Long codigo) {
        // 1. Buscar IDs de alertas do processo
        var alertaIds = alertaRepo.findIdsByProcessoCodigo(codigo);
        // 2. Deletar referências de usuários aos alertas (resolve constraint)
        if (!alertaIds.isEmpty()) {
            alertaUsuarioRepo.deleteByIdAlertaCodigoIn(alertaIds);
        }
        // 3. Deletar alertas em bulk por processo (evita optimistic locking)
        alertaRepo.deleteByProcessoCodigo(codigo);
        // 4. Deletar movimentações vinculadas aos subprocessos (FK MOVIMENTACAO -> SUBPROCESSO)
        subprocessoRepo.findByProcessoCodigo(codigo).forEach(sp -> {
            movimentacaoRepo.findBySubprocessoCodigo(sp.getCodigo()).forEach(mv -> movimentacaoRepo.deleteById(mv.getCodigo()));
            subprocessoRepo.deleteById(sp.getCodigo());
        });
        // 5. Deletar processo
        processoRepo.deleteById(codigo);

        return ResponseEntity.noContent().build();
    }

    /**
     * Remove FORÇADAMENTE todos os processos que contenham a unidade especificada.
     * Usa código de unidade (ex: STIC = 2).
     */
    @PostMapping("/processos/unidade/{codigoUnidade}/limpar")
    @Transactional
    public ResponseEntity<Void> forcarExclusaoProcessosComUnidade(@PathVariable Long codigoUnidade) {
        // Busca todos os processos que têm a unidade através da tabela de junção
        var unidadesProcesso = unidadeProcessoRepo.findByCodUnidadeIn(List.of(codigoUnidade));
        var codigosProcesso = unidadesProcesso.stream()
                .map(UnidadeProcesso::getCodProcesso)
                .distinct()
                .toList();

        // Deleta alertas e processos

        // TODO Esse trecho é duplicado a seguir
        if (!codigosProcesso.isEmpty()) {
            var alertaIds = alertaRepo.findIdsByProcessoCodigoIn(codigosProcesso);
            if (!alertaIds.isEmpty()) {
                alertaUsuarioRepo.deleteByIdAlertaCodigoIn(alertaIds);
            }
            alertaRepo.deleteByProcessoCodigoIn(codigosProcesso);
            // Remover movimentações e subprocessos antes do processo (FKs)
            codigosProcesso.forEach(codProcesso -> {
                subprocessoRepo.findByProcessoCodigo(codProcesso).forEach(sp -> {
                    movimentacaoRepo.findBySubprocessoCodigo(sp.getCodigo()).forEach(mv -> movimentacaoRepo.deleteById(mv.getCodigo()));
                    subprocessoRepo.deleteById(sp.getCodigo());
                });
                processoRepo.deleteById(codProcesso);
            });
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Remove FORÇADAMENTE todos os processos com situação EM_ANDAMENTO.
     * Evita que testes deixem "lixo" que bloqueie testes subsequentes.
     */
    @PostMapping("/limpar-processos-em-andamento")
    @Transactional
    public ResponseEntity<Void> limparProcessosEmAndamento() {
        var processosEmAndamento = processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
        var codigosProcesso = processosEmAndamento.stream()
                .map(EntidadeBase::getCodigo)
                .toList();

        // Deleta alertas e processos
        if (!codigosProcesso.isEmpty()) {
            var alertaIds = alertaRepo.findIdsByProcessoCodigoIn(codigosProcesso);
            if (!alertaIds.isEmpty()) {
                alertaUsuarioRepo.deleteByIdAlertaCodigoIn(alertaIds);
            }
            alertaRepo.deleteByProcessoCodigoIn(codigosProcesso);
            // Remover movimentações e subprocessos antes do processo (FKs)
            codigosProcesso.forEach(codProcesso -> {
                subprocessoRepo.findByProcessoCodigo(codProcesso).forEach(sp -> {
                    movimentacaoRepo.findBySubprocessoCodigo(sp.getCodigo()).forEach(mv -> movimentacaoRepo.deleteById(mv.getCodigo()));
                    subprocessoRepo.deleteById(sp.getCodigo());
                });
                processoRepo.deleteById(codProcesso);
            });
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Reset completo: remove TODOS os processos do sistema.
     * Útil para iniciar suíte de testes com banco limpo.
     */
    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Void> resetCompleto() {
        // Deletar referências em ALERTA_USUARIO antes de deletar alertas
        alertaUsuarioRepo.deleteAll();
        alertaRepo.deleteAll();

        // Remover movimentações e subprocessos antes de processos (FKs)
        movimentacaoRepo.deleteAll();
        subprocessoRepo.deleteAll();
        processoRepo.deleteAll();
        unidadeProcessoRepo.deleteAll();

        return ResponseEntity.noContent().build();
    }

    /**
     * Reset + Reseed: Remove todos os processos e recarrega dados do import.sql.
     * ⚠️ IMPORTANTE: O Hibernate só executa import.sql automaticamente no startup.
     * Este endpoint **não** recarrega os dados - apenas limpa o banco.
     * <p>
     * Para testes idempotentes, o backend deve ser reiniciado entre execuções
     * OU os testes devem criar seus próprios dados de teste.
     */
    @PostMapping("/reset-and-reseed")
    @Transactional
    public ResponseEntity<String> resetAndReseed() {
        resetCompleto();
        return ResponseEntity.ok("Banco resetado. ATENÇÃO: Dados do import.sql não são recarregados automaticamente. Reinicie o backend para recarregar.");
    }

    /**
     * DEBUG: Verifica dados da tabela UNIDADE_PROCESSO
     */
    @GetMapping("/debug/unidade-processo/{processoId}")
    public ResponseEntity<List<Map<String, Object>>> debugUnidadeProcesso(@PathVariable Long processoId) {
        var unidadesProcesso = unidadeProcessoRepo.findByCodProcesso(processoId);

        List<Map<String, Object>> result = unidadesProcesso.stream()
                .map(up -> Map.of(
                        "processo_codigo", up.getCodProcesso(),
                        "unidade_codigo", up.getCodUnidade(),
                        "nome", up.getNome(),
                        "sigla", (Object) up.getSigla()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}
