package com.workrec.data.repository

import com.workrec.data.database.dao.ExerciseTemplateDao
import com.workrec.data.database.entities.ExerciseTemplateEntity
import com.workrec.data.database.entities.toDomainModel
import com.workrec.data.database.entities.toEntity
import com.workrec.domain.entities.*
import com.workrec.domain.repository.ExerciseTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * エクササイズテンプレートリポジトリの実装
 * Room DatabaseとドメインレイヤーのExerciseTemplateRepositoryインターフェースを繋ぐ
 */
@Singleton
class ExerciseTemplateRepositoryImpl @Inject constructor(
    private val exerciseTemplateDao: ExerciseTemplateDao
) : ExerciseTemplateRepository {

    override fun getAllExerciseTemplatesFlow(): Flow<List<ExerciseTemplate>> {
        return exerciseTemplateDao.getAllExerciseTemplatesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAllExerciseTemplates(): List<ExerciseTemplate> {
        return exerciseTemplateDao.getAllExerciseTemplates().map { it.toDomainModel() }
    }

    override suspend fun getExerciseTemplateById(id: Long): ExerciseTemplate? {
        return exerciseTemplateDao.getExerciseTemplateById(id)?.toDomainModel()
    }

    override suspend fun searchExerciseTemplatesByName(name: String): List<ExerciseTemplate> {
        return exerciseTemplateDao.searchExerciseTemplatesByName(name).map { it.toDomainModel() }
    }

    override suspend fun getExerciseTemplatesByCategory(category: ExerciseCategory): List<ExerciseTemplate> {
        return exerciseTemplateDao.getExerciseTemplatesByCategory(category).map { it.toDomainModel() }
    }


    override suspend fun searchExerciseTemplates(
        filter: ExerciseFilter,
        sortOrder: ExerciseSortOrder
    ): List<ExerciseTemplate> {
        return exerciseTemplateDao.searchExerciseTemplates(
            name = filter.searchQuery.takeIf { it.isNotBlank() },
            category = filter.category,
            showUserCreated = filter.showUserCreated,
            sortOrder = sortOrder.name
        ).map { it.toDomainModel() }
    }

    override suspend fun getUserCreatedExerciseTemplates(): List<ExerciseTemplate> {
        return exerciseTemplateDao.getUserCreatedExerciseTemplates().map { it.toDomainModel() }
    }

    override suspend fun getPredefinedExerciseTemplates(): List<ExerciseTemplate> {
        return exerciseTemplateDao.getPredefinedExerciseTemplates().map { it.toDomainModel() }
    }

    override suspend fun getUniqueCategories(): List<ExerciseCategory> {
        return exerciseTemplateDao.getUniqueCategories()
    }


    override suspend fun saveExerciseTemplate(exerciseTemplate: ExerciseTemplate): Long {
        return exerciseTemplateDao.insertExerciseTemplate(exerciseTemplate.toEntity())
    }

    override suspend fun saveExerciseTemplates(exerciseTemplates: List<ExerciseTemplate>): List<Long> {
        val entities = exerciseTemplates.map { it.toEntity() }
        return exerciseTemplateDao.insertExerciseTemplates(entities)
    }

    override suspend fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate) {
        exerciseTemplateDao.updateExerciseTemplate(exerciseTemplate.toEntity())
    }

    override suspend fun deleteExerciseTemplate(exerciseTemplate: ExerciseTemplate) {
        exerciseTemplateDao.deleteExerciseTemplate(exerciseTemplate.toEntity())
    }

    override suspend fun deleteExerciseTemplateById(id: Long) {
        exerciseTemplateDao.deleteExerciseTemplateById(id)
    }

    override suspend fun deleteAllUserCreatedExerciseTemplates() {
        exerciseTemplateDao.deleteAllUserCreatedExerciseTemplates()
    }

    override suspend fun seedPredefinedExercises() {
        // 既にデータが存在する場合はスキップ
        if (hasSeedData()) return
        
        val predefinedExercises = createPredefinedExercises()
        saveExerciseTemplates(predefinedExercises)
    }

    override suspend fun hasSeedData(): Boolean {
        return getPredefinedExerciseTemplates().isNotEmpty()
    }
    
    /**
     * 事前定義されたエクササイズテンプレートを作成
     */
    private fun createPredefinedExercises(): List<ExerciseTemplate> {
        return listOf(
            // 胸部エクササイズ
            ExerciseTemplate(
                name = "ベンチプレス",
                category = ExerciseCategory.CHEST,
                description = "胸部の基本的な筋力トレーニング種目",
                instructions = listOf(
                    "ベンチに仰向けに寝る",
                    "肩甲骨を寄せ、胸を張る",
                    "バーベルを胸まで下ろす",
                    "力強く押し上げる"
                ),
                tips = listOf("肩甲骨をしっかり寄せること", "胸の高さまで確実に下ろすこと")
            ),
            ExerciseTemplate(
                name = "プッシュアップ",
                category = ExerciseCategory.CHEST,
                description = "自重による胸部トレーニング",
                instructions = listOf(
                    "腕立て伏せの姿勢を作る",
                    "胸を床まで下ろす",
                    "力強く押し上げる"
                ),
                tips = listOf("体をまっすぐに保つこと", "肘は外に開かない")
            ),
            ExerciseTemplate(
                name = "ダンベルフライ",
                category = ExerciseCategory.CHEST,
                description = "胸部の分離トレーニング",
                instructions = listOf(
                    "ベンチに仰向けに寝る",
                    "ダンベルを胸の上で構える",
                    "弧を描くように広げる",
                    "胸の筋肉を意識して戻す"
                ),
                tips = listOf("肘を軽く曲げて保つ", "肩関節の可動域を意識する")
            ),
            
            // 背中エクササイズ
            ExerciseTemplate(
                name = "デッドリフト",
                category = ExerciseCategory.BACK,
                description = "背中と下半身の複合種目",
                instructions = listOf(
                    "バーベルの前に立つ",
                    "膝を曲げて腰を落とす",
                    "背中をまっすぐに保つ",
                    "立ち上がりながらバーを引き上げる"
                ),
                tips = listOf("背中を丸めない", "膝とつま先の向きを揃える")
            ),
            ExerciseTemplate(
                name = "ラットプルダウン",
                category = ExerciseCategory.BACK,
                description = "マシンを使った背中のトレーニング",
                instructions = listOf(
                    "マシンに座る",
                    "バーを肩幅より広く握る",
                    "胸に向かって引き下ろす",
                    "ゆっくりと戻す"
                ),
                tips = listOf("肩甲骨を寄せる意識", "胸を張って引く")
            ),
            ExerciseTemplate(
                name = "チンアップ",
                category = ExerciseCategory.BACK,
                description = "自重による背中のトレーニング",
                instructions = listOf(
                    "鉄棒にぶら下がる",
                    "顎がバーを越えるまで引き上げる",
                    "ゆっくりと降ろす"
                ),
                tips = listOf("肩甲骨を使って引く", "反動を使わない")
            ),
            
            // 肩エクササイズ
            ExerciseTemplate(
                name = "ショルダープレス",
                category = ExerciseCategory.SHOULDERS,
                description = "肩の基本的な押す動作",
                instructions = listOf(
                    "ダンベルを肩の高さで構える",
                    "頭上に押し上げる",
                    "ゆっくりと戻す"
                ),
                tips = listOf("背中を反らせすぎない", "コントロールして動作する")
            ),
            ExerciseTemplate(
                name = "サイドレイズ",
                category = ExerciseCategory.SHOULDERS,
                description = "肩の側面を鍛えるトレーニング",
                instructions = listOf(
                    "ダンベルを体の横で持つ",
                    "肩の高さまで真横に上げる",
                    "ゆっくりと降ろす"
                ),
                tips = listOf("肘を軽く曲げる", "肩より上に上げない")
            ),
            
            // 腕エクササイズ
            ExerciseTemplate(
                name = "バーベルカール",
                category = ExerciseCategory.ARMS,
                description = "上腕二頭筋の基本種目",
                instructions = listOf(
                    "バーベルを持って立つ",
                    "肘を固定してカールアップ",
                    "ゆっくりと戻す"
                ),
                tips = listOf("肘の位置を固定", "反動を使わない")
            ),
            ExerciseTemplate(
                name = "トライセプスエクステンション",
                category = ExerciseCategory.ARMS,
                description = "上腕三頭筋のトレーニング",
                instructions = listOf(
                    "ベンチに仰向けになる",
                    "ダンベルを頭上で構える",
                    "肘だけを動かして下ろす",
                    "元の位置に戻す"
                ),
                tips = listOf("肘の位置を固定", "ゆっくりとした動作")
            ),
            
            // 脚エクササイズ
            ExerciseTemplate(
                name = "スクワット",
                category = ExerciseCategory.LEGS,
                description = "下半身の王様的種目",
                instructions = listOf(
                    "バーベルを肩に担ぐ",
                    "足を肩幅に開く",
                    "膝を曲げて腰を下ろす",
                    "立ち上がる"
                ),
                tips = listOf("膝がつま先より前に出ない", "背中をまっすぐ保つ")
            ),
            ExerciseTemplate(
                name = "レッグプレス",
                category = ExerciseCategory.LEGS,
                description = "マシンを使った脚のトレーニング",
                instructions = listOf(
                    "マシンに座る",
                    "足をプレートに置く",
                    "膝を曲げて重量を下ろす",
                    "押し戻す"
                ),
                tips = listOf("膝とつま先の向きを揃える", "可動域をフルに使う")
            ),
            ExerciseTemplate(
                name = "ルーマニアンデッドリフト",
                category = ExerciseCategory.LEGS,
                description = "ハムストリングに効果的な種目",
                instructions = listOf(
                    "バーベルを持って立つ",
                    "膝を軽く曲げる",
                    "お尻を後ろに引きながら前傾",
                    "ハムストリングのストレッチを感じたら戻す"
                ),
                tips = listOf("背中をまっすぐ保つ", "ハムストリングの伸展を意識")
            ),
            
            // 体幹エクササイズ
            ExerciseTemplate(
                name = "プランク",
                category = ExerciseCategory.CORE,
                description = "体幹の安定性を鍛える",
                instructions = listOf(
                    "うつ伏せになる",
                    "肘と前腕で体を支える",
                    "体をまっすぐに保つ",
                    "指定時間キープ"
                ),
                tips = listOf("お尻を上げすぎない", "呼吸を続ける")
            ),
            ExerciseTemplate(
                name = "クランチ",
                category = ExerciseCategory.CORE,
                description = "腹筋の基本種目",
                instructions = listOf(
                    "仰向けに寝る",
                    "膝を90度に曲げる",
                    "頭と肩を床から離す",
                    "ゆっくりと戻す"
                ),
                tips = listOf("首に力を入れない", "腹筋を意識して動作")
            ),
            
            // 有酸素エクササイズ
            ExerciseTemplate(
                name = "ランニング",
                category = ExerciseCategory.CARDIO,
                description = "基本的な有酸素運動",
                instructions = listOf(
                    "適切なペースを見つける",
                    "正しいフォームで走る",
                    "呼吸を意識する"
                ),
                tips = listOf("無理をしないペース", "着地は前足部から")
            ),
            ExerciseTemplate(
                name = "バーピー",
                category = ExerciseCategory.CARDIO,
                description = "全身を使った高強度運動",
                instructions = listOf(
                    "立った状態から始める",
                    "スクワットの姿勢になる",
                    "プランクの姿勢に移行",
                    "元の姿勢に戻りジャンプ"
                ),
                tips = listOf("動作を正確に", "無理のない回数から始める")
            )
        )
    }
}