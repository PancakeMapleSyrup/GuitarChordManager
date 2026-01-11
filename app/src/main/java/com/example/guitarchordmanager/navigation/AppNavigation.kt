package com.example.guitarchordmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

import com.example.guitarchordmanager.login.LoginScreen
import com.example.guitarchordmanager.songlist.SongListScreen
import com.example.guitarchordmanager.songdetail.SongDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // startDestination: 앱 켜자마자 보일 화면 이름
    NavHost(navController = navController, startDestination = "LoginScreen") {

        // LoginScreen 화면
        composable("LoginScreen") {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 신호가 오면 실행될 코드
                    navController.navigate("SongListScreen") {
                        // ⭐️ 뒤로가기 눌렀을 때 로그인 화면으로 돌아가게 함. 풀고 싶으면 주석 해제
                        // popUpTo("LoginScreen") { inclusive = true }
                    }
                }
            )
        }

        // SongListScreen 화면
        composable(
            route = "SongListScreen",
            // [SongDetailScreen으로 갈 때] 화면이 왼쪽으로 살짝 밀려남
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(400)
                )
            },
            // [SongListScreen으로 돌아올 때] 화면이 왼쪽에서 다시 들어옴
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400)
                )
            }
            ) {
            SongListScreen(
                onSongClick = { song ->
                    // SongDetailScreen으로 이동
                    navController.navigate("detail/${song.id}")
                }
            )
        }

        // SongDetailScreen 화면
        composable(
            route = "detail/{songId}",
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType }
            ),
            // [SongDetailScreen으로 들어올 때] 오른쪽에서 왼쪽으로 들어옴
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                )
            },
            // [뒤로가기 할 때] 오른쪽으로 빠져나감
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400)
                )
            }
        ) {
            backStackEntry ->
            SongDetailScreen(
                onBackClick = { navController.popBackStack() } // 뒤로가기
            )
        }
    }
}
