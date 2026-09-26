# Condo App

Aplicação white-label privada para moradores de condomínios, construída com
Kotlin Multiplatform e Compose Multiplatform para Android, iOS e desktop.

## Status real

Em desenvolvimento. As nove telas de referência e seus fluxos auxiliares
usam repositórios demonstrativos persistentes executados dentro do app.
**Não existe backend próprio. Nenhum fluxo foi integrado à API real.**
A documentação, autenticação e o ambiente de homologação externos ainda
precisam ser fornecidos. O build de produção exclui o módulo demonstrativo
e apresenta a indisponibilidade da integração; não faz fallback para dados fictícios.
Consulte [validações](docs/VALIDATION.md) e [pendências](docs/PROGRESS.md).

## Recursos

- Login, recuperação, primeiro acesso por convite e logout.
- Condomínios vinculados e troca de contexto sem manter dados da unidade anterior.
- Encomendas, indicadores dos últimos 30 dias, histórico e retirada por QR Code.
- Distinção entre retirada informada pelo morador e evento simulado de locker.
- Câmeras com filtros, permissões e disponibilidade demonstrativa explícita.
- Visitantes, prestadores, convites com validade, edição, revogação e uso único.
- Pets, vacinação, cadastro, edição e alertas de perdidos e encontrados.
- Reservas, conflitos demonstrativos, cancelamento e agenda de eventos.
- Perfil, moradores, veículos, preferências, privacidade e suporte.
- Avisos, notificações lidas/não lidas, solicitações e ocorrências.
- Duas marcas configuráveis, com módulos por cliente e por condomínio.

## Pré-requisitos

JDK 21, Python 3, acesso inicial à internet e Git. Para Android: SDK 37.0 (compileSdk 37),
build-tools 36 e `ANDROID_HOME` ou `local.properties` com `sdk.dir`.
Para iOS: macOS, Xcode e XcodeGen (`brew install xcodegen`).
O catálogo `gradle/libs.versions.toml` centraliza Kotlin 2.4.20, Compose 1.12.0,
AGP 9.2.0, Ktor Client 3.5.2 e bibliotecas. Gradle está fixado em 9.4.1.
As versões foram conferidas nas documentações oficiais, com links em
[arquitetura](docs/ARCHITECTURE.md). Resultados com o Xcode local constam em validações.

Os launchers verificam o SHA-256 da distribuição Gradle e das fontes Manrope/Sora.
JAR do wrapper e fontes binárias não são versionados, respeitando a regra de commits.
Os assets são obtidos automaticamente na primeira execução.

## Executar

Desktop (macOS, Windows e Linux):

```sh
./gradlew :app:run
# Windows: gradlew.bat :app:run
```

Android, com emulador ou dispositivo conectado:

```sh
./gradlew :androidApp:installDebug
adb shell am start -n app.condo.resident/app.condo.android.MainActivity
```

Para gerar apenas o APK, use `./gradlew :androidApp:assembleDebug`.
A saída fica em `androidApp/build/outputs/apk/debug/`.

Para iOS:

```sh
xcodegen generate --spec iosApp/project.yml
open iosApp/CondoApp.xcodeproj
```

Selecione o esquema CondoApp e um simulador iPhone/iPad no Xcode e execute.
O script da target compila e incorpora o framework compartilhado.
Build sem assinatura para simulador:

```sh
xcodebuild -project iosApp/CondoApp.xcodeproj -scheme CondoApp \
  -sdk iphonesimulator -configuration Debug CODE_SIGNING_ALLOWED=NO build
```

## Testar a demonstração

Use **Entrar na demonstração**, ou `alex@condo.demo` / `Demo1234!`.
A segunda conta fictícia é `bia@condo.demo`, com a mesma senha.
O CPF fictício `00000000000` é apenas um identificador alternativo de Alex.
Convites demonstrativos: `PRIMEIRO-DEMO` para ativação e `VINCULAR-DEMO` para outro condomínio.
A alteração de senha é válida somente durante a execução demonstrativa.

1. Abra uma encomenda e gere o código. O QR é decodificável e tem validade curta.
2. Use **Já retirei**: o estado fica aguardando confirmação física.
3. Use **Simular leitura no locker** ou **Simular evento de retirada** para um evento fictício.
4. Cadastre uma visita; gere e compartilhe o convite; simule uso único ou revogue.
5. Reserve um espaço; tente o mesmo horário; confira o histórico e cancele.
6. Cadastre um pet, veículo ou solicitação. Reinicie o app para conferir persistência.
7. Troque de condomínio ou conta: os registros são isolados.
8. Toque na faixa **Demonstração** para cenários de vazio, erro, expiração e acesso negado.

Dados demonstrativos são armazenados localmente e não acionam hardware ou terceiros.
No desktop ficam em `~/.condo-app/<marca>/<ambiente>/`; no Android/iOS, no armazenamento do app.
Logout apaga a credencial e todo o estado de apresentação; os registros fictícios
permanecem no armazenamento demonstrativo, separados por conta e condomínio.

## Testes e builds

```sh
./gradlew :domain:desktopTest :demo:desktopTest :app:desktopTest
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:connectedDebugAndroidTest
./gradlew :app:linkDebugFrameworkIosSimulatorArm64
./gradlew :app:linkReleaseFrameworkIosArm64
python3 scripts/check-history.py
```

Testes Compose geram capturas em `app/build/validation/`.
A CI verifica lógica, transporte HTTP com respostas controladas, UI, Android,
frameworks Apple e compilação Windows. A execução em cada runner depende do próprio ambiente.

## White-label e ambientes

```sh
./gradlew -Pbrand=viva :app:run
./gradlew -Pbrand=viva :androidApp:assembleDebug
./gradlew -PappEnvironment=production :app:run
```

`condo` mantém a paleta da referência; `viva` usa azul e desabilita câmeras.
Marca de distribuição e condomínios vinculados são conceitos separados.
Veja [como configurar marca, módulos e ambiente](docs/ARCHITECTURE.md).
Veja [o que falta para integrar a API](docs/API-INTEGRATION.md).
Não há endpoints nem credenciais de servidor inventados.

## Distribuição

`./gradlew :app:packageDistributionForCurrentOS` gera o pacote desktop no host compatível.
DMG, MSI e DEB exigem o respectivo sistema e ferramentas de empacotamento.
Android release: `:androidApp:bundleRelease`; configurar assinatura com segredos de CI,
fora do repositório. iOS físico/TestFlight exige conta Apple, Team e provisioning profiles.
macOS distribuído exige assinatura Developer ID e notarização.
Nenhuma assinatura de distribuição ou publicação em loja está configurada.
Inclua os avisos de bibliotecas e das fontes em qualquer distribuição.

## Histórico de commits

`git config core.hooksPath .githooks` ativa a verificação de staging após clonar.
Cada commit desta implementação altera exatamente um arquivo, com no máximo 20
linhas adicionadas + removidas. Arquivos são construídos incrementalmente.
`scripts/commit-file.py caminho 'feat: descrição específica'` auxilia o processo.
O histórico inicial do README é preservado como baseline da auditoria.

## Licença

Software privado e proprietário. Nenhuma licença open-source é concedida para este projeto.
As dependências e fontes mantêm suas próprias licenças; consulte [terceiros](docs/THIRD-PARTY.md).
