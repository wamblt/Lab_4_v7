package com.example.lab_4_v5

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

class MyApp: Application(){
    val protoDataStore by dataStore(fileName = "myProtoDataStore", serializer = SuperSerializer)
    val preferDataStore by preferencesDataStore(name = "testDataStore")
}

class CustomViewModel(dataStore1: DataStore<Preferences>, dataStore2: DataStore<MyProto>): ViewModel(){
    private lateinit var _darkMode: MutableStateFlow<Boolean>
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    init {
        viewModelScope.launch{
           dataStore1.data.first()
            dataStore2.data.first()
            _darkMode = MutableStateFlow(dataStore1.data.first().get(EXAMPLE_COUNTER)?:false)
            darkMode.first()

            dataStore1.data.collect{
                it[EXAMPLE_COUNTER]?.let { it1 -> _darkMode.emit(it1) }
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
    val darkModeFlag by model.darkMode.collectAsState()
    Lab_4_v5Theme()  {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = nav, startDestination = "/") {
                composable("main"){ Menu(cVM = model) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(cVM: CustomViewModel){
    Scaffold(topBar = { TopAppBar(title = {Text(text = "Dark mode toggles")}) }) {
            innerpadding -> Column (modifier = Modifier.padding(innerpadding), verticalArrangement = Arrangement.SpaceBetween){

        Column {
            Text(text = "PreferenceDataStore Toggle")
            Switch(checked = false, onCheckedChange = {
                cVM.darkMode.value
            })
        }
        Column {
            Text(text = "ProtoDataStore Toggle")
            Switch(checked = false, onCheckedChange = {

            })
        }
    }
    }
}


fun doTheDataStore(flow: StateFlow<Boolean?>){
    if (flow == null){

    }
}



//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            Lab_4_v5Theme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Lab_4_v5Theme {
//        Greeting("Android")
//    }
//}