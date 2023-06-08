package com.example.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo.ui.theme.DemoTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting(vm: UserViewModel = viewModel()) {
    val state = vm.state.collectAsState()

    Column {
        when (state.value) {
            is UserBloc.UiState.Loading -> {
                Text("Loading")
            }
            is UserBloc.UiState.Success -> {
                Text((state.value as UserBloc.UiState.Success).data.name)
            }
            is UserBloc.UiState.Error -> {
                Text("${(state.value as UserBloc.UiState.Error).error.message}")
            }
        }
        Button(
            onClick = { UserBloc(vm = vm, uiAction = UserBloc.UiAction.Success) },
            content = { Text("click") }
        )
        Button(
            onClick = {
                UserBloc(vm = vm, uiAction = UserBloc.UiAction.Error, error = Error("error"))
            },
            content = { Text("Click me") }
        )
    }
}

object UserBloc {
    data class User(val name: String, val age: Int)

    sealed class UiState {
        object Loading : UiState()
        data class Success(val data: User) : UiState()
        data class Error(val error: Throwable) : UiState()
    }

    sealed interface UiAction {
        object Success : UiAction
        object Error : UiAction
    }

    operator fun invoke(
        vm: UserViewModel,
        uiAction: UiAction,
        error: Error? = null,
    ) {
        when (uiAction) {
            is UiAction.Success -> {
                vm.success()
            }
            is UiAction.Error -> {
                if (error != null) {
                    vm.error(error)
                }
            }
        }
    }
}

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UserBloc.UiState>(UserBloc.UiState.Loading)
    val state = _uiState.asStateFlow()

    fun success() {
        _uiState.update { currState ->
            if (currState is UserBloc.UiState.Loading) {
                UserBloc.UiState.Success(UserBloc.User("name", 1))
            } else {
                currState
            }
        }
    }

    fun error(error: Error) {
        _uiState.update { currState ->
            if (currState is UserBloc.UiState.Success) {
                UserBloc.UiState.Error(error)
            } else {
                currState
            }
        }
    }
}
