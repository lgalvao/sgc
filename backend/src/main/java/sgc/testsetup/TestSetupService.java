package sgc.testsetup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.AnaliseRepo;
import sgc.analise.modelo.TipoAnalise;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.atividade.modelo.Conhecimento;
import sgc.atividade.modelo.ConhecimentoRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.modelo.*;
import sgc.sgrh.modelo.Usuario;
import sgc.sgrh.modelo.UsuarioRepo;
import sgc.subprocesso.modelo.*;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
// TODO Precisamos mesmo dessa confusão toda?
public class TestSetupService {
    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final AnaliseRepo analiseRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final UsuarioRepo usuarioRepo;

    @Transactional
    public Map<String, Object> criarProcessoComMapaFinalizado(Map<String, Object> params) {
        String siglaUnidade = (String) params.get("siglaUnidade");
        String prefixo = (String) params.getOrDefault("prefixo", "E2E");

        // 1. Encontrar a Unidade e seu Chefe
        Unidade unidade = unidadeRepo.findBySigla(siglaUnidade)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada: " + siglaUnidade));
        Usuario chefe = usuarioRepo.findById(unidade.getTitular().getTituloEleitoral())
                .orElseThrow(() -> new RuntimeException("Chefe da unidade não encontrado."));

        // 2. Criar e Iniciar o Processo
        Processo processo = new Processo();
        processo.setDescricao(prefixo + " - Processo de Mapeamento para " + siglaUnidade);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataLimite(LocalDate.now().plusDays(30).atStartOfDay());
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO); // Inicia diretamente
        processo = processoRepo.save(processo);

        unidadeProcessoRepo.save(new UnidadeProcesso(processo.getCodigo(), unidade.getCodigo(), unidade.getNome(), unidade.getSigla(), String.valueOf(chefe.getTituloEleitoral()), unidade.getTipo(), "INICIADO", null));

        // 3. Criar o Subprocesso e Mapa inicial
        Mapa mapa = mapaRepo.save(new Mapa());
        Subprocesso subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, processo.getDataLimite());
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Processo iniciado via Test Setup"));

        // 4. Cadastrar Atividades e Conhecimentos
        Atividade atividade = atividadeRepo.save(new Atividade(mapa, prefixo + " - Atividade de Teste"));
        conhecimentoRepo.save(new Conhecimento(prefixo + " - Conhecimento de Teste", atividade));
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Cadastro disponibilizado"));

        // 5. Simular Análise e Homologação do Cadastro
        analiseRepo.save(Analise.builder().subprocesso(subprocesso).tipo(TipoAnalise.CADASTRO).observacoes("Análise de aceite (Gestor) via Test Setup").build());
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Cadastro aceito"));

        analiseRepo.save(Analise.builder().subprocesso(subprocesso).tipo(TipoAnalise.CADASTRO).observacoes("Análise de homologação (Admin) via Test Setup").build());
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Cadastro homologado"));

        // 6. Cadastrar Competências e Associar
        Competencia competencia = competenciaRepo.save(new Competencia(prefixo + " - Competência de Teste", mapa));
        competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(competencia.getCodigo(), atividade.getCodigo()), competencia, atividade));
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Mapa disponibilizado"));

        // 7. Simular Validação e Homologação do Mapa
        analiseRepo.save(Analise.builder().subprocesso(subprocesso).tipo(TipoAnalise.VALIDACAO).observacoes("Análise de validação (Chefe) via Test Setup").build());
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Mapa validado"));

        analiseRepo.save(Analise.builder().subprocesso(subprocesso).tipo(TipoAnalise.VALIDACAO).observacoes("Análise de aceite (Gestor) via Test Setup").build());
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Mapa aceito"));

        analiseRepo.save(Analise.builder().subprocesso(subprocesso).tipo(TipoAnalise.VALIDACAO).observacoes("Análise de homologação (Admin) via Test Setup").build());
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Mapa homologado"));

        // 8. Finalizar o Processo e tornar o Mapa Vigente
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processoRepo.save(processo);

        unidadeMapaRepo.save(new UnidadeMapa(unidade.getCodigo(), mapa.getCodigo(), LocalDateTime.now(), unidade, mapa));

        // 9. Retornar IDs para verificação no teste
        Map<String, Object> result = new HashMap<>();
        result.put("processoId", processo.getCodigo());
        result.put("subprocessoId", subprocesso.getCodigo());
        result.put("mapaId", mapa.getCodigo());
        result.put("unidadeId", unidade.getCodigo());
        return result;
    }
}
