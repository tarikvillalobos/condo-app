# Integração com API externa

## Situação atual

O contrato versionado está em `api/openapi.yaml` (Condo Platform 1.1.0-draft).
O adaptador `ApiRepository` usa as rotas de morador desse contrato. Ele envia
`X-Brand-Id`, bearer, `Idempotency-Key` nas mutações pertinentes e `If-Match`
quando o contrato exige versão. Refresh rotativo usa o cofre seguro quando a
sessão é persistida. Paginação por cursor mantém a consulta até a última página.

Staging/produção precisam de `-PapiBaseUrl=https://.../v1`. Sem URL, usam
`UnavailableRepository`; nunca recorrem aos dados demonstrativos. O hostname
no OpenAPI é somente placeholder e não foi usado como servidor real.

O app não chama `/ops` nem `/admin`. Códigos de retirada e convites são obtidos
pela API; eventos físicos continuam exclusivos do backend/equipamento. Vídeo
ao vivo não é mostrado como real até existir um player autorizado.

## Limitações ainda visíveis

- Primeiro acesso exige CPF e `acceptedTermsVersion`, que a tela atual não coleta.
- Alteração de contato exige desafio e verificação; a edição simples de perfil
  fica indisponível no adaptador.
- Preferências da tela são por assunto, mas o contrato define canais
  (`inApp`, `sms`, `whatsapp`); a gravação fica indisponível até alinhar a UI.
- Disponibilidade de espaços precisa ser consultada antes da reserva; o servidor
  ainda decide conflitos no POST de reservas.
- Falta URL de homologação e contas reais para validar respostas e permissões.

## Rotas principais

| Fluxo | Caminhos do contrato |
|---|---|

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
