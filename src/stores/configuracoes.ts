import {defineStore} from 'pinia';

interface ConfiguracoesState {
  diasInativacaoProcesso: number;
  diasAlertaNovo: number;
}

export const useConfiguracoesStore = defineStore('configuracoes', {
  state: (): ConfiguracoesState => ({
    diasInativacaoProcesso: 30, // Valor padrão
    diasAlertaNovo: 7, // Valor padrão
  }),
  actions: {
    loadConfiguracoes() {
      try {
        const savedConfig = localStorage.getItem('appConfiguracoes');
        if (savedConfig) {
          const parsedConfig = JSON.parse(savedConfig);
          this.diasInativacaoProcesso = parsedConfig.diasInativacaoProcesso || this.diasInativacaoProcesso;
          this.diasAlertaNovo = parsedConfig.diasAlertaNovo || this.diasAlertaNovo;
        }
      } catch (e) {
        console.error('Erro ao carregar configurações do localStorage:', e);
      }
    },
    saveConfiguracoes() {
      try {
        const configToSave = {
          diasInativacaoProcesso: this.diasInativacaoProcesso,
          diasAlertaNovo: this.diasAlertaNovo,
        };
        localStorage.setItem('appConfiguracoes', JSON.stringify(configToSave));
        
        return true;
      } catch (e) {
        console.error('Erro ao salvar configurações no localStorage:', e);
        return false;
      }
    },
    setDiasInativacaoProcesso(dias: number) {
      if (dias >= 1) {
        this.diasInativacaoProcesso = dias;
      } else {
        console.warn('DIAS_INATIVACAO_PROCESSO deve ser 1 ou mais.');
      }
    },
    setDiasAlertaNovo(dias: number) {
      if (dias >= 1) { // Assumindo que deve ser 1 ou mais, como DIAS_INATIVACAO_PROCESSO
        this.diasAlertaNovo = dias;
      } else {
        console.warn('DIAS_ALERTA_NOVO deve ser 1 ou mais.');
      }
    },
  },
});
