// Em modo development, o Kotlin/Wasm já desliga config.devtool (igual
// produção), mas empurra manualmente um EvalSourceMapDevToolPlugin:
// é ESSE plugin, não o devtool, quem embrulha cada módulo em eval("...").
// O glue gerado pelo próprio plugin Kotlin/Wasm
// (clickOrcamento-webApp.import-object.mjs) usa import.meta.url, e
// import.meta dentro de um eval() é sempre SyntaxError no navegador,
// independente do type do <script>. Em produção o Kotlin usa o
// SourceMapDevToolPlugin (sem eval) em vez do Eval*. Aqui trocamos um
// pelo outro só no modo development, sem mexer em produção.
if (config.mode !== 'production') {
    const webpack = require('webpack');

    config.plugins = config.plugins.filter(
        (p) => p.constructor.name !== 'EvalSourceMapDevToolPlugin'
    );

    config.plugins.push(new webpack.SourceMapDevToolPlugin({
        module: true,
        columns: true,
        noSources: false,
        ignoreList: /NATIVE_IMPLEMENTATIONS.kt/,
    }));
}
