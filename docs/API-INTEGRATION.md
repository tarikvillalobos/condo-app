# Integração com API externa

## Situação atual

O contrato versionado está em `api/openapi.yaml` (Condo Platform 1.1.0-draft).
O adaptador `ApiRepository` usa as rotas de morador desse contrato. Ele envia
`X-Brand-Id`, bearer, `Idempotency-Key` nas mutações pertinentes e `If-Match`
quando o contrato exige versão. Refresh rotativo usa o cofre seguro quando a
sessão é persistida. Paginação por cursor mantém a consulta até a última página.

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
Nenhum retry automático de mutações ou interceptor de autenticação foi presumido.
Nenhum logger imprime headers, corpos, códigos QR, senhas ou identificadores pessoais.
O transporte não segue redirecionamentos automaticamente.

Só adicionar renovação de token e idempotência conforme regras da API.
Os adaptadores de armazenamento seguro e capacidades nativas já têm contratos separados.
VideoAdapter retorna indisponibilidade até existir um fornecedor autorizado;
as imagens de câmera atuais são ilustrações nativas marcadas SIMULAÇÃO.
Não se conecta o app diretamente a equipamentos ou serviços por suposição.

## Contrato exclusivamente demonstrativo

Payload QR: `condo-demo:v1:<condomínio>:<pickup|visit>:<registro>:<nonce>`.
Não é um contrato de locker real. O nonce usa aleatoriedade para cenários locais,
não deve ser usado como credencial de produção. Código de retirada: seis dígitos,
validade máxima de cinco minutos ou o prazo restante da encomenda.
Convite usa a janela cadastrada, até sete dias, com verificação no simulador.
Reemissão substitui o código anterior; consumo e revogação impedem reutilização local.
Um evento físico demonstrativo é sempre identificado como simulado na interface.
Reserva usa janela de quatro horas, máximo de duas futuras e horizonte de trinta dias.
Essas regras são fixtures para exercitar UI; não garantem segurança nem disponibilidade real.

## Homologação pendente

Após o contrato: testar contas de unidades distintas, expiração e revogação real,
conflitos simultâneos entre clientes, códigos expirados/consumidos, paginação,
falhas de rede, respostas atrasadas após troca de contexto, cache e logout.
A aprovação dessas regras no cliente não substitui verificações no backend externo.
Nenhum backend alternativo será criado para preencher lacunas da API.
