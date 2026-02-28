package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de Integração - MapaManutencaoService")
class MapaManutencaoServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MapaManutencaoService service;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("Cenários de Leitura (Atividade e Mapa)")
    class LeituraTests {

        @Test
        @DisplayName("Deve listar todas as atividades")
        void deveListarTodas() {
            List<Atividade> atividades = service.listarAtividades();
            assertThat(atividades).isNotEmpty();
        }

        @Test
        @DisplayName("Deve obter atividade por código")
        void deveObterPorCodigo() {
            // Em data.sql, a atividade 17001 está inserida e associada ao mapa 1700
            Atividade ativ = service.obterAtividadePorCodigo(17001L);
            assertThat(ativ).isNotNull();
            assertThat(ativ.getCodigo()).isEqualTo(17001L);
        }

        @Test
        @DisplayName("Deve buscar atividades por mapa (com conhecimentos)")
        void deveBuscarPorMapa() {
            // Em data.sql, o mapa 1700 possui a atividade 17001
            List<Atividade> ativs = service.buscarAtividadesPorMapaCodigo(1700L);
            assertThat(ativs).isNotEmpty();

            List<Atividade> comConhecimentos = service.buscarAtividadesPorMapaCodigoComConhecimentos(1700L);
            assertThat(comConhecimentos).isNotEmpty();
        }

        @Test
        @DisplayName("Deve listar todos os mapas")
        void listarTodosMapas() {
            List<Mapa> mapas = service.listarTodosMapas();
            assertThat(mapas).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar mapa vigente por unidade")
        void buscarMapaVigentePorUnidade() {
            // Em data.sql, a Unidade 8 possui o mapa 1001 como vigente no subprocesso 60000
            Mapa mapa = service.buscarMapaVigentePorUnidade(8L).orElse(null);
            assertThat(mapa).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar mapa por código de subprocesso")
        void buscarMapaPorSubprocessoCodigo() {
            // Em data.sql, o subprocesso 60000 está associado ao mapa 1001
            Mapa mapa = service.buscarMapaPorSubprocessoCodigo(60000L).orElse(null);
            assertThat(mapa).isNotNull();
        }

        @Test
        @DisplayName("Deve verificar se mapa existe")
        void mapaExiste() {
            // Em data.sql, o mapa 1001 existe
            boolean existe = service.mapaExiste(1001L);
            assertThat(existe).isTrue();
        }
    }

    @Nested
    @DisplayName("Criação de Atividade")
    class Criacao {
        @Test
        @DisplayName("Deve criar atividade com sucesso e atualizar estado do subprocesso")
        void deveCriarAtividade() {
            Mapa mapa = mapaRepo.findById(1001L).orElseThrow();

            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(mapa.getCodigo())
                    .build();

            Atividade res = service.criarAtividade(request);

            assertThat(res).isNotNull();
            assertThat(res.getMapa().getCodigo()).isEqualTo(mapa.getCodigo());
        }

        @Test
        @DisplayName("Deve lançar erro ao criar atividade sem mapa")
        void deveLancarErroAoCriarSemMapa() {
            CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                    .mapaCodigo(99999L)
                    .build();

            assertThatThrownBy(() -> service.criarAtividade(request))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Atualização e Exclusão (Atividade)")
    class AtualizacaoExclusao {
        @Test
        @DisplayName("Deve atualizar atividade com sucesso")
        void deveAtualizarAtividade() {
            // Criando dados ad-hoc pois precisamos modificar, para não impactar outros testes com data.sql
            Subprocesso sub = subprocessoRepo.findById(60000L).orElseThrow();
            Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(sub).build());
            Atividade atividade = atividadeRepo.saveAndFlush(Atividade.builder().mapa(mapa).descricao("Antiga").build());

            AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder()
                    .descricao("Nova Descrição")
                    .build();

            service.atualizarAtividade(atividade.getCodigo(), request);

            Atividade atualizada = service.obterAtividadePorCodigo(atividade.getCodigo());
            assertThat(atualizada.getDescricao()).isEqualTo("Nova Descrição");
        }

        @Test
        @DisplayName("Deve excluir atividade com sucesso")
        void deveExcluirAtividade() {
            Subprocesso sub = subprocessoRepo.findById(60000L).orElseThrow();
            Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(sub).build());
            Atividade atividade = atividadeRepo.saveAndFlush(Atividade.builder().mapa(mapa).descricao("Para Excluir").build());

            service.excluirAtividade(atividade.getCodigo());

            Long codigo = atividade.getCodigo();
            assertThatThrownBy(() -> service.obterAtividadePorCodigo(codigo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Atualização em Lote")
    class AtualizacaoLote {
        @Test
        @DisplayName("Deve atualizar descrições em lote")
        void deveAtualizarDescricoesEmLote() {
            Subprocesso sub = subprocessoRepo.findById(60000L).orElseThrow();
            Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(sub).build());
            Atividade a1 = atividadeRepo.saveAndFlush(Atividade.builder().mapa(mapa).descricao("Antiga 1").build());
            Atividade a2 = atividadeRepo.saveAndFlush(Atividade.builder().mapa(mapa).descricao("Antiga 2").build());

            Map<Long, String> descricoes = Map.of(
                    a1.getCodigo(), "Nova 1",
                    a2.getCodigo(), "Nova 2"
            );

            service.atualizarDescricoesAtividadeEmLote(descricoes);

            assertThat(service.obterAtividadePorCodigo(a1.getCodigo()).getDescricao()).isEqualTo("Nova 1");
            assertThat(service.obterAtividadePorCodigo(a2.getCodigo()).getDescricao()).isEqualTo("Nova 2");
        }
    }

    @Nested
    @DisplayName("Competência")
    class CompetenciaTests {

        @Test
        @DisplayName("Deve buscar competências por código e por mapa")
        void leitura() {
            // No data.sql o mapa 1001 possui competências 10001 e 10002
            List<Competencia> comps = service.buscarCompetenciasPorCodMapa(1001L);
            assertThat(comps).isNotEmpty();

            Competencia comp = service.buscarCompetenciaPorCodigo(comps.getFirst().getCodigo());
            assertThat(comp).isNotNull();
        }

        @Test
        @DisplayName("Deve criar, atualizar e remover competência")
        void cicloVida() {
            Subprocesso sub = subprocessoRepo.findById(60000L).orElseThrow();
            Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(sub).build());
            Atividade a1 = atividadeRepo.saveAndFlush(Atividade.builder().mapa(mapa).descricao("Atividade Teste").build());

            service.criarCompetenciaComAtividades(mapa, "Comp Nova", List.of(a1.getCodigo()));

            List<Competencia> criadas = service.buscarCompetenciasPorCodMapa(mapa.getCodigo());
            assertThat(criadas).hasSize(1);
            Competencia novaComp = criadas.getFirst();
            assertThat(novaComp.getDescricao()).isEqualTo("Comp Nova");

            service.atualizarCompetencia(novaComp.getCodigo(), "Comp Atualizada", List.of(a1.getCodigo()));

            Competencia atualizada = service.buscarCompetenciaPorCodigo(novaComp.getCodigo());
            assertThat(atualizada.getDescricao()).isEqualTo("Comp Atualizada");

            service.removerCompetencia(novaComp.getCodigo());
            Long codigo = novaComp.getCodigo();
            assertThatThrownBy(() -> service.buscarCompetenciaPorCodigo(codigo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Conhecimento")
    class ConhecimentoTests {

        @Test
        @DisplayName("Deve criar, atualizar e excluir conhecimento")
        void cicloVida() {
            Subprocesso sub = subprocessoRepo.findById(60000L).orElseThrow();
            Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(sub).build());
            Atividade atividade = atividadeRepo.saveAndFlush(Atividade.builder().mapa(mapa).descricao("Ativ para Conhec").build());

            CriarConhecimentoRequest req1 = CriarConhecimentoRequest.builder().descricao("Conhecimento 1").build();
            Conhecimento c1 = service.criarConhecimento(atividade.getCodigo(), req1);

            // Flush and clear cache to ensure DB has the correct state and the entity manager fetches it afresh
            entityManager.flush();
            entityManager.clear();

            assertThat(c1).isNotNull();
            assertThat(c1.getDescricao()).isEqualTo("Conhecimento 1");

            AtualizarConhecimentoRequest req2 = AtualizarConhecimentoRequest.builder().descricao("Conhec Atualizado").build();
            service.atualizarConhecimento(atividade.getCodigo(), c1.getCodigo(), req2);

            entityManager.flush();
            entityManager.clear();

            List<Conhecimento> conhecs = service.listarConhecimentosPorAtividade(atividade.getCodigo());
            assertThat(conhecs).hasSize(1);
            assertThat(conhecs.getFirst().getDescricao()).isEqualTo("Conhec Atualizado");

            service.excluirConhecimento(atividade.getCodigo(), c1.getCodigo());

            // Flush to trigger actual DB deletion and clear the persistence context cache to ensure correct check
            entityManager.flush();
            entityManager.clear();

            assertThat(service.listarConhecimentosPorAtividade(atividade.getCodigo())).isEmpty();
        }
    }
}
