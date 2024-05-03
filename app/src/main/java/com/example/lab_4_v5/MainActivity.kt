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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.src.main.java.MyProto
import com.example.lab_4_v5.ui.theme.Lab_4_v5Theme
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


private val Context.preferDataStore by preferencesDataStore(name = "testDataStore")
val EXAMPLE_COUNTER = intPreferencesKey("example_counter")

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

    val Context.protoDataStore by dataStore(fileName = "myProtoDataStore", serializer = SuperSerializer)
}

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            Menu()
        }
    }
}

class MyApp: Application(){

}

class CustomViewModel(): ViewModel(){
    companion object Factory{

        val Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApp)
                CustomViewModel()
            }
        }
    }
}

@Composable
fun App(){
    val nav = rememberNavController()
    Lab_4_v5Theme  {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(navController = nav, startDestination = "/") {
                composable("main"){ Menu()}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(){
    Scaffold(topBar = { TopAppBar(title = {Text(text = "Dark mode toggles")}) }) {
            innerpadding -> Column (modifier = Modifier.padding(innerpadding), verticalArrangement = Arrangement.SpaceBetween){

        Column {
            Text(text = "PreferenceDataStore Toggle")
            Switch(checked = false, onCheckedChange = {})
        }
        Column {
            Text(text = "ProtoDataStore Toggle")
            Switch(checked = false, onCheckedChange = {})
        }
    }
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