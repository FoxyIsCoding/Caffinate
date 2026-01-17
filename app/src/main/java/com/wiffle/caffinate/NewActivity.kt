package com.wiffle.caffinate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.wiffle.caffinate.data.Drink
import com.wiffle.caffinate.data.DrinkViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NewCanScreen(onClose: () -> Unit, viewModel: DrinkViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var drinkName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Monster Energy") }
    var notes by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(4.5f) }
    var caffeineContent by remember { mutableStateOf("160") }
    var sugarContent by remember { mutableStateOf("54") }
    var calories by remember { mutableStateOf("210") }
    var size by remember { mutableStateOf("16 fl oz (473ml)") }
    var location by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var capturedImageFile by remember { mutableStateOf<File?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var shouldLaunchCamera by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val copiedFile = CameraUtils.copyImageFromUri(context, it)
            if (copiedFile != null) {
                imageUrl = copiedFile.absolutePath
                capturedImageFile = copiedFile
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageFile != null) {
            val compressedFile = CameraUtils.compressAndSaveImage(context, capturedImageFile!!)
            if (compressedFile != null) {
                imageUrl = compressedFile.absolutePath
                capturedImageFile = compressedFile
            }
        }
    }

    val launchCamera: () -> Unit = {
        val file = CameraUtils.createImageFile(context)
        capturedImageFile = file
        val uri = CameraUtils.getImageUri(context, file)
        cameraLauncher.launch(uri)
    }

    val launchGalleryPicker: () -> Unit = {
        photoPickerLauncher.launch(
            androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    LaunchedEffect(cameraPermissionState.status.isGranted, shouldLaunchCamera) {
        if (cameraPermissionState.status.isGranted && shouldLaunchCamera) {
            shouldLaunchCamera = false
            launchCamera()
        }
    }

    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val categories = listOf("Energy Drink", "Coffee Energy Drink", "Performance Energy Drink", "Zero Sugar")
    var selectedCategory by remember { mutableStateOf("Energy Drink") }

    val availableTags =
        listOf("Sweet", "Citrus", "Tropical", "Zero Sugar", "Carbonated", "Creamy", "High Caffeine", "Light")

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Add New Can",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (drinkName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a drink name")
                        }
                        return@ExtendedFloatingActionButton
                    }

                    val newDrink = Drink(
                        name = drinkName,
                        brand = brand,
                        category = selectedCategory,
                        caffeineContent = caffeineContent.toIntOrNull() ?: 0,
                        sugarContent = sugarContent.toIntOrNull() ?: 0,
                        size = size,
                        sizeInMl = 473,
                        location = location,
                        rating = rating,
                        imageUrl = imageUrl,
                        consumedDate = System.currentTimeMillis(),
                        notes = notes,
                        tags = selectedTags.toList(),
                        isFavorite = false,
                        calories = calories.toIntOrNull() ?: 0,
                        timesConsumed = 1
                    )

                    viewModel.insertDrink(newDrink)
                    onClose()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                icon = { Icon(Icons.Default.Save, null) },
                text = { Text("Save Can") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        showImageOptions = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Drink photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = {
                            if (capturedImageFile != null) {
                                CameraUtils.deleteImageFile(imageUrl)
                            }
                            imageUrl = ""
                            capturedImageFile = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete photo",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add a photo",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showImageOptions) {
                AlertDialog(
                    onDismissRequest = { showImageOptions = false },
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Add Photo",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Choose how you'd like to add a photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                onClick = {
                                    showImageOptions = false
                                    if (cameraPermissionState.status.isGranted) {
                                        launchCamera()
                                    } else {
                                        shouldLaunchCamera = true
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Take Photo",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "Capture with camera",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = {
                                    showImageOptions = false
                                    launchGalleryPicker()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Choose from Gallery",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            "Select existing photo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showImageOptions = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                "Cancel",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                )
            }

            if (cameraPermissionState.status.shouldShowRationale) {
                AlertDialog(
                    onDismissRequest = { },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    title = {
                        Text(
                            "Camera Permission Required",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            "This app needs camera access to take photos of your drinks. Please grant the permission to continue.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "Grant Permission",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "Not Now",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                )
            }

            TextField(
                value = drinkName,
                onValueChange = { drinkName = it },
                label = { Text("Drink Name *") },
                placeholder = { Text("e.g. Ultra Paradise") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            TextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                placeholder = { Text("e.g. Monster Energy") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Column {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = caffeineContent,
                    onValueChange = { caffeineContent = it },
                    label = { Text("Caffeine (mg)") },
                    placeholder = { Text("160") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                TextField(
                    value = sugarContent,
                    onValueChange = { sugarContent = it },
                    label = { Text("Sugar (g)") },
                    placeholder = { Text("54") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    placeholder = { Text("16 fl oz (473ml)") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                TextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    placeholder = { Text("210") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            TextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("e.g. 7-Eleven, Austin TX") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Column {
                Text(
                    "Tags",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableTags) { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            },
                            label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Rating",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            rating.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " stars",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 0f..5f,
                    steps = 9
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Bad",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        "God Tier",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            TextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Tasting Notes") },
                placeholder = { Text("Flavor profile, carbonation level...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}
