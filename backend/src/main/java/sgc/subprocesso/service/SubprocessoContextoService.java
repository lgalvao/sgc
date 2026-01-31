package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.Acao;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoCadastroDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.SugestoesDto;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sgc.mapa.service.MapaFacade;
import sgc.seguranca.acesso.AccessControlService;

/**
 * Service responsável por preparar contextos de visualização de subprocessos.
 * 
 * <p>Extrai lógica de preparação de DTOs complexos que estava em métodos privados de {@link SubprocessoFacade}.
 * Responsabilidades:
 * <ul>
 *   <li>Preparar contexto de detalhes do subprocesso</li>
 *   <li>Preparar contexto de cadastro do subprocesso</li>
 *   <li>Preparar contexto de edição do subprocesso</li>
 *   <li>Obter sugestões do subprocesso</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoContextoService {

    private final SubprocessoCrudService crudService;
    private final UsuarioFacade usuarioService;
    private final UnidadeFacade unidadeFacade;
    private final MapaFacade mapaFacade;
    private final MapaManutencaoService mapaManutencaoService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SubprocessoDetalheMapper subprocessoDetalheMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final AccessControlService accessControlService;
    private final SubprocessoAtividadeService atividadeService;
    private final SubprocessoPermissaoCalculator permissaoCalculator;

    /**
     * Obtém detalhes completos de um subprocesso.
     * 
     * @param codigo código do subprocesso
     * @param usuarioAutenticado usuário autenticado
     * @return DTO com detalhes do subprocesso
     */
    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);
        return obterDetalhes(sp, usuarioAutenticado);
    }

    /**
     * Obtém detalhes completos de um subprocesso a partir da entidade.
     * 
     * @param sp subprocesso
     * @param usuarioAutenticado usuário autenticado
     * @return DTO com detalhes do subprocesso
     */
    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        accessControlService.verificarPermissao(usuarioAutenticado, Acao.VISUALIZAR_SUBPROCESSO, sp);

        Usuario responsavel = usuarioService.buscarResponsavelAtual(sp.getUnidade().getSigla());
        Usuario titular = null;
        try {
            titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
        } catch (Exception e) {
            log.warn("Erro ao buscar titular: {}", e.getMessage());
        }

        List<Movimentacao> movimentacoes = movimentacaoRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        SubprocessoPermissoesDto permissoes = permissaoCalculator.calcularPermissoes(sp, usuarioAutenticado);

        return subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes);
    }

    /**
     * Obtém contexto de cadastro de um subprocesso.
     * 
     * @param codSubprocesso código do subprocesso
     * @return DTO com contexto de cadastro
     */
    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);

        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        accessControlService.verificarPermissao(usuario, Acao.VISUALIZAR_SUBPROCESSO, sp);

        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        for (Atividade a : atividades) {
            List<ConhecimentoResponse> ksDto = a.getConhecimentos().stream().map(conhecimentoMapper::toResponse)
                    .toList();
            atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                    .codigo(a.getCodigo())
                    .descricao(a.getDescricao())
                    .conhecimentos(ksDto)
                    .build());
        }
        return SubprocessoCadastroDto.builder()
                .subprocessoCodigo(sp.getCodigo())
                .unidadeSigla(sp.getUnidade().getSigla())
                .atividades(atividadesComConhecimentos)
                .build();
    }

    /**
     * Obtém sugestões para um subprocesso.
     * 
     * @param codSubprocesso código do subprocesso
     * @return DTO com sugestões (vazio por enquanto)
     */
    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        crudService.buscarSubprocesso(codSubprocesso); 
        return SugestoesDto.builder()
                .sugestoes("")
                .dataHora(LocalDateTime.now())
                .build();
    }

    /**
     * Obtém contexto completo para edição de um subprocesso.
     * 
     * @param codSubprocesso código do subprocesso
     * @return DTO com contexto de edição
     */
    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        SubprocessoDetalheDto subprocessoDto = obterDetalhes(subprocesso, usuario);

        String sigla = subprocessoDto.getUnidade().getSigla();
        UnidadeDto unidadeDto = unidadeFacade.buscarPorSigla(sigla);

        MapaCompletoDto mapaDto = mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
        List<AtividadeDto> atividades = atividadeService.listarAtividadesSubprocesso(codSubprocesso);

        return ContextoEdicaoDto.builder()
                .unidade(unidadeDto)
                .subprocesso(subprocessoDto)
                .mapa(mapaDto)
                .atividadesDisponiveis(atividades)
                .build();
    }
}
