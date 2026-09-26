# Integração com API externa

## Situação atual

Não foram encontrados OpenAPI/Swagger, coleções, URLs, regras de autenticação,
exemplos de resposta ou erros no repositório original. A documentação foi solicitada.
Nenhuma rota, campo de negócio, token, paginação ou endpoint de fornecedor foi inventado.
Os caminhos e DTOs em testes MockEngine são fixtures de teste, sem significado de produção.

| Camada | Estado |
|---|---|
| Interface com dados fictícios | Implementada |
| Repositórios demonstrativos locais | Implementados e isolados |
| Infraestrutura Ktor Client | Implementada; testes com respostas controladas |
| DTOs/mappers da API de negócio | Aguardam documentação |
| Autenticação/renovação/paginação reais | Aguardam contrato |
| Integração com homologação | Não realizada |
| Lockers, portaria, câmeras, push, e-mail/SMS/WhatsApp | Não integrados |

## Informações necessárias

1. Especificação versionada e URL de homologação por marca/ambiente.
2. Autenticação, expiração, renovação, logout e política de armazenamento de tokens.
3. Identificadores e regras de escopo por cliente, usuário, unidade e condomínio.
4. Formatos de sucesso/erro, paginação, limites e política de idempotência.
5. Contratos de reserva, disponibilidade, confirmação e conflitos concorrentes.
6. Convites/códigos: payload, validade, escopo, revogação e consumo.
7. Estado físico de lockers, reconciliação e eventos duplicados/fora de ordem.
8. Permissões de câmeras, reprodução/gravações e URLs autorizadas.
9. Registro de dispositivos para push, preferências e consentimentos necessários.
10. Conteúdo institucional, contatos e política de privacidade por distribuição.

## Adaptador a implementar após receber o contrato

Criar DTOs na camada data separados das entidades; mapeá-los em funções explícitas.
Implementar CondoRepository sobre ApiTransport e substituir UnavailableRepository na factory.
As URLs vêm de Brand.apiUrls; HTTPS é obrigatório. Não usar query string para tokens.
Headers, método, corpo e caminho devem ser definidos a partir do contrato recebido.
O transporte possui timeouts, cancelamento, serialização e mapeamento genérico de
status HTTP 401/403/409, sem mostrar corpo de erro arbitrário ao usuário.
