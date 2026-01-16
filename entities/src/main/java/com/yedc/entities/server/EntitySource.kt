package com.yedc.entities.server

interface EntitySource {
    fun fetchDeletedStates(integrityUrl: String, ids: List<String>): List<Pair<String, Boolean>>
}
