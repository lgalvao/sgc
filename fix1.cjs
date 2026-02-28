const fs = require('fs');

let file1241 = 'frontend/src/views/processo/ProcessoDetalheView.vue';
let content1241 = fs.readFileSync(file1241, 'utf8');

// Issue 1241: ADM "Ao acessar o subprocesso o botão Finalizar processo só deve está habilitado quando todos os subprocessos foram homologados."
// Wait, is it Processo or Subprocesso?
// "Ao acessar o subprocesso o botão Finalizar processo..." wait, there's no Finalizar processo in Subprocesso.
// Oh, the issue says "Ao acessar o processo o botão Finalizar processo só deve está habilitado quando todos os subprocessos foram homologados."
// Let's re-read the issue.
