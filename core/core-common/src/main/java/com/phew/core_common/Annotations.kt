package com.phew.core_common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebug

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion