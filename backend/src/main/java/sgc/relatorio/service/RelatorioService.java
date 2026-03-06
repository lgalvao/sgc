package sgc.relatorio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.relatorio.dto.RelatorioAndamentoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.UsuarioFacade;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioService {

    private final SubprocessoService subprocessoService;
    private final UsuarioFacade usuarioFacade;

    @Transactional(readOnly = true)
    public List<RelatorioAndamentoDto> obterRelatorioAndamento(Long processoId) {
        log.info("Gerando relatorio de andamento para processo {}", processoId);
        
        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(processoId);
        Usuario usuarioAcesso = usuarioFacade.usuarioAutenticado();

        return subprocessos.stream()
                .map(sp -> {
                    SubprocessoDetalheResponse detalhes = subprocessoService.obterDetalhes(sp, usuarioAcesso);
                    
                    String responsavel = detalhes.responsavel() != null && detalhes.responsavel().usuario() != null
                            ? detalhes.responsavel().usuario().getNome() 
                            : null;
                    String titular = detalhes.titular() != null ? detalhes.titular().getNome() : null;
                    
                    LocalDateTime dataMovimentacao = detalhes.movimentacoes() != null && !detalhes.movimentacoes().isEmpty()
                            ? detalhes.movimentacoes().get(0).dataHora()
                            : sp.getDataLimiteEtapa1();
                            
                    return RelatorioAndamentoDto.builder()
                            .siglaUnidade(sp.getUnidade().getSigla())
                            .nomeUnidade(sp.getUnidade().getNome())
                            .situacaoAtual(sp.getSituacao().name())
                            .dataUltimaMovimentacao(dataMovimentacao)
                            .responsavel(responsavel)
                            .titular(titular)
                            .build();
                })
                .toList();
    }

    public byte[] exportarRelatorioAndamentoPdf(Long processoId) {
        // Implementar geração real de PDF do JasperReports / iText se necessário
        log.info("Mock exportação PDF andamento processo {}", processoId);
        return new byte[0];
    }

    public byte[] exportarRelatorioMapasPdf(Long processoId, Long unidadeId) {
        // Implementar geração real de PDF do JasperReports / iText se necessário
        log.info("Mock exportação PDF mapas processo {}, unidade {}", processoId, unidadeId);
        return new byte[0];
    }
}
