package com.phew.core.ui.navigation

/**
 * Navigation에서 사용되는 SavedStateHandle 키들을 관리하는 객체
 */
object NavigationKeys {
    /**
     * 카드 작성 완료 후 피드 새로고침을 위한 키
     */
    const val CARD_ADDED = "card_added"
    
    /**
     * 카드 삭제 완료 후 피드 새로고침을 위한 키
     */
    const val CARD_DELETED = "card_deleted"

    /**
     * 카드 상세에서 좋아요/댓글 변경 후 피드 새로고침을 위한 키
     */
    const val CARD_UPDATED = "card_updated"
    
    /**
     * 프로필 업데이트 완료 후 화면 새로고침을 위한 키
     */
    const val PROFILE_UPDATED = "profile_updated"
}
