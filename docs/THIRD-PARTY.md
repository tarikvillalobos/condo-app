# Componentes de terceiros

O Condo App continua sendo software privado e proprietário.
Este arquivo identifica componentes com licenças independentes.

- Kotlin, Coroutines, Serialization, Compose Multiplatform e Ktor: Apache-2.0.
- QRCode Kotlin, de Rafael M. Lins: MIT; veja https://github.com/g0dkar/qrcode-kotlin.
- ZXing, usado nos testes: Apache-2.0.
- Manrope e Sora: SIL Open Font License 1.1, distribuídas pelo Google Fonts.
- Fontes: https://github.com/google/fonts/tree/main/ofl/manrope e /ofl/sora.

O script prepare-assets baixa os textos OFL para `.generated/licenses/`.
Ao distribuir binários, incluir esses textos e os avisos aplicáveis às dependências.
Não foi adicionada uma licença open-source para o código do aplicativo.
