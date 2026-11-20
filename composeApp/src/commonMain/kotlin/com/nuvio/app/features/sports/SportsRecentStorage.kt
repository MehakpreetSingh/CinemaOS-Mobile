package com.nuvio.app.features.sports

internal expect object SportsRecentStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}
