package com.workrec.domain.entities

/**
 * 事前定義されたエクササイズテンプレートのドメインエンティティ
 * ユーザーが選択可能なエクササイズの基本情報を保持
 */
data class ExerciseTemplate(
    val id: Long = 0,
    val name: String,
    val category: ExerciseCategory,
    val description: String? = null, // エクササイズの説明
    val instructions: List<String> = emptyList(), // 実行手順
    val tips: List<String> = emptyList(), // コツやポイント
    val isUserCreated: Boolean = false // ユーザーが追加したカスタムエクササイズかどうか
)


/**
 * エクササイズテンプレートの検索・フィルタリング用データクラス
 */
data class ExerciseFilter(
    val searchQuery: String = "",
    val category: ExerciseCategory? = null,
    val showUserCreated: Boolean = true
)

/**
 * エクササイズテンプレートのソート順
 */
enum class ExerciseSortOrder(val displayName: String) {
    NAME_ASC("名前（昇順）"),
    NAME_DESC("名前（降順）"),
    CATEGORY("カテゴリー別"),
    POPULARITY("人気順") // 将来的に使用履歴から算出
}