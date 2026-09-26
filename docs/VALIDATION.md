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
