package com.orynexlab.neetbattleneetgame.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

/**
 * The same 10 questions for everyone, every day, with no server involved:
 * the day number seeds the shuffle, so every device derives an identical paper.
 * Playable once per day.
 */
object Daily {

    private lateinit var sp: SharedPreferences

    var playedToday by mutableStateOf(false); private set
    var todayScore by mutableIntStateOf(0); private set
    var todayCorrect by mutableIntStateOf(0); private set
    var bestScore by mutableIntStateOf(0); private set
    var dailyStreak by mutableIntStateOf(0); private set

    /** IST so the reset lines up with the user's actual day. */
    private fun dayNumber(): Int {
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("neet_daily", Context.MODE_PRIVATE)
        val today = dayNumber()
        val last = sp.getInt("last_played_day", 0)
        playedToday = last == today
        todayScore = if (playedToday) sp.getInt("today_score", 0) else 0
        todayCorrect = if (playedToday) sp.getInt("today_correct", 0) else 0
        bestScore = sp.getInt("best_score", 0)
        dailyStreak = sp.getInt("daily_streak", 0)

        // missed more than one day, chain broken
        if (last != 0 && last < today - 1) {
            dailyStreak = 0
            sp.edit().putInt("daily_streak", 0).apply()
        }
    }

    /** Mixed subjects, mixed difficulty, deterministic from the date. */
    fun questions(ctx: Context): List<Question> {
        val seed = dayNumber().toLong()

        // NEET's own weighting: 4 botany, 3 zoology, 2 physics, 1 chemistry
        val quota = listOf(
            Subject.BOTANY to 3,
            Subject.ZOOLOGY to 3,
            Subject.PHYSICS to 2,
            Subject.CHEMISTRY to 2
        )

        val set = quota.flatMap { (subj, n) ->
            QuestionBank.load(ctx, subj)
                .sortedBy { (it.id * 2654435761L + seed).hashCode() }
                .take(n)
        }

        if (set.isEmpty()) return emptyList()

        return set.sortedBy { (it.id + seed).hashCode() }
            .map { QuestionBank.shuffleOptions(it, Random(seed + it.id)).copy(timeLimit = 15) }
    }

    /** Everyone faces the same opponent run too, so the comparison is fair. */
    fun ghost(): List<GhostAnswer> {
        val rng = Random(dayNumber().toLong() * 31)
        return (0 until 10).map { i ->
            val t = rng.nextInt(2800, 12800)
            val ok = rng.nextInt(100) < 66
            GhostAnswer(i, t, ok, if (ok) 100 + ((15000 - t) * 100 / 15000) else 0)
        }
    }

    fun record(score: Int, correct: Int) {
        val today = dayNumber()
        val last = sp.getInt("last_played_day", 0)
        if (last == today) return

        dailyStreak = if (last == today - 1) dailyStreak + 1 else 1
        playedToday = true
        todayScore = score
        todayCorrect = correct
        if (score > bestScore) bestScore = score

        sp.edit()
            .putInt("last_played_day", today)
            .putInt("today_score", score)
            .putInt("today_correct", correct)
            .putInt("best_score", bestScore)
            .putInt("daily_streak", dailyStreak)
            .apply()
    }

    /** Hours and minutes until the next IST midnight. */
    fun resetsIn(): Pair<Int, Int> {
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val h = 23 - c.get(Calendar.HOUR_OF_DAY)
        val m = 59 - c.get(Calendar.MINUTE)
        return h to m
    }
}
