package sgc.subprocesso.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("AnaliseRepo - Testes de Repositório")
class AnaliseRepoTest {

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Test
    @DisplayName("deve buscar analises do subprocesso em ordem decrescente")
    void deveBuscarAnalisesDoSubprocessoEmOrdemDecrescente() {
        Subprocesso subprocesso = subprocessoRepo.findById(60000L).orElseThrow();

        analiseRepo.save(Analise.builder()
                .subprocesso(subprocesso)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .dataHora(LocalDateTime.of(2025, 1, 4, 9, 0))
                .unidadeCodigo(8L)
                .usuarioTitulo("1")
                .observacoes("Primeira")
                .build());
        analiseRepo.save(Analise.builder()
                .subprocesso(subprocesso)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .dataHora(LocalDateTime.of(2025, 1, 4, 10, 0))
                .unidadeCodigo(8L)
                .usuarioTitulo("1")
                .motivo("Motivo")
                .build());

        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(60000L))
                .extracting(Analise::getDataHora)
                .containsExactly(
                        LocalDateTime.of(2025, 1, 4, 10, 0),
                        LocalDateTime.of(2025, 1, 4, 9, 0)
                );
        assertThat(analiseRepo.findBySubprocessoCodigo(60000L)).hasSize(2);
    }
}
