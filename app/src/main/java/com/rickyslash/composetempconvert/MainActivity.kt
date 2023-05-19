package com.rickyslash.composetempconvert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rickyslash.composetempconvert.ui.theme.ComposeTempConvertTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTempConvertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    StatefulTempInput()
                }
            }
        }
    }
}

@Composable
fun StatefulTempInput(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    Column(modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.stateful_converter),
            style = MaterialTheme.typography.h5)
        OutlinedTextField(
            value = input,
            onValueChange = { newInput ->
                input = newInput
                output = celciusToFahrenheit(newInput)
            },
            label = { Text(stringResource(R.string.enter_celsius)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(stringResource(R.string.temperature_fahrenheit, output))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeTempConvertTheme {
    }
}

private fun celciusToFahrenheit(celcius: String) = celcius.toDoubleOrNull()?.let { (it * (9/5)) + 32 }.toString()

// State is a component that contains value to dynamically affect UI
// UI Update Loop: 0 -> Event -> Renew State -> Display State -> 0

// Event is anything that cause a State to change
/* example: `onValueChange`
@Composable
fun FormInput() {
    var name by remember { mutableStateOf("") } //state
    OutlinedTextField(
        value = name, // display state
        onValueChange = { newName -> // event
            name = newName //update state
        },
        label = { Text("Name") },
        modifier = Modifier.padding(8.dp)
    )
}*/

// Jetpack Compose applies UDF (Unidirectional Flow) handle Events & State:
// (State) -> State -> (UI) -> Event -> (State)

// Benefits in following UDF Pattern:
// - Testability: separating state & UI so the test between both component will be easier
// - State Encapsulation: State chang process could only be done in 1 place, so the potential of bug could be decreased
// - UI Consistency: State that is being made is directly reflected to UI, making it consistent both ways

// `mutableStateOf` used to track a state, which has a type of 'observable' in Compose
// `remember` used to save value to memory so the data is still available when recomposition
// `rememberSavable` used to save state in bundle, so it save State when there is configuration change (like rotation)

// 2 type of Composable Function:
// - Stateful: save State inside it
// --- useful when parent composable didn't have to control child composable & child composable is independent in managing its composable
// --- it is less reusable and hard to test, because the logic inside hard to be managed
// - Stateless: not save State inside it
// --- more flexible. Could manage logic outside. Easier to test

// to change Stateful Composable function into Stateless, we need State Hoisting

// State Hoisting is a pattern to move State to parent above it (caller composable), so the Composable becoming Stateless
// benefit in doing State Hoisting:
// - Single Source of Truth (from the caller)
// - Shareable: can be used to more than one composable
// - Interceptable: caller can choose to use Event or not
// - Decoupled: State can be separated from the Composable, like in ViewModel

// to implement State Hoisting, State variable nee to be changed in 2 parameter:
// - `value: T` (State's value)
// - `onEvent: (T) -> Unit` (Lambda that shows occurring Events)
/* example:
@Composable
fun StatefulCounter(modifier: Modifier = Modifier) {
    var count by rememberSaveable { mutableStateOf(0) }
    StatelessCounter(
        count = count,
        onClick = { count++ },
        modifier = modifier,
    )
}

@Composable
fun StatelessCounter(
    count: Int,           //state
    onClick : () -> Unit  //event,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        Text("Button clicked $count times:")
        Button(onClick = { onClick() }) {
            Text("Click me!")
        }
    }
}*/

// best practice to place State of a component:
// - place State on 'lowest level parent Composable' that 'read the State'
// --- means to place the State variable to the parent composable that is 'closest to the child that read the state'
/* example:
@Composable
fun ParentComposable() {
    val state = remember { mutableStateOf("Hello") }
    ChildComposable(state.value)
}

@Composable
fun ChildComposable(stateValue: String) {
    Text(text = stateValue)
}
*/
// - place State on 'highest level parent Composable' that 'write the State'
// --- means to place State variable to the farther up of the composition hierarchy (closest to the root)
/* example:
@Composable
fun OuterComposable(stateValue: String, onStateChange: (String) -> Unit) {
    // OuterComposable may have some other child composable

    InnerComposable(stateValue, onStateChange)
}

@Composable
fun InnerComposable(stateValue: String, onStateChange: (String) -> Unit) {
    // InnerComposable may have some other child composable

    Button(onClick = { onStateChange("New Value") }) {
        Text(text = stateValue)
    }
}*/
// - if '2 state changed by the same event', place state on the 'same level'
/* example:
@Composable
fun MyScreen() {
    // State only available in 'MyScreen'
    var checked by remember { mutableStateOf(false) }
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ){
        MySwitch(checked = checked, onCheckChanged = {checked = it})
        Text(
            text = if(checked) "ON" else "OFF",
            Modifier.clickable {
                checked = !checked
            }
        )
    }
}

// checked is Immutable (can't be changed)
@Composable
fun MySwitch(checked: Boolean, onCheckChanged: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = {
            onCheckChanged(it)
        }
    )
}*/