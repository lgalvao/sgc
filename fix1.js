const fs = require('fs');

let file1241 = 'frontend/src/views/processo/ProcessoDetalheView.vue';
let content1241 = fs.readFileSync(file1241, 'utf8');
content1241 = content1241.replace('podeAceitarBloco.value || podeHomologarBloco.value || podeDisponibilizarBloco.value', 'podeAceitarBloco.value || podeDisponibilizarBloco.value');
fs.writeFileSync(file1241, content1241);

let comp1241 = 'frontend/src/components/processo/ProcessoAcoes.vue';
let contentComp1241 = fs.readFileSync(comp1241, 'utf8');
contentComp1241 = contentComp1241.replace('podeAceitarBloco || podeHomologarBloco', 'podeAceitarBloco');
contentComp1241 = contentComp1241.replace(/<BButton[^>]+v-if="podeHomologarBloco"[\s\S]*?<\/BButton>/, '');
fs.writeFileSync(comp1241, contentComp1241);

let file1240 = 'frontend/src/services/subprocessoService.ts';
let content1240 = fs.readFileSync(file1240, 'utf8');
content1240 = content1240.replace('totalAtividadesInseridas: data.totalInseridas || 0,', 'totalAtividadesInseridas: data.totalInseridas || 0,'); // keep same
fs.writeFileSync(file1240, content1240);

let file1239 = 'frontend/src/components/processo/ProcessoFormFields.vue';
let content1239 = fs.readFileSync(file1239, 'utf8');
fs.writeFileSync(file1239, content1239);
