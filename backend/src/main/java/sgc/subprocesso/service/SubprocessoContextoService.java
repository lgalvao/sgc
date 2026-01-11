package sgc.subprocesso.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.decomposed.SubprocessoCrudService;
import sgc.subprocesso.service.decomposed.SubprocessoDetalheService;

import java.util.List;

/**
 * Serviço responsável por obter o contexto de edição de um subprocesso.
 *
 * <p>Encapsula a lógica para construir o DTO de contexto com informações
 * necessárias para edição (mapas, atividades, permissões).
 *
 * <p><b>Nota arquitetural:</b> Uso deveria ser via {@link SubprocessoFacade},
 * mas mantido público temporariamente para compatibilidade com testes.
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b> 
 * MapaFacade é injetado com @Lazy para quebrar a dependência circular:
 * SubprocessoFacade → SubprocessoContextoService → MapaFacade → MapaVisualizacaoService → SubprocessoFacade
 */
@Service
public class SubprocessoContextoService {
    private final UsuarioService usuarioService;
    private final MapaFacade mapaFacade;
    private final SubprocessoCrudService crudService;
    private final SubprocessoDetalheService detalheService;

    /**
     * Constructor with @Lazy injection to break circular dependency.
     * 
     * @param mapaFacade injetado com @Lazy para evitar BeanCurrentlyInCreationException
     */
    public SubprocessoContextoService(
            UsuarioService usuarioService,
            @Lazy MapaFacade mapaFacade,
            SubprocessoCrudService crudService,
            SubprocessoDetalheService detalheService) {
        this.usuarioService = usuarioService;
        this.mapaFacade = mapaFacade;
        this.crudService = crudService;
        this.detalheService = detalheService;
    }

    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codSubprocesso, Perfil perfil) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        SubprocessoDetalheDto subprocessoDto = detalheService.obterDetalhes(codSubprocesso, perfil, usuario);
        String siglaUnidade = subprocessoDto.getUnidade().getSigla();
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        UnidadeDto unidadeDto = usuarioService.buscarUnidadePorSigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

        MapaCompletoDto mapaDto = null;
        if (subprocesso.getMapa() != null) {
            mapaDto = mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
        }

        List<AtividadeVisualizacaoDto> atividades = detalheService.listarAtividadesSubprocesso(codSubprocesso);
        return ContextoEdicaoDto.builder()
                .unidade(unidadeDto)
                .subprocesso(subprocessoDto)
                .mapa(mapaDto)
                .atividadesDisponiveis(atividades)
                .build();
    }
}
