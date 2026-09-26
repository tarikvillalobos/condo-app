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
