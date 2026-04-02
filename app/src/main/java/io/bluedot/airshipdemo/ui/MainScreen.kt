package io.bluedot.airshipdemo.ui

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.MainApplication
import io.bluedot.airshipdemo.R
import io.bluedot.airshipdemo.utilities.localPermissions
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    channelId: String?,
    isAirshipInitialized: Boolean,
    isPointSdkInitialized: Boolean,
    onInitAirship: (String, String) -> Unit,
    onInitPointSdk: (String) -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as MainApplication }
    val savedProjectId = remember { app.getSavedProjectId() }
    val savedAirshipAppKey = remember { app.getSavedAirshipAppKey() }
    val savedAirshipAppSecret = remember { app.getSavedAirshipAppSecret() }
    var projectId by remember {
        mutableStateOf(savedProjectId.ifEmpty { BuildConfig.BLUEDOT_PROJECT_ID })
    }
    var airshipAppKey by remember {
        mutableStateOf(savedAirshipAppKey.ifEmpty { BuildConfig.AIRSHIP_APP_KEY })
    }
    var airshipAppSecret by remember {
        mutableStateOf(savedAirshipAppSecret.ifEmpty { BuildConfig.AIRSHIP_APP_SECRET })
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = localPermissions)

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 160.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Airship Channel ID",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val clipboard = LocalClipboard.current
        val coroutineScope = rememberCoroutineScope()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = channelId ?: "Not available",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            if (channelId != null) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        clipboard.setClipEntry(
                            ClipData.newPlainText("Channel ID", channelId).toClipEntry()
                        )
                    }
                    Toast.makeText(context, "Channel ID copied", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy channel ID",
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = airshipAppKey,
            onValueChange = { airshipAppKey = it },
            label = { Text("Airship App Key") },
            placeholder = { Text("Enter Airship App Key") },
            singleLine = true,
            enabled = !isAirshipInitialized,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = airshipAppSecret,
            onValueChange = { airshipAppSecret = it },
            label = { Text("Airship App Secret") },
            placeholder = { Text("Enter Airship App Secret") },
            singleLine = true,
            enabled = !isAirshipInitialized,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onInitAirship(airshipAppKey, airshipAppSecret) },
            enabled = !isAirshipInitialized && airshipAppKey.isNotBlank() && airshipAppSecret.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = stringResource(id = R.string.initialize_airship))
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onInitPointSdk(projectId) },
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
}
