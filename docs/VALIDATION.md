# Validação executada e limitações

Registro de 26/09/2026, no ambiente local macOS. O aplicativo permanece em
desenvolvimento: a demonstração é local e nenhuma operação foi homologada com
API, condomínio, portaria ou locker reais. Compilar ou aprovar testes locais
não equivale a liberar uma versão para produção.

## Evidências confirmadas

| Verificação | Resultado observado | Alcance |
| --- | --- | --- |
| Testes de domínio | 4 aprovados | Métricas, conflitos de horários, validade de convites e validação de entrada. |
| Repositório demonstrativo | 9 aprovados | Isolamento, persistência, permissões, reservas, códigos, eventos e cenários locais. |
| Testes do app no desktop | 18 aprovados | 8 controller, 5 HTTP, 1 integração indisponível, 1 QR, 1 renderização e 2 fluxos de UI. |
| Capturas Compose desktop Condo | 66 PNGs gerados e casos representativos revisados | Inclui revalidação do cabeçalho a 320 e do calendário com fonte 200%. |
| Android debug | `:androidApp:assembleDebug` aprovado | Geração do APK; não é distribuição assinada para loja. |
| Android em configuração de tablet | 2 testes instrumentais aprovados | Credenciais/retirada informada e formulário de visita após recriação da Activity. |
| Android em configuração de celular | 2 testes instrumentais aprovados | AVD com resolução nativa 390 × 844 e densidade 160 dpi, serial `emulator-5592`. |
| Distribuição macOS | `:app:createDistributable` aprovado | Aplicativo `.app` gerado; sem assinatura Developer ID ou notarização validadas. |
| Inicialização macOS | Executável do pacote iniciou sem erros no log | Smoke test de processo; não é uma auditoria manual completa da UI nativa. |
| Compilação Windows na CI | `:app:desktopJar` aprovado | Runner Windows; execução da interface e instalador MSI ainda não validados. |
| Linux e Android na CI | Testes desktop e builds Android aprovados | Ubuntu com Xvfb: demo, produção e compilação da marca Viva; não valida instalador DEB. |
| Framework iOS de simulador | Debug compilado | Target `iosSimulatorArm64`. |
| Host Swift e UI iPhone | Compilação e 1 XCTest aprovados | iPhone 17 Pro, iOS 26.2: login, encomenda e troca de condomínio. |
| UI iPad | 1 XCTest aprovado e capturas revisadas | iPad Pro 11 (M5), iOS 26.2: login, encomenda e troca de condomínio. |
| Framework iOS físico | Release compilado | Target `iosArm64`; execução física e assinatura pendentes. |
| Produção | 7 testes e Android release aprovados | Inspeção do DEX do APK confirma ausência de `Lapp/condo/demo` e presença de `UnavailableRepository`. |
| Marca Viva | APK debug e teste de renderização aprovados | 58 PNGs; navegação para câmeras bloqueada; início azul e sem módulo de câmeras revisado. |

São **31 testes aprovados** somando domínio, demo e app nas execuções desktop
registradas. Android executou os mesmos 2 casos em tablet e celular; iPhone
e iPad aprovaram 1 XCTest cada, e produção aprovou os 7 testes aplicáveis ao seu ambiente.
Os XML consultados não registram falhas, erros ou testes ignorados nessas
suítes desktop. Os relatórios são artefatos locais, sobrescritos em novas execuções:

- `domain/build/test-results/desktopTest/` e `demo/build/test-results/desktopTest/`.
- `app/build/test-results/desktopTest/` e `app/build/reports/tests/desktopTest/`.
- `androidApp/build/outputs/androidTest-results/connected/` para a execução Android mais recente.
- `app/build/validation/` para as capturas; os PNGs não são versionados.

Logs locais desta rodada: `/tmp/condo-final-tests.log` (desktop e Android celular),
`/tmp/condo-ios-iphone-v4.log` (XCTest), `/tmp/condo-production.log` e
`/tmp/condo-viva-final.log` e `/tmp/condo-ios-ipad.log`. Capturas nativas foram
preservadas em `.generated/validation/ios-iphone/` e `ios-ipad/`.
Esses artefatos locais e logs temporários não são versionados.

## Cobertura funcional

