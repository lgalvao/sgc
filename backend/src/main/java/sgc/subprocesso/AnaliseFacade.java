package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.CriarAnaliseCommand;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.AnaliseService;

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
@Transactional(readOnly = true)
public class AnaliseFacade {
    private final AnaliseService analiseService;
    private final OrganizacaoFacade organizacaoFacade;

    public List<Analise> listarPorSubprocesso(Long codSubprocesso, TipoAnalise tipoAnalise) {
        return analiseService.listarPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == tipoAnalise)
                .toList();
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return analiseService.listarPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.CADASTRO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return analiseService.listarPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.VALIDACAO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    public AnaliseHistoricoDto paraHistoricoDto(Analise analise) {
        UnidadeDto unidade = organizacaoFacade.dtoPorCodigo(analise.getUnidadeCodigo());
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

    @Transactional
    public Analise criarAnalise(Subprocesso subprocesso, CriarAnaliseCommand command) {
        UnidadeDto unidadeDto = organizacaoFacade.buscarPorSigla(command.siglaUnidade());

        Analise analise = Analise.builder()
                .subprocesso(subprocesso)
                .dataHora(LocalDateTime.now())
                .observacoes(command.observacoes())
                .tipo(command.tipo())
                .acao(command.acao())
                .unidadeCodigo(unidadeDto.getCodigo())
                .usuarioTitulo(command.tituloUsuario())
                .motivo(command.motivo())
                .build();

        return analiseService.salvar(analise);
    }

    @Transactional
    public void removerPorSubprocesso(Long codSubprocesso) {
        analiseService.removerPorSubprocesso(codSubprocesso);
    }
}
