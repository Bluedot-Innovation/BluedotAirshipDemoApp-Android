package io.bluedot.airshipdemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import au.com.bluedot.point.net.engine.GeoTriggeringService
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.MainApplication
import io.bluedot.airshipdemo.R
import io.bluedot.airshipdemo.utilities.RezolvePreferences
import kotlin.text.ifEmpty

@Composable
fun PointSdkInitSection(
    isPointSdkInitialized: Boolean,
    onInitPointSdk: (String, String) -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as MainApplication }
    var projectId by remember {
        mutableStateOf(app.getSavedProjectId().ifEmpty { BuildConfig.BLUEDOT_PROJECT_ID })
    }
    var baseUrl by remember {
        mutableStateOf(app.getSavedBaseUrl().ifEmpty { RezolvePreferences.DEFAULT_BASE_URL })
    }
    var baseUrlDropdownExpanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = projectId,
        onValueChange = { projectId = it },
        label = { Text("Project ID") },
        placeholder = { Text("Enter Bluedot Project ID") },
        singleLine = true,
        enabled = !isPointSdkInitialized,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Base URL dropdown
    val baseUrlOptions = listOf(
        RezolvePreferences.DEFAULT_BASE_URL,
        RezolvePreferences.ALTERNATIVE_BASE_URL
    )

    fun urlLabel(url: String): String {
        // Show only the hostname (without https:// and trailing slash) to keep the label compact.
        // Also strip the common "globalconfig." prefix so the distinctive part is shown first.
        val host = url.removePrefix("https://").trimEnd('/')
        return host//.removePrefix("globalconfig.")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Base URL",
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.weight(3f)) {
            OutlinedTextField(
                value = urlLabel(baseUrl),
                onValueChange = {},
                singleLine = true,
                readOnly = true,
                enabled = !isPointSdkInitialized,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { if (!isPointSdkInitialized) baseUrlDropdownExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select base URL"
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = baseUrlDropdownExpanded,
                onDismissRequest = { baseUrlDropdownExpanded = false }
            ) {
                baseUrlOptions.forEach { url ->
                    DropdownMenuItem(
                        text = { Text(urlLabel(url)) },
                        onClick = {
                            baseUrl = url
                            baseUrlDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { onInitPointSdk(projectId, baseUrl) },
            enabled = !isPointSdkInitialized && projectId.isNotBlank(),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text(text = stringResource(id = R.string.start_point_sdk))
        }

        Button(
            onClick = { onReset() },
            enabled = isPointSdkInitialized,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text(text = stringResource(id = R.string.stop_point_sdk))
        }
    }
}

