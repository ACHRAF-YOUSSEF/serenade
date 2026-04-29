package com.serenade.app.feature.providers.data

import com.serenade.app.feature.providers.data.entity.ProviderEntity
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepository @Inject constructor(
    private val providerDao: ProviderDao
) {
    fun getAll() = providerDao.getAll()

    suspend fun getById(id: String) = providerDao.getById(id)

    suspend fun save(provider: ProviderEntity) {
        require(provider.manifestUrl.isHttpsUrl()) {
            "Provider manifest URL must use HTTPS."
        }
        providerDao.insert(provider)
    }

    suspend fun deleteById(id: String) {
        providerDao.deleteById(id)
    }

    private fun String.isHttpsUrl(): Boolean {
        return runCatching { URI(this).scheme == HTTPS_SCHEME }.getOrDefault(false)
    }

    private companion object {
        const val HTTPS_SCHEME = "https"
    }
}
