package com.phew.device

/**
 * 키-값 기반의 데이터 저장소 인터페이스
 * @param T 저장하고 불러올 데이터의 타입
 */
interface DataStore<T> {
    /**
     * 지정된 키에 데이터를 저장합니다.
     * @param key 저장할 데이터의 키
     * @param data 저장할 데이터
     */
    suspend fun insert(key: String, data: T): Boolean

    /**
     * 지정된 키의 데이터를 불러옵니다.
     * @param key 불러올 데이터의 키
     * @return 키에 해당하는 데이터. 없으면 null을 반환합니다.
     */
    suspend fun get(key: String): T?

    /**
     * 지정된 키의 데이터를 삭제합니다.
     * @param key 삭제할 데이터의 키
     */
    suspend fun remove(key: String): Boolean
}