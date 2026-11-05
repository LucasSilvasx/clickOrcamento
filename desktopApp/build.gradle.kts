import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

val nomeApp = "ClickOrcamento"
val versaoApp = "1.0.0"
val fabricante = "Lucas Gabriel"

compose.desktop {
    application {
        mainClass = "com.clickorcamento.app.MainKt"

        nativeDistributions {
            // Sem Msi aqui: o instalador do Windows sai da task gerarMsi, logo abaixo.
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb)
            packageName = nomeApp
            packageVersion = versaoApp
            vendor = fabricante

            // O instalador leva um Java reduzido; estes módulos vêm de suggestRuntimeModules.
            modules("java.instrument", "jdk.unsupported")

            windows {
                iconFile.set(project.file("icones/app.ico"))
            }
        }
    }
}

// --- Instalador do Windows ---

// O packageMsi do Compose esvazia a própria pasta de recursos antes de chamar o jpackage, e o
// argumento dele vem depois de qualquer freeArgs. Sem acesso a essa pasta não há como trocar os
// modelos do instalador, então esta task chama o jpackage direto sobre o app já montado.
tasks.register<Exec>("gerarMsi") {
    group = "compose desktop"
    description = "Gera o instalador .msi do Windows."
    dependsOn("createDistributable", ":unzipWix")

    val appMontado = layout.buildDirectory.dir("compose/binaries/main/app/$nomeApp")
    val modelos = layout.projectDirectory.dir("instalador")
    val destino = layout.buildDirectory.dir("instalador")
    val wix = rootProject.layout.buildDirectory.dir("wix311")

    inputs.dir(appMontado)
    inputs.dir(modelos)
    outputs.dir(destino)

    // O jpackage procura o WiX (candle e light) no PATH.
    environment("PATH", "${wix.get().asFile};${providers.environmentVariable("PATH").get()}")

    commandLine(
        "${providers.systemProperty("java.home").get()}/bin/jpackage.exe",
        "--type", "msi",
        "--app-image", appMontado.get().asFile.path,
        "--resource-dir", modelos.asFile.path,
        "--dest", destino.get().asFile.path,
        "--name", nomeApp,
        "--app-version", versaoApp,
        "--vendor", fabricante,
        // Instala para todos os usuários, em "Arquivos de Programas\ClickOrcamento" (pede permissão de administrador).
        "--install-dir", nomeApp,
        // Mostra o assistente: a tela de pasta e a tela final, onde fica a caixa de abrir o app.
        "--win-dir-chooser",
        "--win-shortcut",
        "--win-menu",
        "--win-menu-group", "clickOrçamento",
        // Identifica o programa entre versões: trocar faria o Windows instalar outra cópia em vez de atualizar.
        "--win-upgrade-uuid", "5145b9f2-19bb-4412-b66f-03d15fd1a1d8",
    )
}
