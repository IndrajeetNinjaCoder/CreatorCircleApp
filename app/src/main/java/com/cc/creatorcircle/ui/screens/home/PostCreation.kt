package com.cc.creatorcircle.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.PostCreationState
import com.cc.creatorcircle.data.models.PostUpdateState
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.utils.createFileFromUri
import com.cc.creatorcircle.viewModel.PostsViewModel
import kotlin.io.extension






@Composable
fun GradientCreatePostButton(
    modifier: Modifier = Modifier,
    viewModel: PostsViewModel,
    username: String,
    profilePic: String?
) {
    var showPopup by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63)),
                    startX = 0f,
                    endX = 1000f
                ),
                shape = RoundedCornerShape(50)
            )
            .clickable {
                FirebaseAnalyticsHelper.logFeatureUsed("create_post_button_clicked", "floating_button")
                showPopup = true
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showPopup) {
        LaunchedEffect(Unit) {
            FirebaseAnalyticsHelper.logDialogOpened("create_post_dialog", null)
        }

        PostInputPopupDialog(
            viewModel = viewModel,
            username = username,
            profilePic = profilePic,
            onDismiss = {
                FirebaseAnalyticsHelper.logDialogClosed("create_post_dialog", "dismissed")
                showPopup = false
            }
        )
    }
}

@Composable
fun PostInputSection(
    viewModel: PostsViewModel,
    focusRequester: FocusRequester = remember { FocusRequester() },
    username: String,
    profilePic: String?
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    val postCreationState by viewModel.postCreationState.collectAsState()
    val selectedMediaFiles by viewModel.selectedMediaFiles.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let {
                FirebaseAnalyticsHelper.logFeatureUsed("media_added", "image_post_input")
                viewModel.addMediaFile(it)
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let {
                FirebaseAnalyticsHelper.logFeatureUsed("media_added", "video_post_input")
                viewModel.addMediaFile(it)
            }
        }
    }

