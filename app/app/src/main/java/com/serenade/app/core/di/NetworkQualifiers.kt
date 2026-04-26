package com.serenade.app.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicOkHttpClient
