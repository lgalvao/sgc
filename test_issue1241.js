const { readFileSync } = require('fs');
const content = readFileSync('frontend/src/views/processo/ProcessoDetalheView.vue', 'utf8');
if (content.includes('ProcessoAcoes')) {
    console.log('ProcessoAcoes is in ProcessoDetalheView.vue');
}
