package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.api.MapaCompletoDto;
import sgc.mapa.MapaService;
import sgc.sgrh.SgrhService;
import sgc.sgrh.api.UnidadeDto;
import sgc.sgrh.internal.model.Perfil;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubprocessoContextoService {

    private final SubprocessoDtoService subprocessoDtoService;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final SgrhService sgrhService;
    private final MapaService mapaService;
    private final SubprocessoService subprocessoService;

    @Transactional(readOnly = true)
    public ContextoEdicaoDto obterContextoEdicao(Long codSubprocesso, Perfil perfil, Long codUnidadeUsuario) {
        // 1. Obter Detalhes do Subprocesso (já inclui validação de permissão)
        SubprocessoDetalheDto subprocessoDto = subprocessoDtoService.obterDetalhes(codSubprocesso, perfil, codUnidadeUsuario);

        // 2. Obter Unidade
        String siglaUnidade = subprocessoDto.getUnidade().getSigla();
        UnidadeDto unidadeDto = sgrhService.buscarUnidadePorSigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

        // 3. Obter Mapa Completo (se existir)
        MapaCompletoDto mapaDto = null;
        Subprocesso subprocesso = subprocessoConsultaService.getSubprocesso(codSubprocesso);
        if (subprocesso.getMapa() != null) {
            mapaDto = mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
        }

        // 4. Obter Atividades Disponíveis
        List<AtividadeVisualizacaoDto> atividades = subprocessoService.listarAtividadesSubprocesso(codSubprocesso);

        return ContextoEdicaoDto.builder()
                .unidade(unidadeDto)
                .subprocesso(subprocessoDto)
                .mapa(mapaDto)
                .atividadesDisponiveis(atividades)
                .build();
    }
}