//    LaunchedEffect(postCreationState) {
//        when (postCreationState) {
//            is PostCreationState.Success -> {
//                FirebaseAnalyticsHelper.logFeatureUsed("post_created_success", "post_input_section")
//                text = ""
//                showPopup = false
//            }
//            is PostCreationState.Error -> {
//                (postCreationState as? PostCreationState.Error)?.let { errorState ->
//                    FirebaseAnalyticsHelper.logError(
//                        errorType = "post_creation_error",
//                        errorMessage = errorState.message,
//                        context = "PostInputSection"
//                    )
//                }
//            }
//            else -> {}
//        }
//    }
//
//

    LaunchedEffect(postCreationState) {
        when (postCreationState) {
            is PostCreationState.Success -> {
                FirebaseAnalyticsHelper.logFeatureUsed("post_created_success", "post_input_section")
                text = ""
                showPopup = false
                viewModel.fetchPosts(postType = "feed") // Add this line
            }
            is PostCreationState.Error -> {
                (postCreationState as? PostCreationState.Error)?.let { errorState ->
                    FirebaseAnalyticsHelper.logError(
                        errorType = "post_creation_error",
                        errorMessage = errorState.message,
                        context = "PostInputSection"
                    )
                }
            }
            else -> {}
        }
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row {
            if (profilePic?.isNotEmpty() == true) {
                AsyncImage(
                    model = profilePic,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.ic_profile1),
                    error = painterResource(id = R.drawable.ic_profile1),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile1),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            }

            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFB726FF).copy(alpha = 0.5f),
                            Color(0xFFFB3D91).copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
                .clickable {
                    FirebaseAnalyticsHelper.logFeatureUsed("post_input_clicked", "inline_section")
                    showPopup = true
                }
        ) {
            Text(
                text = "Share your thoughts",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        if (selectedMediaFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(selectedMediaFiles.size) { index ->
                    val file = selectedMediaFiles[index]
                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(80.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when {
                                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif") -> {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                file.extension.lowercase() in listOf("mp4", "avi", "mov", "mkv", "3gp", "webm") -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play),
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = file.extension.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                                    .clickable {
                                        FirebaseAnalyticsHelper.logFeatureUsed("media_removed", "post_input")
                                        viewModel.removeMediaFile(file)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AttachmentButton(
                icon = R.drawable.ic_image,
                text = "Image",
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("image_picker_opened", "post_input")
                    imagePickerLauncher.launch("image/*")
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            AttachmentButton(
                icon = R.drawable.ic_video,
                text = "Video",
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("video_picker_opened", "post_input")
                    videoPickerLauncher.launch("video/*")
                }
            )
        }

        val currentState = postCreationState
        if (currentState is PostCreationState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentState.message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.clickable { viewModel.clearError() }
            )
        }
    }

    if (showPopup) {
        LaunchedEffect(Unit) {
            FirebaseAnalyticsHelper.logDialogOpened("create_post_dialog", null)
        }

        PostInputPopupDialog(
            viewModel = viewModel,
            username = username,
            profilePic = profilePic,
            onDismiss = {
                FirebaseAnalyticsHelper.logDialogClosed("create_post_dialog", "dismissed")
                showPopup = false
            }
        )
    }
}

@Composable
fun PostInputPopup(
    viewModel: PostsViewModel,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val postCreationState by viewModel.postCreationState.collectAsState()
    val selectedMediaFiles by viewModel.selectedMediaFiles.collectAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let {
                FirebaseAnalyticsHelper.logFeatureUsed("media_added", "image_popup")
                viewModel.addMediaFile(it)
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let {
                FirebaseAnalyticsHelper.logFeatureUsed("media_added", "video_popup")
                viewModel.addMediaFile(it)
            }
        }
    }

//    LaunchedEffect(postCreationState) {
//        when (postCreationState) {
//            is PostCreationState.Success -> {
//                FirebaseAnalyticsHelper.logFeatureUsed("post_created_success", "popup")
//                text = ""
//            }
//            is PostCreationState.Error -> {
//                (postCreationState as? PostCreationState.Error)?.let { errorState ->
//                    FirebaseAnalyticsHelper.logError(
//                        errorType = "post_creation_error",
//                        errorMessage = errorState.message,
//                        context = "PostInputPopup"
//                    )
//                }
//            }
//            else -> {}
//        }
//    }



    LaunchedEffect(postCreationState) {
        when (postCreationState) {
            is PostCreationState.Success -> {
                FirebaseAnalyticsHelper.logFeatureUsed("post_created_success", "popup")
                text = ""
                viewModel.fetchPosts(postType = "feed") // Add this line
            }
            is PostCreationState.Error -> {
                (postCreationState as? PostCreationState.Error)?.let { errorState ->
                    FirebaseAnalyticsHelper.logError(
                        errorType = "post_creation_error",
                        errorMessage = errorState.message,
                        context = "PostInputPopup"
                    )
                }
            }
            else -> {}
        }
    }




    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "What do you want to talk about?",
                    color = Color(0xFFAAAAAA),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            BasicTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    if (newText.length == 1) {
                        FirebaseAnalyticsHelper.logFeatureUsed("post_text_started", "popup")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )
        }

        if (selectedMediaFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedMediaFiles.size) { index ->
                    val file = selectedMediaFiles[index]
                    Card(
                        modifier = Modifier.size(80.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when {
                                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif") -> {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                file.extension.lowercase() in listOf("mp4", "avi", "mov", "mkv", "3gp", "webm") -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play),
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = file.extension.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                                    .clickable {
                                        FirebaseAnalyticsHelper.logFeatureUsed("media_removed", "popup")
                                        viewModel.removeMediaFile(file)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    AttachmentButton(
                        icon = R.drawable.ic_image,
                        text = "Photo",
                        iconTint = Color(0xFF1DA1F2),
                        hasTextInput = text.isNotBlank(),
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("image_picker_opened", "popup")
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AttachmentButton(
                        icon = R.drawable.ic_video,
                        text = "Video",
                        iconTint = Color(0xFF00C851),
                        hasTextInput = text.isNotBlank(),
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("video_picker_opened", "popup")
                            videoPickerLauncher.launch("video/*")
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            enabled = postCreationState !is PostCreationState.Loading &&
                                    (text.isNotBlank() || selectedMediaFiles.isNotEmpty())
                        ) {
                            if (text.isNotBlank() || selectedMediaFiles.isNotEmpty()) {
                                FirebaseAnalyticsHelper.logFeatureUsed("post_submitted", "popup")
                                viewModel.createPost(text, postType = "feed")
                            }
                        }
                ) {
                    if (postCreationState is PostCreationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Post",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        val currentState = postCreationState
        if (currentState is PostCreationState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentState.message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.clickable { viewModel.clearError() }
            )
        }
    }
}



@Composable
fun EditPostDialog(
    post: Post,
    viewModel: PostsViewModel,
    username: String,
    profilePic: String?,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val postUpdateState by viewModel.postUpdateState.collectAsState()

    // Initialize media for editing when dialog opens
    LaunchedEffect(post.id) {
        viewModel.initializePostForEdit(post.media)
    }

    // Close dialog on success
    LaunchedEffect(postUpdateState) {
        if (postUpdateState is PostUpdateState.Success) {
            FirebaseAnalyticsHelper.logFeatureUsed("post_update_success", "edit_dialog")
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logDialogOpened("edit_post_dialog", post.id)
    }

    Dialog(
        onDismissRequest = {
            FirebaseAnalyticsHelper.logDialogClosed("edit_post_dialog", "dismissed")
            viewModel.resetUpdateState()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 4f / 5f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE0E0E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = profilePic,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = username,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Edit Post",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                            .clickable {
                                FirebaseAnalyticsHelper.logDialogClosed("edit_post_dialog", "close_button")
                                viewModel.resetUpdateState()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                EditPostContent(
                    post = post,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



@Composable
fun PostInputPopupDialog(
    viewModel: PostsViewModel,
    username: String,
    profilePic: String?,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 4f / 5f) // 2/3 of screen height
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with user info and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFFE0E0E0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // You can replace this with actual user image
                            AsyncImage(
                                model = profilePic,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = username, // Replace with actual user name
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Member",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFFF5F5F5),
                                CircleShape
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Use the updated PostInputPopup composable

//                PostInputPopup(
//                    viewModel = viewModel,
//                    modifier = Modifier.weight(1f)
//                )

                PostInputPopup(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )


            }
        }
    }
}


@Composable
fun EditPostContent(
    post: Post,
    viewModel: PostsViewModel,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf(post.content) }
    val postUpdateState by viewModel.postUpdateState.collectAsState()
    val selectedUpdateMediaFiles by viewModel.selectedUpdateMediaFiles.collectAsState()
    val existingMediaUrls by viewModel.existingMediaUrls.collectAsState()

    // Auto-focus when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Add this at the top of EditPostContent, after the variable declarations
    LaunchedEffect(postUpdateState) {
        if (postUpdateState is PostUpdateState.Success) {
            FirebaseAnalyticsHelper.logFeatureUsed("post_update_success", "edit_dialog")
            // Close dialog or show success message
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addUpdateMediaFile(it) }
        }
    }

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addUpdateMediaFile(it) }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Text input area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "What do you want to talk about?",
                    color = Color(0xFFAAAAAA),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )
        }

        // Show existing and new media files
        if (existingMediaUrls.isNotEmpty() || selectedUpdateMediaFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Existing media
                items(existingMediaUrls.size) { index ->
                    val url = existingMediaUrls[index]
                    Card(
                        modifier = Modifier.size(80.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isVideoUrl(url)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_play),
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Existing media",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Remove button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .clickable { viewModel.removeExistingMediaUrl(url) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // New media files
                items(selectedUpdateMediaFiles.size) { index ->
                    val file = selectedUpdateMediaFiles[index]
                    Card(
                        modifier = Modifier.size(80.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                file.extension.lowercase() in listOf(
                                    "jpg",
                                    "jpeg",
                                    "png",
                                    "gif"
                                ) -> {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "New image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                file.extension.lowercase() in listOf(
                                    "mp4",
                                    "avi",
                                    "mov",
                                    "mkv",
                                    "3gp",
                                    "webm"
                                ) -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play),
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = file.extension.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Remove button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .clickable { viewModel.removeUpdateMediaFile(file) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachment buttons
                Row(horizontalArrangement = Arrangement.Start) {
                    AttachmentButton(
                        icon = R.drawable.ic_image,
                        text = "Photo",
                        iconTint = Color(0xFF1DA1F2),
                        hasTextInput = text.isNotBlank(),
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AttachmentButton(
                        icon = R.drawable.ic_video,
                        text = "Video",
                        iconTint = Color(0xFF00C851),
                        hasTextInput = text.isNotBlank(),
                        onClick = { videoPickerLauncher.launch("video/*") }
                    )
                }



                // Update button
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            enabled = postUpdateState !is PostUpdateState.Loading &&
                                    text.isNotBlank()
                        ) {
                            if (text.isNotBlank()) {
                                FirebaseAnalyticsHelper.logFeatureUsed("post_updated", "edit_dialog")
                                viewModel.updatePost(post.id, text)
                            }
                        }
                ){
                    if (postUpdateState is PostUpdateState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Update",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Show error message
        if (postUpdateState is PostUpdateState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (postUpdateState as PostUpdateState.Error).message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.clickable { viewModel.clearUpdateError() }
            )
        }
    }
}

@Composable
fun AttachmentButton(
    icon: Int,
    text: String,
    iconTint: Color = Color.Gray,
    hasTextInput: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .background(
                color = if (hasTextInput) Color(0xFFF0F8FF) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
//            .padding(horizontal = if (hasTextInput) 12.dp else 0.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (hasTextInput) Color(0xFF333333) else Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}

