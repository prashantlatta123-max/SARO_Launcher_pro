package com.sarotech.launcher

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarotech.launcher.data.LauncherRepository
import com.sarotech.launcher.receiver.PackageChangeReceiver
import com.sarotech.launcher.ui.LauncherViewModel
import com.sarotech.launcher.ui.LauncherViewModelFactory
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var packageReceiver: PackageChangeReceiver? = null
    private var packageChangeVersion by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SarotechLauncher(packageChangeVersion = packageChangeVersion) }
    }

    override fun onStart() {
        super.onStart()
        val receiver = PackageChangeReceiver {
            packageChangeVersion++
        }
        packageReceiver = receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }
    }

    override fun onStop() {
        packageReceiver?.let { runCatching { unregisterReceiver(it) } }
        packageReceiver = null
        super.onStop()
    }
}

@Composable
private fun SarotechLauncher(packageChangeVersion: Int) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { LauncherRepository(context.applicationContext) }
    val factory = remember { LauncherViewModelFactory(repository) }
    val viewModel: LauncherViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    var query by remember { mutableStateOf("") }
    var drawerOpen by remember { mutableStateOf(false) }
    var darkOverride by remember { mutableStateOf<Boolean?>(null) }
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val darkTheme = darkOverride ?: systemDark

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(packageChangeVersion) {
        if (packageChangeVersion > 0) viewModel.refresh()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val background = if (darkTheme) Color(0xFF050505) else Color(0xFFF5F5F2)
    val foreground = if (darkTheme) Color(0xFFF3F0E8) else Color(0xFF171717)
    val secondary = if (darkTheme) Color(0xFF969696) else Color(0xFF777777)
    val focusManager = LocalFocusManager.current

    val filtered = remember(state.apps, query) {
        val normalized = NativeBridge.normalize(query)
        if (normalized.isBlank()) state.apps
        else state.apps.filter { NativeBridge.normalize(it.label).contains(normalized) }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme(
            background = background, surface = background, onBackground = foreground
        ) else lightColorScheme(
            background = background, surface = background, onBackground = foreground
        )
    ) {
        Box(
            Modifier.fillMaxSize()
                .background(background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp, vertical = 24.dp)
                .pointerInput(drawerOpen) {
                    if (!drawerOpen) detectVerticalDragGestures(
                        onVerticalDrag = { change, amount ->
                            if (amount < -8f) {
                                change.consume()
                                drawerOpen = true
                            }
                        }
                    )
                }
        ) {
            if (!drawerOpen) {
                HomeScreen(
                    apps = state.apps.take(5),
                    foreground = foreground,
                    secondary = secondary,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkOverride = !darkTheme },
                    onOpenDrawer = { drawerOpen = true },
                    onLaunch = repository::launch
                )
            } else {
                DrawerScreen(
                    apps = filtered,
                    query = query,
                    foreground = foreground,
                    secondary = secondary,
                    onQueryChange = { query = it },
                    onClose = {
                        focusManager.clearFocus()
                        query = ""
                        drawerOpen = false
                    },
                    onLaunch = repository::launch
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    apps: List<com.sarotech.launcher.data.AppEntry>,
    foreground: Color,
    secondary: Color,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenDrawer: () -> Unit,
    onLaunch: (com.sarotech.launcher.data.AppEntry) -> Unit
) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(1_000)
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("SAROTECH", color = secondary, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp)
            Text(if (darkTheme) "●" else "○", color = foreground,
                modifier = Modifier.clickable(onClick = onToggleTheme))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
            ), color = secondary, fontSize = 14.sp)
            Text(now.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = foreground, fontSize = 68.sp,
                fontWeight = FontWeight.Light, letterSpacing = (-3).sp)
            Text("A little less. A lot more.", color = secondary, fontSize = 13.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            apps.forEach { app ->
                Text(app.label, color = foreground, fontSize = 19.sp,
                    modifier = Modifier.fillMaxWidth().clickable { onLaunch(app) })
            }
            Text("SWIPE UP  ↑  ·  ALL APPS", color = secondary,
                fontSize = 11.sp, letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .clickable(onClick = onOpenDrawer).padding(12.dp))
        }
    }
}

@Composable
private fun DrawerScreen(
    apps: List<com.sarotech.launcher.data.AppEntry>,
    query: String,
    foreground: Color,
    secondary: Color,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onLaunch: (com.sarotech.launcher.data.AppEntry) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("APPLICATIONS", color = foreground, fontSize = 12.sp,
                letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Text("DONE", color = secondary, fontSize = 11.sp,
                modifier = Modifier.clickable(onClick = onClose).padding(8.dp))
        }
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search apps", color = secondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = foreground,
                unfocusedTextColor = foreground,
                focusedBorderColor = secondary,
                unfocusedBorderColor = secondary.copy(alpha = .35f),
                cursorColor = foreground
            )
        )
        Spacer(Modifier.height(12.dp))
        if (apps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (query.isBlank()) "No launchable apps found" else "No matching apps",
                    color = secondary)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(apps, key = { it.stableId }) { app ->
                    Text(app.label, color = foreground, fontSize = 17.sp,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onLaunch(app) }
                            .padding(vertical = 14.dp))
                    HorizontalDivider(color = secondary.copy(alpha = .18f))
                }
            }
        }
    }
}
