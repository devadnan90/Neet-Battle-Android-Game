package com.orynexlab.neetbattleneetgame.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orynexlab.neetbattleneetgame.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TICK_MS = 50L
private const val REVEAL_MS = 1500L
private const val MAX_BASE = 100
private const val MAX_SPEED_BONUS = 100

class MatchEngine(app: Application) : AndroidViewModel(app) {

    var packet by mutableStateOf<MatchPacket?>(null); private set
    var loading by mutableStateOf(true); private set

    var phase by mutableStateOf(Phase.COUNTDOWN); private set
    var index by mutableIntStateOf(0); private set
    var elapsedMs by mutableIntStateOf(0); private set
    var myScore by mutableIntStateOf(0); private set
    var ghostScore by mutableIntStateOf(0); private set
    var selected by mutableStateOf<Int?>(null); private set
    var countdown by mutableIntStateOf(3); private set
    var ghostAnsweredThisQ by mutableStateOf(false); private set

    var lastGain by mutableIntStateOf(0); private set
    var runStreak by mutableIntStateOf(0); private set
    private var lastTickSec = -1
    var xpGained by mutableIntStateOf(0); private set
    var level: Level = Level.MEDIUM; private set
    var isDaily = false; private set

    // power-ups
    var eliminated by mutableStateOf(setOf<Int>()); private set
    var usedFifty = false; private set
    var usedFreeze = false; private set
    var usedSkip = false; private set
    var frozen by mutableStateOf(false); private set

    val myAnswers = mutableListOf<Int?>()

    private var loop: Job? = null
    private var armed = false
    private var recorded = false
    private var fastestMs = Int.MAX_VALUE

    private val limitMs get() = (question?.timeLimit ?: level.secs) * 1000

    val question: Question? get() = packet?.questions?.getOrNull(index)
    val total: Int get() = packet?.questions?.size ?: 0
    val remainingMs: Int get() = (limitMs - elapsedMs).coerceAtLeast(0)
    val progress: Float get() = remainingMs.toFloat() / limitMs.toFloat()

