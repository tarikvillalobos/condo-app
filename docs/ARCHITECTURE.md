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

## Marca e ambiente

Brands em `app/design/Brand.kt` centraliza nome, cores, módulos, monograma,
contato, textos institucionais, termos, privacidade e mapa de URLs de ambiente.
URLs permanecem vazias até serem fornecidas pelo cliente. Nunca insira segredos.
Para adicionar marca: cadastrar Brand, permitir seu id na configuração Gradle,
definir applicationId/bundle identifier e substituir assets de marca.
Condo e Viva são configurações fictícias. Contatos `.invalid` não enviam mensagens.

No Android, `-Pbrand=viva` altera o applicationId e o nome exibido. No iOS, personalize
PRODUCT_BUNDLE_IDENTIFIER/CFBundleDisplayName no XcodeGen e defina CONDO_BRAND=viva.
CONDO_ENVIRONMENT seleciona demo/staging/production e é repassado ao Gradle pelo host.
A troca de marca é configuração de distribuição; a seleção de condomínio acontece em runtime.

## Adaptação visual

A largura disponível determina a navegação: abaixo de 600 dp há barra inferior;
a partir de 600 dp há rail; a partir de 840 dp há menu lateral e lista/detalhe de encomendas.
Conteúdo tem largura máxima de 1180 dp; formulários usam até 520 dp.
Grades calculam colunas pela largura mínima e escala de fonte, sem escalar a tela inteira.
Câmeras usam até três colunas; calendário usa até sete; atalhos, até quatro.
Insets seguros e de teclado são aplicados na raiz. Conteúdo principal e diálogos podem rolar.
Botões têm pelo menos 48 dp de altura. Status combinam texto e cor.
Títulos têm semântica de heading; controles recebem rótulos e descrições.
O QR preserva proporção 1:1 e margem branca de quatro módulos, com código numérico alternativo.

Android retém o controller durante mudanças de configuração. Desktop e iOS mantêm
o estado enquanto a janela muda de tamanho; dados digitados não dependem de largura.
Validação específica de dobradiça em foldables, leitor de tela e teclado físico
está registrada separadamente; não deve ser inferida a partir dos renders desktop.

## Versões verificadas

- [Compatibilidade Kotlin/Gradle/AGP/Xcode](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html).
- [Compatibilidade Compose e plataformas](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html).
- [Plugin Android KMP](https://developer.android.com/kotlin/multiplatform/plugin).
- [Releases Ktor](https://ktor.io/docs/releases.html).
- [QR Code Kotlin](https://github.com/g0dkar/qrcode-kotlin).

Kotlin 2.4.20, Gradle 9.4.1 e AGP 9.2.0 estão na faixa de compatibilidade publicada.
O plugin Compose Compiler usa exatamente a versão de Kotlin.
O Xcode de referência da tabela é 26.4; o ambiente local tem 26.2. Builds locais
são reportados como evidência específica, sem substituir a matriz oficial.

A CI Apple usa o runner ARM `macos-26`, conforme a [matriz oficial de runners](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).
