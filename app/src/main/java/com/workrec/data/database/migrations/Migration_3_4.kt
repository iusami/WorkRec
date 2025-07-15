package com.workrec.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * データベースバージョン3から4への移行
 * exercise_templatesテーブルから muscle、equipment、difficulty カラムを削除
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 新しいテーブル構造でテンプレートテーブルを再作成
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `exercise_templates_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `description` TEXT,
                `instructions` TEXT NOT NULL,
                `tips` TEXT NOT NULL,
                `isUserCreated` INTEGER NOT NULL
            )
        """)
        
        // 既存データを新テーブルにコピー（muscle、equipment、difficultyカラムを除く）
        database.execSQL("""
            INSERT INTO `exercise_templates_new` 
            (`id`, `name`, `category`, `description`, `instructions`, `tips`, `isUserCreated`)
            SELECT `id`, `name`, `category`, `description`, `instructions`, `tips`, `isUserCreated`
            FROM `exercise_templates`
        """)
        
        // 古いテーブルを削除
        database.execSQL("DROP TABLE `exercise_templates`")
        
        // 新しいテーブルを正式名称にリネーム
        database.execSQL("ALTER TABLE `exercise_templates_new` RENAME TO `exercise_templates`")
        
        // 必要なインデックスのみを再作成（muscle、equipmentのインデックスは削除）
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_templates_name` ON `exercise_templates` (`name`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_templates_category` ON `exercise_templates` (`category`)")
    }
}