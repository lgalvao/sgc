package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
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
 * MapaFacade injetado normalmente. Dependência circular verificada e refutada.
 */
@Service
@RequiredArgsConstructor
public class SubprocessoContextoService {
    private final UsuarioService usuarioService;
    private final MapaFacade mapaFacade;
    private final SubprocessoCrudService crudService;
    private final SubprocessoDetalheService detalheService;

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
