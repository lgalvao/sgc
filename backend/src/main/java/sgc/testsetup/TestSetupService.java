package sgc.testsetup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.modelo.*;
import sgc.subprocesso.modelo.*;
import sgc.unidade.modelo.*;
import sgc.mapa.modelo.*;
import sgc.competencia.modelo.*;
import sgc.atividade.modelo.*;
import sgc.conhecimento.modelo.*;
import sgc.sgrh.modelo.*;
import sgc.analise.modelo.*;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
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
        processo.setDataLimite(LocalDate.now().plusDays(30));
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
        movimentacaoRepo.save(new Movimentacao(subprocesso, chefe, unidade, "Cadastro disponibilizado"));

        // 5. Simular Análise e Homologação do Cadastro
        analiseRepo.save(new Analise(subprocesso, TipoAnalise.CADASTRO, "Análise de aceite (Gestor) via Test Setup"));
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_ACEITO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Cadastro aceito"));

        analiseRepo.save(new Analise(subprocesso, TipoAnalise.CADASTRO, "Análise de homologação (Admin) via Test Setup"));
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Cadastro homologado"));

        // 6. Cadastrar Competências e Associar
        Competencia competencia = competenciaRepo.save(new Competencia(prefixo + " - Competência de Teste", mapa));
        competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(competencia.getCodigo(), atividade.getCodigo()), competencia, atividade));
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, chefe, unidade, "Mapa disponibilizado"));

        // 7. Simular Validação e Homologação do Mapa
        analiseRepo.save(new Analise(subprocesso, TipoAnalise.VALIDACAO, "Análise de validação (Chefe) via Test Setup"));
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, chefe, unidade, "Mapa validado"));

        analiseRepo.save(new Analise(subprocesso, TipoAnalise.VALIDACAO, "Análise de aceite (Gestor) via Test Setup"));
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_ACEITO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Mapa aceito"));

        analiseRepo.save(new Analise(subprocesso, TipoAnalise.VALIDACAO, "Análise de homologação (Admin) via Test Setup"));
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        subprocesso = subprocessoRepo.save(subprocesso);
        movimentacaoRepo.save(new Movimentacao(subprocesso, null, unidade, "Mapa homologado"));

        // 8. Finalizar o Processo e tornar o Mapa Vigente
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processoRepo.save(processo);

        unidadeMapaRepo.save(new UnidadeMapa(unidade.getCodigo(), mapa.getCodigo()));

        // 9. Retornar IDs para verificação no teste
        Map<String, Object> result = new HashMap<>();
        result.put("processoId", processo.getCodigo());
        result.put("subprocessoId", subprocesso.getCodigo());
        result.put("mapaId", mapa.getCodigo());
        result.put("unidadeId", unidade.getCodigo());
        return result;
    }
}
