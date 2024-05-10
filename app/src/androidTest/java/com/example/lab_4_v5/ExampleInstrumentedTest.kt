package com.example.lab_4_v5

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.src.main.java.MyProto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val textContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testCoroutineScope =
        TestCoroutineScope(testCoroutineDispatcher + Job())

    private val preferDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = testCoroutineScope,
        produceFile = { textContext.preferencesDataStoreFile("testPreferDataStore") })
    private val protoDataStore: DataStore<MyProto> = DataStoreFactory.create(
        scope = testCoroutineScope,
        produceFile = { textContext.dataStoreFile("testname") },
        serializer = SuperSerializer
    )


    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    //UI tests
    @Test
    fun switchCheck() {
        composeTestRule.setContent {
            Menu(cVM = CustomViewModel(dataStore1 = preferDataStore, dataStore2 = protoDataStore))
        }

        composeTestRule.onNode(hasText("PreferenceDataStore Toggle")).assertExists()
        composeTestRule.onNode(hasText("ProtoDataStore Toggle")).assertExists()
    }

    @Test
    fun checkboxCheck() {
        composeTestRule.setContent {
            Menu(cVM = CustomViewModel(preferDataStore, protoDataStore))
        }

        composeTestRule.onNode(hasText("PreferenceDataStore Box")).assertExists()
        composeTestRule.onNode(hasText("ProtoDataStore Box")).assertExists()
    }

    @Test
    fun isValueUpdated() {
            composeTestRule.waitForIdle()
            composeTestRule.setContent {
                Menu(cVM = CustomViewModel(preferDataStore, protoDataStore))
            }

            composeTestRule.onNodeWithTag("preferSwitch").performClick().assertIsOn()
            composeTestRule.onNodeWithTag("protoSwitch").performClick().assertIsOn()
        composeTestRule.onNodeWithTag("protoCheck").assertIsOn()
        composeTestRule.onNodeWithTag("preferCheck").assertIsOn()

    }


    // DataStore tests
    @Test
    fun UpdateDataStores(){
        val cVM = CustomViewModel(preferDataStore, protoDataStore)

        cVM.saveToPreferDataStore()
        cVM.saveToProtoDataStore()

        runBlocking {
            assertTrue(cVM.preferDarkMode.first())
            assertTrue(cVM.protoDarkMode.first())
        }

    }

}


