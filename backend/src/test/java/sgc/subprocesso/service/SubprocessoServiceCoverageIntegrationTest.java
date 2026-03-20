package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
class SubprocessoServiceCoverageIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Nested
    @DisplayName("excluir")
    class Excluir {

        @Test
        @DisplayName("deve excluir subprocesso")
        void deveExcluir() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U1");
            u = unidadeRepo.save(u);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            subprocessoService.excluir(sp.getCodigo());

            assertThat(subprocessoRepo.findById(sp.getCodigo())).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarEntidadesPorProcessoEUnidades")
    class ListarEntidadesPorProcessoEUnidades {

        @Test
        @DisplayName("deve retornar vazio se lista de unidades for vazia")
        void vazioSeVazio() {
            java.util.List<Subprocesso> lista = subprocessoService.listarEntidadesPorProcessoEUnidades(1L, java.util.List.of());
            assertThat(lista).isEmpty();
        }
    }

    @Nested
    @DisplayName("criarParaMapeamento")
    class CriarParaMapeamento {

        @Test
        @DisplayName("deve abortar se nenhuma unidade elegivel for encontrada")
        void semElegiveis() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U1");
            u.setTipo(sgc.organizacao.model.TipoUnidade.INTERMEDIARIA); // Nao elegivel
            u = unidadeRepo.save(u);

            sgc.organizacao.model.Usuario user = new sgc.organizacao.model.Usuario();
            user.setTituloEleitoral("123");

            subprocessoService.criarParaMapeamento(proc, java.util.List.of(u), u, user);

            assertThat(subprocessoRepo.findByProcessoCodigoComUnidade(proc.getCodigo())).isEmpty();
        }
    }

    @Nested
    @DisplayName("obterMapaParaAjuste")
    class ObterMapaParaAjuste {

        @Test
        @DisplayName("deve buscar mapa e montar dto")
        void deveBuscarMapa() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U2");
            u = unidadeRepo.save(u);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            sp = subprocessoRepo.saveAndFlush(sp);

            sgc.subprocesso.dto.MapaAjusteDto dto = subprocessoService.obterMapaParaAjuste(sp.getCodigo());

            assertThat(dto).isNotNull();
            assertThat(dto.getCodMapa()).isEqualTo(mapa.getCodigo());
        }
    }

    @Nested
    @DisplayName("salvarAjustesMapa")
    class SalvarAjustesMapa {

        @Test
        @DisplayName("deve salvar ajustes e alterar a situacao")
        void salvarAjustes() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U3");
            u = unidadeRepo.save(u);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            sp = subprocessoRepo.saveAndFlush(sp);

            subprocessoService.salvarAjustesMapa(sp.getCodigo(), java.util.List.of());

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        }
    }

    @Nested
    @DisplayName("listarPorProcessoESituacoes")
    class ListarPorProcessoESituacoes {

        @Test
        @DisplayName("deve listar por processo e situacao")
        void deveListar() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U10");
            u = unidadeRepo.save(u);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            java.util.List<Subprocesso> lista = subprocessoService.listarPorProcessoESituacoes(proc.getCodigo(), java.util.List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).getCodigo()).isEqualTo(sp.getCodigo());
        }
    }

    @Nested
    @DisplayName("listarPorProcessoUnidadeESituacoes")
    class ListarPorProcessoUnidadeESituacoes {

        @Test
        @DisplayName("deve listar por processo, unidade e situacao")
        void deveListar() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U11");
            u = unidadeRepo.save(u);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            java.util.List<Subprocesso> lista = subprocessoService.listarPorProcessoUnidadeESituacoes(proc.getCodigo(), u.getCodigo(), java.util.List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));
            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).getCodigo()).isEqualTo(sp.getCodigo());
        }
    }

    @Nested
    @DisplayName("atualizarEntidade")
    class AtualizarEntidade {

        @Test
        @DisplayName("deve atualizar e mudar o mapa quando for diferente")
        void deveMudarMapaQuandoDiferente() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U1");
            u = unidadeRepo.save(u);

            Mapa mapaAntigo = new Mapa();
            mapaAntigo = mapaRepo.save(mapaAntigo);

            Mapa mapaNovo = new Mapa();
            mapaNovo = mapaRepo.save(mapaNovo);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setMapa(mapaAntigo);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                    .codMapa(mapaNovo.getCodigo())
                    .build();

            Subprocesso atualizado = subprocessoService.atualizarEntidade(sp.getCodigo(), request);

            assertThat(atualizado.getMapa().getCodigo()).isEqualTo(mapaNovo.getCodigo());
        }

        @Test
        @DisplayName("nao deve mudar mapa se for o mesmo")
        void naoDeveMudarMapaSeForMesmo() {
            Processo proc = new Processo();
            proc = processoRepo.save(proc);

            Unidade u = new Unidade();
            u.setSigla("U1");
            u = unidadeRepo.save(u);

            Mapa mapaAntigo = new Mapa();
            mapaAntigo = mapaRepo.save(mapaAntigo);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(u);
            sp.setMapa(mapaAntigo);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                    .codMapa(mapaAntigo.getCodigo())
                    .build();

            Subprocesso atualizado = subprocessoService.atualizarEntidade(sp.getCodigo(), request);

            assertThat(atualizado.getMapa().getCodigo()).isEqualTo(mapaAntigo.getCodigo());
        }
    }
}
