package com.workrec.domain.entities

/**
 * 事前定義されたエクササイズテンプレートのドメインエンティティ
 * ユーザーが選択可能なエクササイズの基本情報を保持
 */
data class ExerciseTemplate(
    val id: Long = 0,
    val name: String,
    val category: ExerciseCategory,
    val muscle: String, // 主要対象筋肉（例: "大胸筋"、"広背筋"）
    val equipment: ExerciseEquipment,
    val difficulty: ExerciseDifficulty,
    val description: String? = null, // エクササイズの説明
    val instructions: List<String> = emptyList(), // 実行手順
    val tips: List<String> = emptyList(), // コツやポイント
    val isUserCreated: Boolean = false // ユーザーが追加したカスタムエクササイズかどうか
)

/**
 * エクササイズで使用する器具・道具の分類
 */
enum class ExerciseEquipment(val displayName: String) {
    BARBELL("バーベル"),
    DUMBBELL("ダンベル"),
    MACHINE("マシン"), 
    CABLE("ケーブル"),
    BODYWEIGHT("自重"),
    RESISTANCE_BAND("レジスタンスバンド"),
    KETTLEBELL("ケトルベル"),
    MEDICINE_BALL("メディシンボール"),
    SUSPENSION("サスペンション"),
    OTHER("その他")
}

/**
 * エクササイズの難易度分類
 */
enum class ExerciseDifficulty(val displayName: String, val level: Int) {
    BEGINNER("初級", 1),
    INTERMEDIATE("中級", 2),
    ADVANCED("上級", 3),
    EXPERT("エキスパート", 4)
}

/**
 * エクササイズテンプレートの検索・フィルタリング用データクラス
 */
data class ExerciseFilter(
    val searchQuery: String = "",
    val category: ExerciseCategory? = null,
    val equipment: ExerciseEquipment? = null,
    val difficulty: ExerciseDifficulty? = null,
    val muscle: String? = null,
    val showUserCreated: Boolean = true
)

/**
 * エクササイズテンプレートのソート順
 */
enum class ExerciseSortOrder(val displayName: String) {
    NAME_ASC("名前（昇順）"),
    NAME_DESC("名前（降順）"),
    CATEGORY("カテゴリー別"),
    DIFFICULTY("難易度順"),
    POPULARITY("人気順") // 将来的に使用履歴から算出
}