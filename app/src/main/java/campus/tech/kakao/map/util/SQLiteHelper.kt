package campus.tech.kakao.map.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "map.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "map_table"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_ADDRESS = "address"
        const val COL_CATEGORY = "category"
    }


    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성 SQL
        val createTableStatement = ("CREATE TABLE $TABLE_NAME ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_NAME TEXT, "
                + "$COL_ADDRESS TEXT, "
                + "$COL_CATEGORY TEXT)")
        db.execSQL(createTableStatement)

        // 비어있는지 확인
        if (isTableEmpty(db)) {
            insertInitialData(db)
        }
    }

    // 테이블이 비어있는지 확인
    private fun isTableEmpty(db: SQLiteDatabase): Boolean {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        cursor.moveToFirst()

        val cnt = cursor.getInt(0)
        cursor.close()

        return cnt == 0
    }

    // 데이터베이스 업그레이드 시 기존 데이터 유지
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    // 초기 데이터 삽입 메서드
    // id만 삽입 후 db inspector에서 직접 입력
    private fun insertInitialData(db: SQLiteDatabase) {
        for (i in 1..50) {
            val values = ContentValues().apply {
                put(COL_NAME, "")
                put(COL_ADDRESS, "")
                put(COL_CATEGORY, "")
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    //항목 검색
    fun searchItems(query: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery( //cursor로 반환
            "SELECT * FROM $TABLE_NAME WHERE $COL_NAME LIKE ?",
            arrayOf("%$query%")
        )
    }
}