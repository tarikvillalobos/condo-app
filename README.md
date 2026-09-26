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

JDK 21, Python 3, acesso inicial à internet e Git. Para Android: SDK 36,
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


Private and proprietary software.
