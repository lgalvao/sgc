#!/usr/bin/env node
process.env.SGC_RUNTIME_TS = "sim";
const {principal} = await import("./sgc.js");
await principal(process.argv);
