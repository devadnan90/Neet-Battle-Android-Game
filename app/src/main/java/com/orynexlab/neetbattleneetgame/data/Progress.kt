package com.orynexlab.neetbattleneetgame.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar

object Progress {

    private lateinit var sp: SharedPreferences

    var name by mutableStateOf("")
        private set
    var xp by mutableIntStateOf(0); private set
    var coins by mutableIntStateOf(0); private set
    var streak by mutableIntStateOf(0); private set
    var played by mutableIntStateOf(0); private set
    var wins by mutableIntStateOf(0); private set
    var answered by mutableIntStateOf(0); private set
    var correct by mutableIntStateOf(0); private set
    var badges by mutableStateOf(setOf<String>()); private set

    private var seen = mutableSetOf<Long>()
    private var mistakes = mutableSetOf<Long>()
    private var topicHits = mutableMapOf<String, Pair<Int, Int>>()  // topic -> attempted to correct

    var soundOn by mutableStateOf(true); private set

    var freezes by mutableIntStateOf(0); private set
    const val FREEZE_COST = 120

    val level: Int get() = Levels.levelFor(xp)
    val levelProgress: Float get() = Levels.progressIn(xp)
    val accuracy: Int get() = if (answered == 0) 0 else correct * 100 / answered
    val loggedIn: Boolean get() = name.isNotBlank()

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("neet_progress", Context.MODE_PRIVATE)
        name = sp.getString("name", "") ?: ""
        xp = sp.getInt("xp", 0)
        coins = sp.getInt("coins", 0)
        streak = sp.getInt("streak", 0)
        played = sp.getInt("played", 0)
        wins = sp.getInt("wins", 0)
        answered = sp.getInt("answered", 0)
        correct = sp.getInt("correct", 0)
        badges = sp.getStringSet("badges", emptySet())?.toSet() ?: emptySet()
        freezes = sp.getInt("freezes", 0)
        soundOn = sp.getBoolean("sound", true)
        mistakes = (sp.getStringSet("mistakes", emptySet()) ?: emptySet())
            .mapNotNull { it.toLongOrNull() }.toMutableSet()
        topicHits = (sp.getStringSet("topics", emptySet()) ?: emptySet())
            .mapNotNull { row ->
                val p = row.split("|")
                if (p.size == 3) p[0] to (p[1].toInt() to p[2].toInt()) else null
            }.toMap().toMutableMap()
        seen = (sp.getStringSet("seen", emptySet()) ?: emptySet())
            .mapNotNull { it.toLongOrNull() }.toMutableSet()
        touchStreak()
    }

    fun saveName(n: String) {
        name = n.trim()
        sp.edit().putString("name", name).apply()
    }

    fun seenIds(): Set<Long> = seen
    fun mistakeIds(): Set<Long> = mistakes
    fun topicStats(): Map<String, Pair<Int, Int>> = topicHits

    fun toggleSound() {
        soundOn = !soundOn
        com.orynexlab.neetbattleneetgame.game.Feedback.enabled = soundOn
        sp.edit().putBoolean("sound", soundOn).apply()
    }

    fun clearMistake(id: Long) {
        mistakes.remove(id)
        sp.edit().putStringSet("mistakes", mistakes.map { it.toString() }.toSet()).apply()
    }

    /** Called per answer so chapter accuracy stays live. */
    fun logAnswer(questionId: Long, topic: String, wasCorrect: Boolean) {
        if (wasCorrect) mistakes.remove(questionId) else mistakes.add(questionId)
        if (topic.isNotBlank()) {
            val (a, c) = topicHits[topic] ?: (0 to 0)
            topicHits[topic] = (a + 1) to (c + if (wasCorrect) 1 else 0)
        }
    }

    private fun persistLogs() {
        sp.edit()
            .putStringSet("mistakes", mistakes.map { it.toString() }.toSet())
            .putStringSet("topics", topicHits.map { (k, v) -> "$k|${v.first}|${v.second}" }.toSet())
            .apply()
    }

    private fun today() = Calendar.getInstance().let {
        it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR)
    }

    private fun touchStreak() {
        val last = sp.getInt("last_day", 0)
        val t = today()
        when {
            last == 0 -> { streak = 1 }
            t == last -> { /* already counted today */ }
            t == last + 1 -> streak += 1
            // a freeze absorbs exactly one missed day
            t == last + 2 && freezes > 0 -> {
                freezes -= 1
                streak += 1
                sp.edit().putInt("freezes", freezes).apply()
            }
            else -> streak = 1
        }
        sp.edit().putInt("last_day", t).putInt("streak", streak).apply()
        if (streak >= 7) award("streak7")
    }

    fun spend(amount: Int): Boolean {
        if (coins < amount) return false
        coins -= amount
        sp.edit().putInt("coins", coins).apply()
        return true
    }

    fun buyFreeze(): Boolean {
        if (coins < FREEZE_COST) return false
        coins -= FREEZE_COST
        freezes += 1
        sp.edit().putInt("coins", coins).putInt("freezes", freezes).apply()
        return true
    }

    fun award(id: String) {
        if (id in badges) return
        badges = badges + id
        sp.edit().putStringSet("badges", badges).apply()
    }

    /** Returns XP earned so the result screen can animate it. */
    fun recordMatch(
        won: Boolean,
        score: Int,
        correctCount: Int,
        total: Int,
        level: Level,
        fastestMs: Int,
        questionIds: List<Long>
    ): Int {
        val base = if (won) 60 else 20
        val gained = ((base + score / 12) * level.xpMult).toInt()

        xp += gained
        coins += if (won) 25 else 8
        played += 1
        if (won) wins += 1
        answered += total
        correct += correctCount
        seen += questionIds

        if (won) award("first_win")
        if (correctCount == total && total > 0) award("perfect")
        if (fastestMs in 1..2999) award("speed")
        if (answered >= 100) award("century")
        if (won && level == Level.HARD) award("brutal")

        sp.edit()
            .putInt("xp", xp).putInt("coins", coins).putInt("played", played)
            .putInt("wins", wins).putInt("answered", answered).putInt("correct", correct)
            .putStringSet("seen", seen.map { it.toString() }.toSet())
            .apply()
        persistLogs()

        return gained
    }

    /** Local board until the backend lands. Player is slotted in by XP. */
    fun localBoard(): List<LeaderRow> {
        // Target scores to beat, labelled as bots. Online play is not live yet,
        // so nothing here claims to be another person.
        val bots = listOf(
            "Bot · Diamond" to 8420, "Bot · Platinum" to 7180, "Bot · Gold II" to 6640,
            "Bot · Gold I" to 5910, "Bot · Silver III" to 5230, "Bot · Silver II" to 4470,
            "Bot · Silver I" to 3980, "Bot · Bronze III" to 3210, "Bot · Bronze II" to 2640,
            "Bot · Bronze I" to 2110, "Bot · Rookie II" to 1580, "Bot · Rookie I" to 1120
        )
        val all = (bots + (name.ifBlank { "you" } to xp))
            .sortedByDescending { it.second }
        return all.mapIndexed { i, (n, x) ->
            LeaderRow(i + 1, n, x, Levels.levelFor(x), n == name && name.isNotBlank())
        }
    }
}
