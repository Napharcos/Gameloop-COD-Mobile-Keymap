import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.windows.FOLDERID_RoamingAppData
import sqlite.sqlite3_get_config_value
import sqlite.sqlite_close
import sqlite.sqlite_exec
import sqlite.sqlite_open

@OptIn(ExperimentalForeignApi::class)
object DatabaseManager {

    private var db: COpaquePointer? = null

    private val dbPath = "${ConsoleManager.getSpecificPath(FOLDERID_RoamingAppData)}\\Tencent\\GameLoop\\db\\Configs.db"

    fun deleteTriggers() {
        if (db == null) db = sqlite_open(dbPath)

        removeConfigTriggers()
        removeKeymapTriggers()
        closeDb()
    }

    fun launchUpdateDb() {
        if (db == null) db = sqlite_open(dbPath)

        removeConfigTriggers()
        updateSMK()
        updateDefKeymap()
        addConfigTriggers()
        removeKeymapTriggers()
        clearKeymapConfigs()
        addAllKeymapConfig()
        addKeymapTriggers()
        closeDb()
    }

    fun addAllKeymapConfig() {
        if (db == null) { db = sqlite_open(dbPath) }

        db?.let {
            sqlite_exec(it, OFFICIAL_CONFIG2)
            sqlite_exec(it, LAST_MODE)
            sqlite_exec(it, KEYMAP_145)
            sqlite_exec(it, KEYMAP_145_145)
            sqlite_exec(it, KEYMAP_145_175)
            sqlite_exec(it, KEYMAP_146)
            sqlite_exec(it, KEYMAP_146_146)
            sqlite_exec(it, KEYMAP_146_175)
        }
    }

    fun clearKeymapConfigs() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, CLEAR_KEYMAP_CONFIGS) }
    }

    fun updateSMK() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, UPDATE_SMK) }
    }

    fun updateDefKeymap() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, UPDATE_DEFAULT_KEYMAP) }
    }

    fun addConfigTriggers() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, ADD_CONFIG_TRIGGERS) }
    }

    fun removeConfigTriggers() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, REMOVE_CONFIG_TRIGGERS) }
    }

    fun addKeymapTriggers() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, ADD_KEYMAP_TRIGGERS) }
    }

    fun removeKeymapTriggers() {
        if (db == null) db = sqlite_open(dbPath)

        db?.let { sqlite_exec(it, REMOVE_KEYMAP_TRIGGERS) }
    }

    fun closeDb() {
        db?.let { sqlite_close(it) }
        db = null
    }
}