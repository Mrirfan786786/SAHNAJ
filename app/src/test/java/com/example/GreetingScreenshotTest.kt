package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.presentation.screens.SplashScreen
import com.example.ui.theme.SahNajAITheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @org.junit.Before
  fun setUp() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(
        context,
        com.google.firebase.FirebaseOptions.Builder()
          .setApplicationId("1:329101568335:android:test")
          .setApiKey("fakeApiKey12345")
          .setProjectId("sahnaj-ai")
          .build()
      )
    }
  }

  @Test
  fun splash_screenshot() {
    composeTestRule.setContent {
      SahNajAITheme {
        SplashScreen(
          isLoggedIn = true,
          isSetupCompleted = true,
          hasSeenOnboarding = true,
          onNavigateToLogin = {},
          onNavigateToOnboarding = {},
          onNavigateToSetup = {},
          onNavigateToAssistant = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/splash.png")
  }
}

