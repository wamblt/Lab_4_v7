package com.example.lab_4_v5

import android.content.Context
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import app.src.main.java.MyProto
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.jupiter.api.*


class MainActivityKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

     private val textContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope =
        TestCoroutineScope(testCoroutineDispatcher + Job())

    private val preferDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = testCoroutineScope, produceFile = {textContext.preferencesDataStoreFile("testPreferDataStore")})
    private val protoDataStore: DataStore<MyProto> = DataStoreFactory.create(scope = testCoroutineScope, produceFile = {textContext.dataStoreFile("testname")}, serializer = SuperSerializer)


    //UI tests
    @Test
    fun preferSwitchCheck(){
        composeTestRule.setContent {
            Menu(cVM = CustomViewModel(dataStore1 = preferDataStore, dataStore2 = protoDataStore))
        }

        composeTestRule.onNode(hasText("PreferenceDataStore Toggle")).assertExists()
    }
}