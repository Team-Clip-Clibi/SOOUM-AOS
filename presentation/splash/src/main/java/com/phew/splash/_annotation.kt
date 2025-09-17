package com.phew.splash

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebug

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion
