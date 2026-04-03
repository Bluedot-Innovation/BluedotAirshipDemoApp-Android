package io.bluedot.airshipdemo.ui

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
import com.urbanairship.AirshipConfigOptions
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.MainApplication
import io.bluedot.airshipdemo.R
import kotlin.text.ifEmpty

@Composable
fun AirshipInitSection(
    isAirshipInitialized: Boolean,
    onInitAirship: (String, String, String) -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as MainApplication }
    var airshipAppKey by remember {
        mutableStateOf(app.getSavedAirshipAppKey().ifEmpty { BuildConfig.AIRSHIP_APP_KEY })
    }
    var airshipAppSecret by remember {
        mutableStateOf(app.getSavedAirshipAppSecret().ifEmpty { BuildConfig.AIRSHIP_APP_SECRET })
    }
    var airshipSite by remember { mutableStateOf(app.getSavedAirshipSite()) }
    var siteDropdownExpanded by remember { mutableStateOf(false) }

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

    // Site dropdown
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Airship Site",
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = airshipSite,
                onValueChange = {},
                singleLine = true,
                readOnly = true,
                enabled = !isAirshipInitialized,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { if (!isAirshipInitialized) siteDropdownExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select site"
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = siteDropdownExpanded,
                onDismissRequest = { siteDropdownExpanded = false }
            ) {
                listOf(
                    AirshipConfigOptions.SITE_EU,
                    AirshipConfigOptions.SITE_US
                ).forEach { site ->
                    DropdownMenuItem(
                        text = { Text(site) },
                        onClick = {
                            airshipSite = site
                            siteDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onInitAirship(airshipAppKey, airshipAppSecret, airshipSite) },
        enabled = !isAirshipInitialized && airshipAppKey.isNotBlank() && airshipAppSecret.isNotBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = stringResource(id = R.string.initialize_airship))
    }
}

