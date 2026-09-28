package com.orynexlab.neetbattleneetgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orynexlab.neetbattleneetgame.data.Daily
import com.orynexlab.neetbattleneetgame.data.Level
import com.orynexlab.neetbattleneetgame.data.Phase
import com.orynexlab.neetbattleneetgame.data.Progress
import com.orynexlab.neetbattleneetgame.data.Subject
import com.orynexlab.neetbattleneetgame.game.Feedback
import com.orynexlab.neetbattleneetgame.game.MatchEngine
import com.orynexlab.neetbattleneetgame.ui.comp.BottomNav
import com.orynexlab.neetbattleneetgame.ui.comp.NavItem
import com.orynexlab.neetbattleneetgame.ui.screens.*
import com.orynexlab.neetbattleneetgame.ui.theme.NeetTheme
import com.orynexlab.neetbattleneetgame.ui.theme.Void

private enum class Route { SPLASH, LOGIN, SHELL, FINDING, DAILY_FIND, MATCH, RESULT }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Progress.init(applicationContext)
        Feedback.init(applicationContext)
        Feedback.enabled = Progress.soundOn
        Daily.init(applicationContext)
        setContent {
            NeetTheme { Surface(Modifier.fillMaxSize(), color = Void) { App() } }
        }
    }
}

@Composable
private fun App() {
    var route by remember { mutableStateOf(Route.SPLASH) }
    var tab by remember { mutableStateOf("home") }
    var matchKey by remember { mutableIntStateOf(0) }
    var subject by remember { mutableStateOf(Subject.BOTANY) }
    var level by remember { mutableStateOf(Level.MEDIUM) }

    BackHandler(enabled = route == Route.RESULT) { route = Route.SHELL }

    val navItems = listOf(
        NavItem("home", "⚔️", "battle"),
        NavItem("practice", "🎯", "revise"),
        NavItem("map", "🗺️", "map"),
        NavItem("ranks", "🏆", "ranks"),
        NavItem("me", "👤", "me"),
    )

    AnimatedContent(
        targetState = route,
        transitionSpec = {
            (fadeIn(tween(320)) + scaleIn(tween(380), initialScale = 0.985f))
                .togetherWith(fadeOut(tween(220)))
        },
        label = "route"
    ) { r: Route ->
        when (r) {
            Route.SPLASH -> SplashScreen {
                route = if (Progress.loggedIn) Route.SHELL else Route.LOGIN
            }

            Route.LOGIN -> LoginScreen { route = Route.SHELL }

            Route.SHELL -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        "home" -> HomeScreen(
                            onStart = { sub, lv ->
                                subject = sub
                                level = lv
                                matchKey++
                                route = Route.FINDING
                            },
                            onDaily = { matchKey++; route = Route.DAILY_FIND },
                            onBoard = { tab = "ranks" }
                        )
                        "practice" -> PracticeScreen()
                        "map" -> ChaptersScreen()
                        "ranks" -> LeaderboardScreen { tab = "home" }
                        "me" -> ProfileScreen()
                    }
                }
                BottomNav(navItems, tab) { tab = it }
            }

            Route.FINDING -> key(matchKey) {
                val vm: MatchEngine = viewModel(key = "match_$matchKey")
                LaunchedEffect(matchKey) { vm.load(subject, level) }
                MatchmakingScreen(vm) { vm.arm(); route = Route.MATCH }
            }

            Route.DAILY_FIND -> key(matchKey) {
                val vm: MatchEngine = viewModel(key = "match_$matchKey")
                LaunchedEffect(matchKey) { vm.loadDaily() }
                MatchmakingScreen(vm) { vm.arm(); route = Route.MATCH }
            }

            Route.MATCH -> key(matchKey) {
                val vm: MatchEngine = viewModel(key = "match_$matchKey")
                LaunchedEffect(vm.phase) {
                    if (vm.phase == Phase.FINISHED) {
                        vm.commit()
                        route = Route.RESULT
                    }
                }
                MatchScreen(vm)
            }

            Route.RESULT -> key(matchKey) {
                val vm: MatchEngine = viewModel(key = "match_$matchKey")
                ResultScreen(
                    vm = vm,
                    onAgain = { matchKey++; route = Route.FINDING },
                    onHome = { route = Route.SHELL },
                    onBoard = { tab = "ranks"; route = Route.SHELL }
                )
            }
        }
    }
}
