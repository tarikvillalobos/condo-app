# Estado de implementação

- [x] Inspeção do repositório e extração das nove telas HTML.
- [x] Alteração de escopo: somente aplicativos, sem backend próprio.
- [x] Regra automatizada de um arquivo e até 20 linhas por commit.
- [x] Versões verificadas nas documentações oficiais.
- [x] Domínio, repositórios demonstrativos e persistência isolada.
- [x] Nove telas Compose e fluxos auxiliares implementados.
- [x] Adaptadores de plataforma e cliente HTTP sem endpoints inventados.
- [x] 31 testes desktop aprovados: 4 domínio, 9 demo e 18 app.
- [x] Android debug compilado; 2 testes instrumentais aprovados em tablet e repetidos com aprovação no celular nativo 390 × 844 a 160 dpi.
- [x] Framework iOS de simulador e host Swift compilados; 1 XCTest aprovado no iPhone 17 Pro com iOS 26.2; `.app` macOS gerada.
- [x] 66 capturas responsivas geradas e revisão visual de casos representativos.
- [x] Documentação de comandos, evidências e limitações em [VALIDATION](VALIDATION.md).
- [x] Revalidar cabeçalho a 320 e calendário com fonte 200% após as correções.
- [x] Falha de credenciais Android resolvida com transferência de foco Compose antes de fechar o teclado; 2 testes revalidados no celular.
- [x] Encerramento iOS resolvido com Info.plist explícito; fluxo de UI revalidado no iPhone.
- [x] Ambiente de produção: 7 testes e Android release aprovados; DEX sem classes demo e com repositório indisponível.
- [x] Framework iOS físico release compilado.
- [x] Marca Viva: APK e teste de renderização aprovados, 58 capturas geradas; início azul e bloqueio de câmeras conferidos.
- [x] XCTest aprovado e capturas de início/detalhes revisadas em simulador iPad Pro 11 (M5).
- [ ] Validar execução/distribuição nativa Windows e Linux e distribuição assinada móvel/macOS.
- [ ] Auditar leitores de tela, dispositivos físicos e condições nativas não cobertas pelas capturas.
- [ ] Contrato da API externa: solicitado ao usuário; não disponível.
- [ ] Integração real e homologação: dependem do contrato e do ambiente.

O modo demonstrativo não representa autenticação, autorização, retirada
física ou confirmação de disponibilidade por um servidor real.
Biometria real, push remoto e vídeo também dependem das integrações externas.
Compilações aprovadas e capturas desktop não significam homologação multiplataforma
ou prontidão para produção. Estado registrado em 26/09/2026; novas validações
devem ser registradas em VALIDATION com plataforma e alcance reais.
