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


## White Label

This project is designed to support multiple condominiums, brands, and clients.

Branding, visual identity, available modules, and content may vary depending on the deployment.

## Status

🚧 This project is currently under development.

## Getting Started

Setup and development instructions will be added as the project evolves.

## License

Private and proprietary software.
