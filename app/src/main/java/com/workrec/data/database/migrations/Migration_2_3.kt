package com.workrec.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * データベースバージョン2から3への移行
 * エクササイズテンプレートテーブルの追加
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // exercise_templatesテーブルを作成
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `exercise_templates` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `muscle` TEXT NOT NULL,
                `equipment` TEXT NOT NULL,
                `difficulty` TEXT NOT NULL,
                `description` TEXT,
                `instructions` TEXT NOT NULL,
                `tips` TEXT NOT NULL,
                `isUserCreated` INTEGER NOT NULL
            )
        """)
        
        // パフォーマンス向上のためのインデックスを作成
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_templates_name` ON `exercise_templates` (`name`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_templates_category` ON `exercise_templates` (`category`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_templates_equipment` ON `exercise_templates` (`equipment`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_templates_muscle` ON `exercise_templates` (`muscle`)")
    }
}