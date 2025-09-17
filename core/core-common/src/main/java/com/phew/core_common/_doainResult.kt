package com.phew.core_common

/**
 * 연산의 결과를 나타내는 제네릭 sealed interface
 * 성공(Success) 또는 실패(Failure)의 두 가지 상태를 가집니다.
 * @param T 성공시 반환 데이터 타입
 * @param E 실패 시 반환될 에러 타입
 */

sealed interface DomainResult<out T, out E> {
    data class Success<T>(val data: T) : DomainResult<T, Nothing>
    data class Failure<E>(val error: E) : DomainResult<Nothing, E>
}