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
