package com.nipun.oceanbin.ui

/*
 * This sealed class is a special class where we define all
 * main screens used in our app,
 * in xml we use activities for every main screen but jetpack compose is a single
 * screen app so we have to navigate screen with the help of navigation compose and this
 * class will help us, so that we don't forget any route of the screen.
 * Route is similar to website backend route.
 */
sealed class Screen(val route : String,val label : String = ""){
    object SplashScreen : Screen(route = "splash")
    object WhoAreYouScreen: Screen(route = "who are you?")
    object SplashSeaViewPager : Screen(route = "splashseaviewpager")
    object SplashBeachViewPager : Screen(route = "splashbviewpager")
    object DoLoginSignup:Screen(route="dologinsignup")
    object Login:Screen("login")
    object Signup:Screen("signup")
    object BottomScreen : Screen(route = "home")
}
