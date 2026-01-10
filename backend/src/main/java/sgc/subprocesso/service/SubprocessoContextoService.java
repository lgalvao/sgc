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
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

/**
 * Serviço responsável por obter o contexto de edição de um subprocesso.
 *
 * <p>Encapsula a lógica para construir o DTO de contexto com informações
 * necessárias para edição (mapas, atividades, permissões).
 *
 * <p><b>Nota arquitetural:</b> Uso deveria ser via {@link SubprocessoFacade},
 * mas mantido público temporariamente para compatibilidade com testes.
 */
@Service
@RequiredArgsConstructor
public class SubprocessoContextoService {
    private final UsuarioService usuarioService;
    private final MapaFacade mapaFacade;
    private final SubprocessoService subprocessoService;

    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codSubprocesso, Perfil perfil) {
        SubprocessoDetalheDto subprocessoDto = subprocessoService.obterDetalhes(codSubprocesso, perfil);
        String siglaUnidade = subprocessoDto.getUnidade().getSigla();
        Subprocesso subprocesso = subprocessoService.buscarSubprocesso(codSubprocesso);
        UnidadeDto unidadeDto = usuarioService.buscarUnidadePorSigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

        MapaCompletoDto mapaDto = null;
        if (subprocesso.getMapa() != null) {
            mapaDto = mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
        }

        List<AtividadeVisualizacaoDto> atividades = subprocessoService.listarAtividadesSubprocesso(codSubprocesso);
        return ContextoEdicaoDto.builder()
                .unidade(unidadeDto)
                .subprocesso(subprocessoDto)
                .mapa(mapaDto)
                .atividadesDisponiveis(atividades)
                .build();
    }
}
