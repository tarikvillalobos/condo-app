package app.condo.data

import io.ktor.http.HttpMethod
import io.ktor.http.encodeURLPathPart

/** Resident routes from Condo Platform API 1.1.0-draft. */
internal object ApiRoutes {
    data class Route(val method: HttpMethod, val path: String)
    private fun id(value: String): String = value.encodeURLPathPart()
    fun configuration() = Route(HttpMethod.Get, "/configuration")
    fun login() = Route(HttpMethod.Post, "/auth/password/login")
    fun refresh() = Route(HttpMethod.Post, "/auth/refresh")
    fun logout() = Route(HttpMethod.Post, "/auth/logout")
    fun profile() = Route(HttpMethod.Get, "/me")
    fun memberships() = Route(HttpMethod.Get, "/me/memberships")
    fun password() = Route(HttpMethod.Post, "/me/password")
    fun recovery() = Route(HttpMethod.Post, "/auth/password/recovery")
    fun invitation(code: String) = Route(HttpMethod.Get, "/auth/invitations/${id(code)}")
    fun accept(code: String) = Route(HttpMethod.Post, "/auth/invitations/${id(code)}/accept")
    fun link(code: String) = Route(HttpMethod.Post, "/me/invitations/${id(code)}/link")
