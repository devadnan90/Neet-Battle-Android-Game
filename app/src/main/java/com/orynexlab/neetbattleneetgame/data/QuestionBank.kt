package com.orynexlab.neetbattleneetgame.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlin.random.Random

object QuestionBank {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val cache = mutableMapOf<Subject, List<Question>>()

    fun load(ctx: Context, subject: Subject): List<Question> = cache.getOrPut(subject) {
        try {
            val raw = ctx.assets.open(subject.asset).bufferedReader().use { it.readText() }
            json.decodeFromString<QuestionFile>(raw).questions
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun sizeOf(ctx: Context, subject: Subject) = load(ctx, subject).size

    /**
     * Pulls n questions of the requested level, skipping ones the player has already
     * seen. Once the unseen pool runs dry it recycles the oldest seen questions rather
     * than returning a short round.
     */
    /**
     * Reorders the four options and remaps the correct index.
     * Without this, any bias in how the bank was written (say, most answers
     * sitting at B) becomes a pattern players can exploit without knowing the
     * material. Shuffling at serve time makes the bank's own balance irrelevant.
     */
    fun shuffleOptions(q: Question, rng: Random = Random.Default): Question {
        val opts = listOf(q.a, q.b, q.c, q.d)
        val order = opts.indices.shuffled(rng)
        val newCorrect = order.indexOf(q.correctIndex)
        return q.copy(
            a = opts[order[0]],
            b = opts[order[1]],
            c = opts[order[2]],
            d = opts[order[3]],
            correct = "abcd"[newCorrect].toString()
        )
    }

    fun draw(ctx: Context, subject: Subject, level: Level, n: Int, seen: Set<Long>): List<Question> {
        val all = load(ctx, subject)
        if (all.isEmpty()) return emptyList()

        val pool = if (level == Level.MIXED) all
        else all.filter { it.difficulty.equals(level.key, true) }.ifEmpty { all }

        val fresh = pool.filter { it.id !in seen }.shuffled()
        val picked = fresh.take(n).toMutableList()

        if (picked.size < n) {
            val filler = pool.filter { q -> picked.none { it.id == q.id } }.shuffled()
            picked += filler.take(n - picked.size)
        }
        return picked.map { shuffleOptions(it).copy(timeLimit = level.secs) }
    }

    // Clearly-labelled pacers, not fake humans. The opponent is a target run,
    // and the app says so rather than implying a live player.
    private val BOT_NAMES = listOf(
        "Pacer Bot", "Rival Bot", "Sprint Bot", "Tempo Bot", "Chaser Bot"
    )

    /** Builds an opponent run. Skill scales with the player's level. */
    fun buildGhost(questions: List<Question>, playerLevel: Int, level: Level): Pair<String, List<GhostAnswer>> {
        val skill = (0.52f + playerLevel * 0.012f).coerceAtMost(0.82f)
        val limit = level.secs * 1000
        val ghost = questions.mapIndexed { i, _ ->
            val t = Random.nextInt((limit * 0.18f).toInt(), (limit * 0.88f).toInt())
            val ok = Random.nextFloat() < skill
            GhostAnswer(i, t, ok, if (ok) 100 + ((limit - t) * 100 / limit) else 0)
        }
        return BOT_NAMES.random() to ghost
    }
}
