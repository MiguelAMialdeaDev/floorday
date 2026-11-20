package studio.mialdea.floorday.core.domain.model

enum class EntrySource(val key: String) {
    APP("app"),
    NOTIFICATION("notification"),
    WIDGET("widget"),
    ;

    companion object {
        fun fromKey(key: String): EntrySource? = entries.firstOrNull { it.key == key }
    }
}
