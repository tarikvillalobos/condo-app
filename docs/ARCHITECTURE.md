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
Trocar contexto cancela carregamento e limpa imediatamente snapshot, códigos,
filtros e rascunhos. Respostas antigas são descartadas por versão do contexto.
Rotas e atalhos consultam módulos da marca e permissões do condomínio.
Isso orienta a UI; a autorização definitiva continua sendo responsabilidade da API.

## Persistência e credenciais

DemoDocument versão 1 é um formato privado de cache demonstrativo, não um DTO da API.
DemoRow mapeia entidades explicitamente, sem introduzir dependência de serialização no domínio.
As chaves incluem marca/ambiente no adaptador e conta/condomínio no repositório.
Desktop grava arquivos por substituição atômica. Android usa preferências privadas,
e iOS usa um domínio específico de NSUserDefaults para os registros fictícios.
Dados reais exigirão a política de cache definida pelo contrato e análise de sensibilidade.

Credenciais: Android Keystore/AES-GCM, iOS Keychain device-only, macOS Keychain,
Linux Secret Service quando instalado. Ausência de cofre desabilita Manter conectado;
não há fallback de credenciais em texto puro. Leia VALIDATION para plataformas efetivamente testadas.
A referência de sessão demo expira em sete dias e nunca é uma credencial de API.
Renovação, revogação remota e login biométrico aguardam contrato externo.
Preferência de push, permissão do sistema e serviço de push são apresentados separadamente.
