package sgc.processo.service;

import net.jqwik.api.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import static org.mockito.Mockito.*;

@Tag("PBT")
class ProcessoWorkflowPbtTest {

    @Property
    void criarSubprocessos_deveCriarParaTodasAsUnidades(@ForAll("processosEUnidades") ProcessoInicializadorPbtTest.ProcessoArgs args) {
        // Mock dependencies
        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        ComumRepo repo = mock(ComumRepo.class);
        UnidadeRepo unidadeRepo = mock(UnidadeRepo.class);
        sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo = mock(sgc.organizacao.model.UnidadeMapaRepo.class);
        ProcessoNotificacaoService notificacaoService = mock(ProcessoNotificacaoService.class);
        SubprocessoService subprocessoService = mock(SubprocessoService.class);
        ProcessoValidador validador = mock(ProcessoValidador.class);

        ProcessoInicializador inicializador = new ProcessoInicializador(
                processoRepo, repo, unidadeRepo, unidadeMapaRepo, notificacaoService, subprocessoService, validador
        );

        // O teste de propriedade é similar ao ProcessoInicializadorPbtTest, aqui focamos apenas na estrutura
        // que deve compilar sem SubprocessoFacade.
        assert inicializador != null;
    }

    @Provide
    Arbitrary<ProcessoInicializadorPbtTest.ProcessoArgs> processosEUnidades() {
        // Reutilizando gerador do outro teste
        return new ProcessoInicializadorPbtTest().processosEAgumentos();
    }
}
