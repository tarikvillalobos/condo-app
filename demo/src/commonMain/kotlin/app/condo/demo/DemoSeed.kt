package app.condo.demo

import app.condo.domain.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal object DemoSeed {
    val memberships = listOf(
        Membership("aurora", "Residencial Jardim Aurora", "Bloco B · Apto 304"),
        Membership(
            "aguas", "Vila das Águas", "Bloco A · Apto 102",
            Module.entries.toSet() - Module.PETS, cameraAccess = false,
        ),
    )
    fun snapshot(membership: Membership, now: Instant): Snapshot = Snapshot(
        membership = membership,
        parcels = listOf(
            Parcel("p1", "Mercado Livre", "DEMO-001", "Portaria", "14 · M", now - 3.hours, now + 2.days),
            Parcel("p2", "Correios", null, "Portaria", "08 · P", now - 1.hours, now + 2.days),
            Parcel(
                "p3", "Amazon", "DEMO-003", "Portaria", "02 · M", now - 2.days,
                now, ParcelStatus.COLLECTED, now - 2.days + 4.hours,
            ),
        ),
        visits = listOf(
            Visit("v1", "Marina Exemplo", "Visita", false, now - 1.hours, now + 5.hours, VisitStatus.ENTERED),
            Visit("v2", "João Exemplo", "Eletricista", true, now, now + 3.hours, frequent = true),
            Visit("v3", "Carlos Exemplo", "Visitante na portaria", false, now, now + 2.hours, VisitStatus.AT_GATE),
        ),
        pets = listOf(
            Pet("pet1", "Thor", "Cão", "Golden Retriever", "2022-04-10", "Grande", "32", "DEMO-123", "Antirrábica", "2027-03-01"),
            Pet("pet2", "Mia", "Gato", "SRD", "2024-01-02", "Pequeno", "4", "", "Antirrábica", "2026-10-15"),
        ),
        facilities = listOf(
            Facility("grill", "Churrasqueira", "2 espaços · até 15 pessoas", "Até 4 horas. Limpeza após o uso. Máximo de 2 reservas futuras por unidade."),
            Facility("hall", "Salão de festas", "Até 40 pessoas", "Até 4 horas. Respeite o silêncio após as 22h. Sem cobrança nesta demonstração."),
            Facility("court", "Quadra", "Esporte e convivência", "Até 4 horas. Use calçados adequados."),
            Facility("gym", "Academia", "Seu tempo de cuidar de você", "Até 4 horas. Higienize os equipamentos."),
        ),
        cameras = listOf(
            Camera("c1", "Portaria principal", "Portaria", true),
            Camera("c2", "Garagem G1", "Garagem", true),
            Camera("c3", "Hall · Bloco B", "Portaria", false),
            Camera("c4", "Área dos lockers", "Portaria", true),
            Camera("c5", "Piscina", "Lazer", true),
        ),
        bulletins = listOf(
            Bulletin("b1", "Manutenção dos elevadores", "Bloco B · das 8h às 12h. Utilize os elevadores do bloco A durante a manutenção programada.", now + 2.days),
            Bulletin("b2", "Assembleia do condomínio", "Salão de festas · 19h30. Pauta: melhorias nas áreas comuns e prestação de contas. Evento fictício.", now + 5.days, true),
            Bulletin("b3", "Feira de troca de livros", "Traga um livro e descubra uma nova história no jardim. Sábado, às 10h.", now + 7.days, true),
        ),
        notices = listOf(
            Notice("n1", "Sua encomenda chegou ao locker", "parcel:p1"),
            Notice("n2", "Novo aviso da administração", "bulletin:b1"),
        ),
        vehicles = listOf(Vehicle("car1", "Hatch · prata", "DEMO001")),
        members = listOf(UnitMember("Alex Exemplo", "Titular"), UnitMember("Sam Exemplo", "Morador")),
        updatedAt = now,
    )
