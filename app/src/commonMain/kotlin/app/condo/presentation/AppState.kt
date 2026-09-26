package app.condo.presentation

import app.condo.domain.*

enum class Route(val title: String, val module: Module? = null) {
    HOME("Início"), PARCELS("Encomendas", Module.PARCELS),
    PARCEL_DETAIL("Detalhe da encomenda", Module.PARCELS),
    CAMERAS("Câmeras", Module.CAMERAS), CAMERA_DETAIL("Visualizar câmera", Module.CAMERAS),
    VISITS("Visitas", Module.VISITS), VISIT_FORM("Nova visita", Module.VISITS),
    PETS("Meus pets", Module.PETS), PET_FORM("Cadastro de pet", Module.PETS),
    PET_DETAIL("Detalhes do pet", Module.PETS), PET_ALERTS("Pets perdidos e achados", Module.PETS),
    BOOKINGS("Reservas", Module.BOOKINGS), EVENTS("Agenda", Module.EVENTS),
    PROFILE("Perfil"), PROFILE_FORM("Dados pessoais"), MEMBERS("Moradores da unidade"),
    VEHICLES("Veículos"), CONDOMINIUMS("Seus condomínios"), NOTIFICATIONS("Notificações"),
    NOTICES("Avisos", Module.NOTICES), BULLETIN("Detalhes do aviso", Module.NOTICES),
    SERVICES("Solicitações", Module.SERVICES), REQUEST_FORM("Nova solicitação", Module.SERVICES),
    REQUEST_DETAIL("Acompanhamento", Module.SERVICES), CONCIERGE("Portaria", Module.CONCIERGE),
    SECURITY("Senha e biometria"), PRIVACY("Privacidade e dados"), HELP("Ajuda e suporte"),
    RECOVERY("Recuperar senha"), ACTIVATE("Primeiro acesso"), DEMO("Cenários demonstrativos"),
}
