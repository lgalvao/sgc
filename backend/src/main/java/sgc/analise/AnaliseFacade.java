package sgc.analise;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade para gerenciamento de análises de subprocessos.
 *
 * <p>Esta facade orquestra operações relacionadas a análises,
 * delegando a persistência para {@link AnaliseService}.
 *
 * @see AnaliseService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnaliseFacade {
    private final AnaliseService analiseService;
    private final UnidadeFacade unidadeService;

    /**
     * Lista todas as análises de um determinado tipoAnalise para um subprocesso específico.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param tipoAnalise    O tipoAnalise de análise a ser filtrada (e.g., CADASTRO, VALIDACAO).
     * @return Uma lista de {@link Analise} ordenada pela data e hora em ordem decrescente.
     */
    @Transactional(readOnly = true)
    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return analiseService.listarPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.CADASTRO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnaliseValidacaoHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return analiseService.listarPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.VALIDACAO)
                .map(this::paraValidacaoHistoricoDto)
                .toList();
    }

    private AnaliseHistoricoDto paraHistoricoDto(Analise analise) {
        UnidadeDto unidade = unidadeService.buscarPorCodigo(analise.getUnidadeCodigo());
        return AnaliseHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao())
                .unidadeSigla(unidade.getSigla())
                .unidadeNome(unidade.getNome())
                .analistaUsuarioTitulo(analise.getUsuarioTitulo())
                .motivo(analise.getMotivo())
                .tipo(analise.getTipo())
                .build();
    }

    private AnaliseValidacaoHistoricoDto paraValidacaoHistoricoDto(Analise analise) {
        UnidadeDto unidade = unidadeService.buscarPorCodigo(analise.getUnidadeCodigo());
        return AnaliseValidacaoHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao())
                .unidadeSigla(unidade.getSigla())
                .analistaUsuarioTitulo(analise.getUsuarioTitulo())
                .motivo(analise.getMotivo())
                .tipo(analise.getTipo())
                .build();
    }

    /**
     * Cria e persiste uma análise com base nos dados fornecidos.
     *
     * @param subprocesso A entidade do subprocesso.
     * @param command     O comando contendo todas as informações necessárias para criar a análise.
     * @return A entidade {@link Analise} que foi criada e salva no banco de dados.
     */
    @Transactional
    public Analise criarAnalise(Subprocesso subprocesso, CriarAnaliseCommand command) {
        Unidade unidade;
        if (command.siglaUnidade() != null) {
            var unidadeDto = unidadeService.buscarPorSigla(command.siglaUnidade());
            unidade = unidadeService.buscarEntidadePorId(unidadeDto.getCodigo());
        } else {
            // Se não veio sigla, tentamos pegar a unidade do subprocesso se fizer sentido,
            // ou lançamos erro.
            // Pela regra "Unidade is not null anywhere", vamos exigir.
            // Mas o command pode ter vindo de um contexto onde a unidade é implicita?
            // O controller pega do request body.
            throw new IllegalArgumentException("Sigla da unidade é obrigatória para criar análise.");
        }

        Analise analise = Analise.builder()
                .subprocesso(subprocesso)
                .dataHora(LocalDateTime.now())
                .observacoes(command.observacoes())
                .tipo(command.tipo())
                .acao(command.acao())
                .unidadeCodigo(unidade.getCodigo())
                .usuarioTitulo(command.tituloUsuario())
                .motivo(command.motivo())
                .build();

        return analiseService.salvar(analise);
    }

    /**
     * Remove todas as análises associadas a um subprocesso específico.
     *
     * <p>Para cenários de limpeza de dados, como a exclusão de um subprocesso, garantindo que suas
     * análises dependentes também sejam removidas.
     *
     * <p>Este método deve ser chamado dentro de uma transação existente.
     *
     * @param codSubprocesso O código do subprocesso cujas análises serão removidas.
     */
    public void removerPorSubprocesso(Long codSubprocesso) {
        analiseService.removerPorSubprocesso(codSubprocesso);
    }
}
