const { readFileSync } = require('fs');
const files = [
  'frontend/src/views/processo/CriarProcessoView.vue',
  'frontend/src/components/processo/ProcessoForm.vue',
  'frontend/src/views/processo/EditarProcessoView.vue'
];

for (const f of files) {
  try {
    const content = readFileSync(f, 'utf8');
    if (content.includes('Data limite') || content.includes('dataLimite')) {
      console.log(`Found data limite in ${f}`);
    }
  } catch (e) {}
}
