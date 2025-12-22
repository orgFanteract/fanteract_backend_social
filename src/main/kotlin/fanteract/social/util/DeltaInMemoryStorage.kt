package fanteract.social.util

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class DeltaInMemoryStorage {
    private val store =
        ConcurrentHashMap<Long, ConcurrentHashMap<String, AtomicLong>>()

    fun addDelta(userId: Long, field: String, delta: Long) {
        store
            .computeIfAbsent(userId) { ConcurrentHashMap() }
            .computeIfAbsent(field) { AtomicLong(0) }
            .addAndGet(delta)
    }

    fun snapshot(): Map<Long, Map<String, Long>> {
        val result = mutableMapOf<Long, Map<String, Long>>()

        store.forEach { (userId, fields) ->
            val snap = mutableMapOf<String, Long>()
            fields.forEach { (field, counter) ->
                val v = counter.get()
                if (v > 0) snap[field] = v
            }
            if (snap.isNotEmpty()) result[userId] = snap
        }

        return result
    }

    fun subtract(userId: Long, field: String, value: Long) {
        val counter = store[userId]?.get(field) ?: return
        counter.addAndGet(-value)
    }
}