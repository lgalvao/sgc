package sgc.processo.mapper;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.testutils.UnidadeTestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("ProcessoMapper")
class ProcessoMapperTest {
    private ProcessoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProcessoMapper.class);
    }

    @Nested
    @DisplayName("toDto")
    class ToDtoTests {
        @Test
        @DisplayName("Deve mapear entidade para DTO com todos os campos")
        void deveMapearEntidadeParaDtoCompleto() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDescricao("Mapeamento 2026");
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setDataCriacao(LocalDateTime.now());
            processo.setDataLimite(LocalDateTime.now().plusDays(30));

            ProcessoDto dto = mapper.toDto(processo);

            assertThat(dto).isNotNull();
            assertThat(dto.getCodigo()).isEqualTo(1L);
            assertThat(dto.getDescricao()).isEqualTo("Mapeamento 2026");
            assertThat(dto.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO.name());
            assertThat(dto.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO.name());
        }

        @Test
        @DisplayName("Deve mapear unidades participantes para string de siglas")
        void deveMapearUnidadesParticipantes() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDescricao("Teste");
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe()
                    .comCodigo("1")
                    .comSigla("SEDIA")
                    .comNome("Unidade 1")
                    .build();

            Unidade unidade2 = UnidadeTestBuilder.umaDe()
                    .comCodigo("2")
                    .comSigla("COSIS")
                    .comNome("Unidade 2")
                    .build();

            // Usando adicionarParticipantes criará os snapshots UnidadeProcesso
            processo.adicionarParticipantes(Set.of(unidade1, unidade2));

            ProcessoDto dto = mapper.toDto(processo);

            assertThat(dto.getUnidadesParticipantes()).isNotNull();
            assertThat(dto.getUnidadesParticipantes()).contains("SEDIA");
            assertThat(dto.getUnidadesParticipantes()).contains("COSIS");
        }

        @Test
        @DisplayName("Deve lidar com participantes null")
        void deveLidarComParticipantesNull() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDescricao("Teste");
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setParticipantes(null);

            ProcessoDto dto = mapper.toDto(processo);

            assertThat(dto).isNotNull();
            assertThat(dto.getUnidadesParticipantes()).isNullOrEmpty();
        }

        @Test
        @DisplayName("Deve lidar com participantes vazios")
        void deveLidarComParticipantesVazio() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDescricao("Teste");
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setParticipantes(new ArrayList<>());

            ProcessoDto dto = mapper.toDto(processo);

            assertThat(dto).isNotNull();
            assertThat(dto.getUnidadesParticipantes()).isEmpty();
        }

        @Test
        @DisplayName("Deve ordenar siglas das unidades alfabeticamente")
        void deveOrdenarSiglasAlfabeticamente() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDescricao("Teste");
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidadeZ = UnidadeTestBuilder.umaDe()
                    .comCodigo("1")
                    .comSigla("ZEBRA")
                    .comNome("Unidade Zebra")
                    .build();

            Unidade unidadeA = UnidadeTestBuilder.umaDe()
                    .comCodigo("2")
                    .comSigla("ALFA")
                    .comNome("Unidade Alfa")
                    .build();

            Unidade unidadeM = UnidadeTestBuilder.umaDe()
                    .comCodigo("3")
                    .comSigla("MEGA")
                    .comNome("Unidade Mega")
                    .build();

            processo.adicionarParticipantes(Set.of(unidadeZ, unidadeA, unidadeM));

            ProcessoDto dto = mapper.toDto(processo);

            assertThat(dto.getUnidadesParticipantes()).isEqualTo("ALFA, MEGA, ZEBRA");
        }

        @Test
        @DisplayName("Deve lidar com datas, situação e tipo null")
        void deveLidarComCamposNull() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setDataCriacao(null);
            processo.setDataFinalizacao(null);
            processo.setDataLimite(null);
            processo.setSituacao(null);
            processo.setTipo(null);

            ProcessoDto dto = mapper.toDto(processo);

            assertThat(dto).isNotNull();
            assertThat(dto.getDataCriacao()).isNull();
            assertThat(dto.getDataFinalizacao()).isNull();
            assertThat(dto.getDataLimite()).isNull();
            assertThat(dto.getSituacao()).isNull();
            assertThat(dto.getTipo()).isNull();
        }

        @Test
        @DisplayName("Deve cobrir branches de tipo nulo no toDto")
        void deveCobrirTipoNuloNoToDto() {
            Processo entity = new Processo();
            entity.setCodigo(1L);
            entity.setTipo(null);

            ProcessoDto dto = mapper.toDto(entity);
            assertThat(dto).isNotNull();
            assertThat(dto.getTipo()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {
        @Test
        @DisplayName("Deve mapear DTO para entidade")
        void deveMapearDtoParaEntidade() {
            ProcessoDto dto = ProcessoDto.builder()
                    .codigo(1L)
                    .descricao("Mapeamento 2026")
                    .tipo(TipoProcesso.MAPEAMENTO.name())
                    .situacao(SituacaoProcesso.CRIADO.name())
                    .build();

            Processo processo = mapper.toEntity(dto);

            assertThat(processo).isNotNull();
            assertThat(processo.getCodigo()).isEqualTo(1L);
            assertThat(processo.getDescricao()).isEqualTo("Mapeamento 2026");
            assertThat(processo.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO);
            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
        }

        @Test
        @DisplayName("Deve cobrir branches de tipo nulo no toEntity")
        void deveCobrirTipoNuloNoToEntity() {
            ProcessoDto dto = ProcessoDto.builder()
                    .codigo(1L)
                    .tipo(null)
                    .build();

            Processo entity = mapper.toEntity(dto);
            assertThat(entity).isNotNull();
            assertThat(entity.getTipo()).isNull();
        }

        @Test
        @DisplayName("Deve ignorar participantes no mapeamento de DTO para entidade")
        void deveIgnorarParticipantes() {
            ProcessoDto dto = ProcessoDto.builder()
                    .codigo(1L)
                    .descricao("Mapeamento 2026")
                    .tipo(TipoProcesso.MAPEAMENTO.name())
                    .situacao(SituacaoProcesso.CRIADO.name())
                    .unidadesParticipantes("SEDIA, COSIS")
                    .build();

            Processo processo = mapper.toEntity(dto);

            assertThat(processo).isNotNull();
            assertThat(processo.getParticipantes()).isEmpty();
        }
    }
}