As regressões do controller verificam troca de condomínio e limpeza de estado,
invalidação de códigos após falha, logout durante autenticação atrasada,
logout consecutivo seguido de novo login, navegação durante ações, nova reserva
após cancelamento e ausência de duplicação de veículo após navegação.
As operações de cofre usam dispatcher injetável nos testes; isso não valida
interações reais com todos os cofres nativos.

O transporte HTTP usa respostas controladas para validar cancelamento,
classificação de sessão expirada/acesso negado, rejeição de conexão sem TLS
e tratamento de corpo inválido. Não existe contrato externo validado nesses
testes. `UnavailableRepositoryTest` verifica a falha explícita da integração.

O teste de QR codifica e decodifica o payload demonstrativo. Os testes de UI
desktop cobrem entrada por credenciais com Tab/Enter e o fluxo de encomenda
com retirada informada pelo morador, sem afirmar retirada física.

## Matriz visual

O teste usa `ImageComposeScene`, densidade 1 e estas larguras: **320, 390, 430,
600, 840 e 1200**, com altura 844. Nessas condições, os valores correspondem
a pixels e dp. Em cada largura são renderizadas nove telas: login, início,
lista e detalhe de encomenda, câmeras, visitas, pets, reservas e perfil.

As 12 capturas adicionais cobrem:

- Fonte em 200% a 390 × 844: login, início, perfil e reservas.
- Reservas em paisagem a 844 × 390.
- Formulário de visita com texto longo a 390 e 1200 de largura.
- Listas vazias de encomendas e câmeras.
- 60 encomendas a 320 e 1200; 12 câmeras a 1200.

Capturas representativas foram inspecionadas visualmente. A revisão encontrou
problemas no cabeçalho a 320 e no calendário com fonte em 200%; as correções
foram renderizadas novamente e conferidas: títulos estreitos ocupam linha própria
e os controles de mês/data se reorganizam com fonte ampliada. O teste automático verifica dimensões e geração de imagens;
não faz comparação com imagens de referência nem prova ausência de recortes.

A variante Viva gerou 58 capturas no teste adaptado aos módulos da marca.
Foi verificado o bloqueio da navegação para câmeras e revisada a tela inicial
com a paleta azul e sem esse módulo. Os resultados não são uma cópia da matriz
Condo: as telas de câmeras não fazem parte da configuração Viva.

Essa matriz não substitui testes nativos de teclado virtual, áreas seguras,
rotação, dobradiça de aparelho dobrável ou leitores de tela. A orientação em
paisagem foi renderizada em uma tela, sem cobrir todos os fluxos nessa orientação.

## Reproduzir testes e capturas

Na raiz do projeto, com os pré-requisitos do [README](../README.md):

```sh
./gradlew :domain:desktopTest :demo:desktopTest :app:desktopTest
./gradlew :app:desktopTest --tests app.condo.ResponsiveRenderTest --rerun-tasks
./gradlew :app:desktopTest --tests app.condo.ResidentFlowTest --rerun-tasks
./gradlew :app:createDistributable
```

Os testes de UI desktop precisam de sessão gráfica. `createDistributable`
produz a imagem de aplicação do host em `app/build/compose/binaries/main/app/`.
Esses resultados macOS não validam empacotamento ou execução nativa Windows/Linux.

### Android

```sh
./gradlew :androidApp:assembleDebug
emulator -list-avds
# Inicie um AVD de teste com resolução nativa 390x844 e densidade 160 dpi.
adb devices
# Defina CONDO_ANDROID_SERIAL com o serial de um emulador de teste listado acima.
adb -s "${CONDO_ANDROID_SERIAL:?Defina o serial do emulador}" shell wm size
adb -s "$CONDO_ANDROID_SERIAL" shell wm density
ANDROID_SERIAL="$CONDO_ANDROID_SERIAL" ./gradlew :androidApp:connectedDebugAndroidTest
```

A execução final em celular usou o AVD `CondoPhone`, serial `emulator-5592`,
com **390 × 844 pixels nativos e densidade 160 dpi**, equivalentes a 390 × 844 dp.
Os 2 testes passaram após transferir o foco Compose ao botão antes de fechar
o teclado virtual. A falha anterior do fluxo de credenciais foi resolvida e
retestada nessa configuração.

