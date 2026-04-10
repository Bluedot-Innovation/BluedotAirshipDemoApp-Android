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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.utilities.localPermissions
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    channelId: String?,
    isAirshipInitialized: Boolean,
    isPointSdkInitialized: Boolean,
    onInitAirship: (String, String, String) -> Unit,
    onInitPointSdk: (String) -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(permissions = localPermissions)

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 120.dp),
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

        AirshipInitSection(
            isAirshipInitialized = isAirshipInitialized,
            onInitAirship = onInitAirship,
        )

        Spacer(modifier = Modifier.height(24.dp))

        PointSdkInitSection(
            isPointSdkInitialized = isPointSdkInitialized,
            onInitPointSdk = onInitPointSdk,
            onReset = onReset,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "PointSDK v${BuildConfig.POINT_SDK_VERSION}  •  Airship v${BuildConfig.AIRSHIP_SDK_VERSION}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}
