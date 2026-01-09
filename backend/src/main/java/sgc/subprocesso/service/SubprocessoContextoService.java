package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.service.MapaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubprocessoContextoService {
    private final UsuarioService usuarioService;
    private final MapaService mapaService;
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
            mapaDto = mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
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