    /** Draws questions from the on-device bank. The matchmaking screen owns the wait. */
    fun loadDaily() {
        if (packet != null) return
        isDaily = true
        level = Level.MEDIUM
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val p = withContext(Dispatchers.IO) {
                MatchPacket(
                    matchUid = "DAILY",
                    opponentName = "today's par",
                    opponentElo = 1200,
                    questions = Daily.questions(ctx),
                    ghost = Daily.ghost()
                )
            }
            packet = p
            loading = false
        }
    }

    fun load(subject: Subject, lvl: Level) {
        if (packet != null) return
        level = lvl
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val p = withContext(Dispatchers.IO) {
                val qs = QuestionBank.draw(ctx, subject, lvl, 10, Progress.seenIds())
                val (opp, ghost) = QuestionBank.buildGhost(qs, Progress.level, lvl)
                MatchPacket(
                    matchUid = "M-" + System.currentTimeMillis(),
                    opponentName = opp,
                    opponentElo = 1000 + Progress.level * 40,
                    questions = qs,
                    ghost = ghost
                )
            }
            packet = p
            loading = false
        }
    }

    /** Handed over from the matchmaking animation. */
    fun arm() {
        if (armed || packet == null) return
        armed = true
        viewModelScope.launch {
            phase = Phase.COUNTDOWN
            for (i in 3 downTo 1) { countdown = i; Feedback.countdown(i); delay(700) }
            beginQuestion()
        }
    }

    private fun beginQuestion() {
        lastTickSec = -1
        eliminated = emptySet()
        frozen = false
        elapsedMs = 0
        selected = null
        ghostAnsweredThisQ = false
        phase = Phase.PLAYING
        loop?.cancel()
        loop = viewModelScope.launch {
            val ghost = packet?.ghost?.getOrNull(index)
            while (phase == Phase.PLAYING && elapsedMs < limitMs) {
                delay(TICK_MS)
                if (!frozen) elapsedMs += TICK_MS.toInt()

                val secLeft = remainingMs / 1000
                if (secLeft <= 3 && secLeft != lastTickSec) {
                    lastTickSec = secLeft
                    if (secLeft > 0) Feedback.tick()
                }
                if (ghost != null && !ghostAnsweredThisQ && elapsedMs >= ghost.timeMs) {
                    ghostAnsweredThisQ = true
                    ghostScore += ghost.points
                }
            }
            if (phase == Phase.PLAYING) submit(null)
        }
    }

    fun submit(optionIndex: Int?) {
        if (phase != Phase.PLAYING) return
        loop?.cancel()
        selected = optionIndex
        myAnswers.add(optionIndex)

        val isCorrect = optionIndex != null && optionIndex == question?.correctIndex
        if (isCorrect && elapsedMs < fastestMs) fastestMs = elapsedMs

        lastGain = if (isCorrect) MAX_BASE + (remainingMs * MAX_SPEED_BONUS) / limitMs else 0
        myScore += lastGain

        question?.let { Progress.logAnswer(it.id, it.topic, isCorrect) }

        if (isCorrect) { runStreak++; Feedback.correct(runStreak) }
        else { runStreak = 0; if (optionIndex == null) Feedback.timeout() else Feedback.wrong() }

        val ghost = packet?.ghost?.getOrNull(index)
        if (ghost != null && !ghostAnsweredThisQ) {
            ghostAnsweredThisQ = true
            ghostScore += ghost.points
        }

        phase = Phase.REVEAL
        viewModelScope.launch {
            delay(REVEAL_MS)
            if (index + 1 < total) { index++; beginQuestion() } else phase = Phase.FINISHED
        }
    }

    /** Removes two wrong options. Costs coins, once per match. */
    fun useFifty(): Boolean {
        if (usedFifty || phase != Phase.PLAYING) return false
        if (!Progress.spend(40)) return false
        usedFifty = true
        val correct = question?.correctIndex ?: return false
        eliminated = (0..3).filter { it != correct }.shuffled().take(2).toSet()
        Feedback.tap()
        return true
    }

    /** Pauses the clock for five seconds. */
    fun useFreeze(): Boolean {
        if (usedFreeze || phase != Phase.PLAYING) return false
        if (!Progress.spend(60)) return false
        usedFreeze = true
        frozen = true
        Feedback.tap()
        viewModelScope.launch { delay(5000); frozen = false }
        return true
    }

    /** Moves on without scoring and without counting it as wrong. */
    fun useSkip(): Boolean {
        if (usedSkip || phase != Phase.PLAYING) return false
        if (!Progress.spend(80)) return false
        usedSkip = true
        loop?.cancel()
        myAnswers.add(null)
        Feedback.tap()
        phase = Phase.REVEAL
        viewModelScope.launch {
            delay(900)
            if (index + 1 < total) { index++; beginQuestion() } else phase = Phase.FINISHED
        }
        return true
    }

    /** Called once when the match ends, so XP is never double-counted. */
    fun commit() {
        if (recorded) return
        recorded = true
        val before = Progress.level
        xpGained = Progress.recordMatch(
            won = won,
            score = myScore,
            correctCount = correctCount,
            total = total,
            level = level,
            fastestMs = if (fastestMs == Int.MAX_VALUE) 0 else fastestMs,
            questionIds = packet?.questions?.map { it.id } ?: emptyList()
        )
        if (isDaily) Daily.record(myScore, correctCount)
        leveledUp = Progress.level > before
        if (leveledUp) Feedback.levelUp() else if (won) Feedback.win() else Feedback.lose()
    }

    var leveledUp by mutableStateOf(false); private set

    val won: Boolean get() = myScore > ghostScore
    val drawn: Boolean get() = myScore == ghostScore

    val eloDelta: Int get() {
        val opp = packet?.opponentElo ?: 1200
        val mine = 1000 + Progress.level * 40
        val expected = 1f / (1f + Math.pow(10.0, ((opp - mine) / 400.0)).toFloat())
        val actual = if (drawn) 0.5f else if (won) 1f else 0f
        return Math.round(32 * (actual - expected))
    }

    val correctCount: Int get() = myAnswers.withIndex().count { (i, ans) ->
        ans != null && ans == packet?.questions?.getOrNull(i)?.correctIndex
    }

    override fun onCleared() { loop?.cancel(); super.onCleared() }
}
