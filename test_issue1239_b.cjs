const { readFileSync } = require('fs');
const content = readFileSync('frontend/src/components/processo/ProcessoFormFields.vue', 'utf8');
if (content.includes('Data limite')) {
    console.log('Found "Data limite" in ProcessoFormFields.vue');
}
