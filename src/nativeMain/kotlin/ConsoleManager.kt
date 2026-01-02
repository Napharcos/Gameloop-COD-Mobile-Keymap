import kernel.*
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*

object ConsoleManager {

    const val TH32CS_SNAPPROCESS: UInt = 0x00000002u

    fun startCODM() {
        val path = getGameloopPath()
        val exe = if (path != null) "$path\\Application\\GameLoopLauncher.exe" else "C:\\Program Files\\Tencent\\GameLoop\\Application\\GameLoopLauncher.exe"
        val args = "--from \"2\" --launch-pkg-name \"com.activision.callofduty.shooter\" --launch-proc-name \"GameLoopEmulator.exe\""
        val command = "cmd /c start \"\" \"$exe\" $args"
        system(command)
    }

    fun stopEmulator() {
        val processList = listOf(
            "GameLoopLauncher.exe",
            "GameLoopEmulator.exe",
            "GameLoopAsistant.exe",
            "GameLoop.exe",
            "GLABoxHeadless.exe",
            "GLABoxSVC.exe",
            "GameLoopVfs.exe",
            "AceKeeper.exe",
            "AceKeeperService.exe",
            "GameLoopVm.exe",
        )

        for (proc in processList) {
            val pid = findPidByName(proc)
            if (pid != null) killProcessByPid(pid)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun findPidByName(processName: String): Int? {
        memScoped {
            val snapshot = my_CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0u)
            if (snapshot == INVALID_HANDLE_VALUE) {
                println("Nem sikerült folyamatlistát lekérni.")
                return null
            }

            val entry = alloc<PROCESSENTRY32>()
            entry.dwSize = sizeOf<PROCESSENTRY32>().toUInt()

            var hasNext = my_Process32First(snapshot, entry.ptr)
            while (hasNext != 0) {
                val exeName = entry.szExeFile.toKString()
                if (exeName.equals(processName, ignoreCase = true)) {
                    my_CloseHandle(snapshot)
                    return entry.th32ProcessID.toInt()
                }
                hasNext = my_Process32Next(snapshot, entry.ptr)
            }

            my_CloseHandle(snapshot)
        }
        return null
    }


    @OptIn(ExperimentalForeignApi::class)
    fun killProcessByPid(pid: Int): Boolean {
        val handle = OpenProcess(PROCESS_TERMINATE.toUInt(), 0, pid.toUInt())
        if (handle == null || handle == INVALID_HANDLE_VALUE) return false
        val result = TerminateProcess(handle, 0u)
        CloseHandle(handle)
        return result != 0
    }

    @OptIn(ExperimentalForeignApi::class)
    fun getSpecificPath(pathId: _GUID): String? = memScoped {
        val pathPtr = alloc<LPWSTRVar>()
        val result = SHGetKnownFolderPath(pathId.ptr, 0u, null, pathPtr.ptr)

        if (result != S_OK) return null

        val path = pathPtr.value?.toKString()
        CoTaskMemFree(pathPtr.value)
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getGameloopPath(): String? = memScoped {
        val hKey = alloc<CPointerVar<HKEY__>>()

        val result = RegOpenKeyExW(
            hKey = HKEY_LOCAL_MACHINE,
            lpSubKey = "Software\\Tencent\\GameLoop",
            ulOptions = 0u,
            samDesired = KEY_READ.toUInt(),
            phkResult = hKey.ptr
        )

        if (result != ERROR_SUCCESS) return null

        try {
            getValue(hKey.value!!, "InstallPath")
        } catch (_: Throwable) {
            null
        } finally {
            RegCloseKey(hKey.value)
        }
    }
}