package com.workrec.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * データベースバージョン4から5への移行
 * goalsテーブルのisCompletedカラムにインデックスを追加してクエリパフォーマンスを向上
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // isCompletedカラムにインデックスを作成してフィルタリングクエリのパフォーマンスを向上
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_isCompleted` ON `goals` (`isCompleted`)")
    }
}