A configuração de tablet também aprovada foi **1024 × 600 pixels, densidade
120 dpi**, equivalente a aproximadamente **1365 × 800 dp**, com 2 testes aprovados.
O teste de formulário também recria a Activity e verifica preservação do rascunho.
O teste instrumental limpa as preferências demonstrativas do aplicativo antes
de cada caso; use emulador destinado à validação.

Para reproduzir o celular, prefira um perfil de AVD com essas dimensões nativas.
Uma redução por `wm size` sobre o AVD de tablet IHM não representa a mesma
configuração usada na execução final aprovada.

### iOS

```sh
./gradlew :app:linkDebugFrameworkIosSimulatorArm64
./gradlew :app:linkReleaseFrameworkIosArm64
xcodegen generate --spec iosApp/project.yml
xcodebuild -project iosApp/CondoApp.xcodeproj -scheme CondoApp \
  -sdk iphonesimulator -configuration Debug -derivedDataPath iosApp/build/DerivedData \
  CODE_SIGNING_ALLOWED=NO build
xcrun simctl list devices available
# Defina CONDO_IOS_SIMULATOR com o UDID de um simulador de teste disponível.
xcodebuild -project iosApp/CondoApp.xcodeproj -scheme CondoApp \
  -destination "platform=iOS Simulator,id=${CONDO_IOS_SIMULATOR:?Defina o UDID}" \
  -configuration Debug -derivedDataPath iosApp/build/DerivedData \
  CODE_SIGNING_ALLOWED=NO test
```

Use simulador com dados demonstrativos limpos. No iPhone 17 Pro com iOS 26.2,
o teste de login, encomenda e troca de condomínio passou, anexando screenshots
ao resultado XCTest. O encerramento anterior foi resolvido com o `Info.plist`
explícito e a chave `CADisableMinimumFrameDurationOnPhone` como booleano `true`.
O framework `iosArm64` release também compilou, em 4min04s; isso não comprova
execução em hardware real. O mesmo XCTest passou no iPad Pro 11 (M5), iOS 26.2.
Capturas de início e detalhes foram revisadas nos dois simuladores: iPhone
1206 × 2622 pixels; iPad 1668 × 2420 pixels. Uma captura do iPad inclui um
banner do sistema Apple Intelligence; isso não faz parte da interface do app.
Não houve homologação em dispositivo iOS físico,
TestFlight ou distribuição assinada. O Xcode local é 26.2; a diferença para a
referência de compatibilidade 26.4 está registrada em [arquitetura](ARCHITECTURE.md).

## Marca Viva e ambiente de produção

```sh
./gradlew -Pbrand=viva :app:desktopTest --tests app.condo.ResponsiveRenderTest :androidApp:assembleDebug
./gradlew -PappEnvironment=production :app:desktopTest :androidApp:assembleRelease
```

Os dois comandos foram aprovados. Viva gerou APK e 58 capturas; produção gerou
APK release e aprovou os sete testes aplicáveis. A inspeção do DEX confirmou
ausência das classes do módulo demo e presença do repositório indisponível.
O ambiente de produção continua falhando explicitamente enquanto faltar a API;
esse resultado confirma isolamento da demonstração, não integração real.
As verificações de CI configuradas só contam como evidência após execução com
resultado disponível; a existência do workflow não prova aprovação.

## Próximas verificações

- Validar plataformas, dispositivos físicos e distribuição assinada descritos abaixo.
- Integrar e homologar os fluxos reais após disponibilização do contrato externo.

## Limites conhecidos

- Windows e Linux não foram validados em execução ou distribuição nativa.
- Windows ainda não possui adaptador de cofre: Manter conectado fica indisponível; não há credencial em texto puro.
- Leitores de tela não foram auditados; Tab/Enter e escala de fonte são cobertura parcial.
- As permissões de Computer Use não estavam disponíveis; não houve inspeção por esse recurso.
- Biometria real, push remoto, vídeo/câmeras e operações de portaria/locker aguardam integração.
- Autenticação, autorização, expiração e cache de dados reais dependem do contrato da API externa.
- Assinaturas, notarização, lojas e ambientes de homologação não foram validados.

Nenhum APK, `.app`, framework, screenshot ou outro binário faz parte desta
documentação versionada. Consulte [o progresso](PROGRESS.md) e
[as dependências de integração](API-INTEGRATION.md) antes de planejar distribuição.
