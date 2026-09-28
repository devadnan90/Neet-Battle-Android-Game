package com.orynexlab.neetbattleneetgame.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Long,
    @SerialName("q_text") val text: String,
    @SerialName("opt_a") val a: String,
    @SerialName("opt_b") val b: String,
    @SerialName("opt_c") val c: String,
    @SerialName("opt_d") val d: String,
    @SerialName("correct_opt") val correct: String,
    val solution: String,
    val chapter: String = "",
    val topic: String = "",
    val difficulty: String = "medium",
    @SerialName("time_limit_s") val timeLimit: Int = 15
) {
    fun optionAt(i: Int) = when (i) { 0 -> a; 1 -> b; 2 -> c; else -> d }
    val correctIndex: Int get() = when (correct.lowercase()) { "a" -> 0; "b" -> 1; "c" -> 2; else -> 3 }
}

@Serializable
data class QuestionFile(val questions: List<Question>)

@Serializable
data class GhostAnswer(
    val seq: Int,
    @SerialName("time_ms") val timeMs: Int,
    val correct: Boolean,
    val points: Int
)

data class MatchPacket(
    val matchUid: String,
    val opponentName: String,
    val opponentElo: Int,
    val questions: List<Question>,
    val ghost: List<GhostAnswer>
)

data class LeaderRow(
    val rank: Int,
    val name: String,
    val xp: Int,
    val level: Int,
    val isMe: Boolean = false
)

enum class Subject(
    val label: String, val code: String, val asset: String,
    val emoji: String, val idBase: Long
) {
    BOTANY("Botany", "BOT", "questions_botany.json", "🌿", 3_000_000L),
    ZOOLOGY("Zoology", "ZOO", "questions_zoology.json", "🦋", 4_000_000L),
    PHYSICS("Physics", "PHY", "questions_physics.json", "⚡", 1_000_000L),
    CHEMISTRY("Chemistry", "CHE", "questions_chemistry.json", "⚗️", 2_000_000L)
}

enum class Level(val label: String, val key: String, val secs: Int, val xpMult: Float, val emoji: String) {
    EASY("Chill", "easy", 20, 1.0f, "🌱"),
    MEDIUM("Real", "medium", 15, 1.5f, "🔥"),
    HARD("Brutal", "hard", 10, 2.5f, "💀"),
    MIXED("Mixed", "mixed", 15, 1.8f, "🎲")
}

enum class Phase { COUNTDOWN, PLAYING, REVEAL, FINISHED }

/** XP thresholds. Level n needs 250 * n * (n+1) / 2 total XP. */
object Levels {
    fun levelFor(xp: Int): Int {
        var lv = 1
        while (xp >= xpForLevel(lv + 1)) lv++
        return lv
    }
    fun xpForLevel(lv: Int) = 250 * (lv - 1) * lv / 2
    fun progressIn(xp: Int): Float {
        val lv = levelFor(xp)
        val lo = xpForLevel(lv); val hi = xpForLevel(lv + 1)
        return if (hi == lo) 0f else (xp - lo).toFloat() / (hi - lo).toFloat()
    }
    fun title(lv: Int) = when {
        lv < 3 -> "Fresher"
        lv < 6 -> "Grinder"
        lv < 10 -> "Sniper"
        lv < 15 -> "Topper"
        lv < 22 -> "Beast"
        else -> "AIR Chaser"
    }
}

data class Badge(val id: String, val emoji: String, val name: String, val desc: String)

object Badges {
    val ALL = listOf(
        Badge("first_win", "🥇", "First Blood", "Win your first battle"),
        Badge("streak7", "🔥", "Week Warrior", "7 day streak"),
        Badge("perfect", "💯", "Flawless", "10/10 in one battle"),
        Badge("speed", "⚡", "Speed Demon", "Answer in under 3 seconds"),
        Badge("century", "💪", "Century", "Answer 100 questions"),
        Badge("brutal", "💀", "Masochist", "Win on Brutal level"),
    )
}
