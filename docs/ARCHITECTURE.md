# Arquitetura e decisões

## Módulos

- `domain`: entidades, validações, contratos, comandos e relógio testável; sem Compose, HTTP ou banco.
- `demo`: implementação local dos contratos, fixtures, DTOs serializáveis e mapeamento.
- `app`: apresentação por funcionalidades, design system, transporte HTTP e adaptadores nativos.
- `androidApp`: Activity e configuração de distribuição Android.
- `iosApp`: host SwiftUI e especificação XcodeGen reproduzível.

A injeção é por construtor no AppController. A factory de ambiente é gerada pelo
Gradle e escolhe a implementação de repositório em tempo de build.
`-PappEnvironment=production` ou `staging` nem inclui `:demo` em settings.
O repositório indisponível falha explicitamente até existir um adaptador da API documentada.
Não há servidor, banco central, migrações de backend ou hospedagem neste repositório.

AppState é imutável, exposto por StateFlow. Composables emitem ações ao controller;
formulários são interpretados fora das telas. Um snapshot atualizado alimenta
Início, detalhes, contadores e listas. Comandos demonstrativos usam Mutex e
persistem um documento completo após validação; falhas não publicam sucesso.
