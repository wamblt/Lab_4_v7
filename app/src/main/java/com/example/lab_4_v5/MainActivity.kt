package com.example.lab_4_v5

import android.app.Application
import android.content.res.Resources.Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.src.main.java.MyProto
import com.example.lab_4_v5.ui.theme.Lab_4_v5Theme
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.OutputStream

val EXAMPLE_COUNTER = booleanPreferencesKey("example_counter")



//adapted from Android Studio docs for ProtoDataStore
object SuperSerializer: Serializer<MyProto> {
    override val defaultValue: MyProto = MyProto.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): MyProto {
        try {
            return MyProto.parseFrom(input)
        }catch (exception: InvalidProtocolBufferException){
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: MyProto, output: OutputStream) = t.writeTo(output)

}
class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            App()
        }
    }
}

//contains datastores to be used and updated
class MyApp: Application(){
    val protoDataStore by dataStore(fileName = "myProtoDataStore", serializer = SuperSerializer)
    val preferDataStore by preferencesDataStore(name = "testDataStore")
}

class CustomViewModel(val dataStore1: DataStore<Preferences>, val dataStore2: DataStore<MyProto>): ViewModel(){
    private lateinit var _preferDarkMode: MutableStateFlow<Boolean>
    private lateinit var _protoDarkMode: MutableStateFlow<Boolean>

    val preferDarkMode: StateFlow<Boolean> get() = _preferDarkMode.asStateFlow()
    val protoDarkMode: StateFlow<Boolean> get() = _protoDarkMode.asStateFlow()


    val readFromPreferDataStore: Flow<Boolean> = dataStore1.data.map {
        it[EXAMPLE_COUNTER] ?: true
    }

     fun saveToPreferDataStore() {
        viewModelScope.launch {
            dataStore1.edit {
                val current = it[EXAMPLE_COUNTER] ?: true
                it[EXAMPLE_COUNTER] = !current
            }
        }
    }

    val readFromProtoDataStore: StateFlow<Boolean> get() = _protoDarkMode.asStateFlow()
    fun saveToProtoDataStore(){
        viewModelScope.launch {
            dataStore2.updateData {
                it.toBuilder().setExampleCounter(!it.exampleCounter).build()
            }
        }
    }



    init {
        //inits the internal darkmode vals
        runBlocking {
            _preferDarkMode = MutableStateFlow(dataStore1.data.first().get(EXAMPLE_COUNTER)?:false)
            _protoDarkMode = MutableStateFlow(dataStore2.data.first().exampleCounter?:false)
        }
        //inits the datastores for use in other functions
        viewModelScope.launch{
            dataStore1.data.collect{
                it[EXAMPLE_COUNTER]?.let { it1 -> _preferDarkMode.emit(it1) }
            }
        }
        viewModelScope.launch {
            dataStore2.data.collect{
                _protoDarkMode.emit(it.exampleCounter)
            }
        }
    }

    companion object Factory{

        val Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApp)
                CustomViewModel(app.preferDataStore, app.protoDataStore)
            }
        }
    }
}

@Composable
fun App(model: CustomViewModel = viewModel(factory = CustomViewModel.Factory)){
    val nav = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "main") {
            composable("main"){ Menu(cVM = model) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(cVM: CustomViewModel){
    val preferDarkMode by cVM.preferDarkMode.collectAsState()
    val protoDarkMode by cVM.protoDarkMode.collectAsState()

    Lab_4_v5Theme(darkTheme = preferDarkMode || protoDarkMode) {
        Scaffold(topBar = { TopAppBar(title = {Text(text = "Dark mode playground")}) }) {
                innerpadding -> Column (modifier = Modifier.padding(innerpadding), verticalArrangement = Arrangement.SpaceBetween){
            Column {
                Text(text = "Toggles")
            }
            Column {
                Text(text = "PreferenceDataStore Toggle")
                Switch(checked = preferDarkMode, modifier = Modifier.testTag("preferSwitch"), onCheckedChange = {
                    cVM.saveToPreferDataStore()
                })
            }
            Column {
                Text(text = "ProtoDataStore Toggle")
                Switch(checked = protoDarkMode, modifier = Modifier.testTag("protoSwitch"), onCheckedChange = {
                    cVM.saveToProtoDataStore()
                })
            }
            Column {
                Text(text = "Checkboxes")
            }
            Column {
                Text(text = "PreferenceDataStore Box")
                Checkbox(checked = preferDarkMode, onCheckedChange = {}, modifier = Modifier.testTag("preferCheck"))
            }
            Column {
                Text(text = "ProtoDataStore Box")
                Checkbox(checked = protoDarkMode, onCheckedChange = {}, modifier = Modifier.testTag("protoCheck"))
            }

            }
        }
    }
}




