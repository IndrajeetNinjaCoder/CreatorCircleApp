package com.cc.creatorcircle.ui.screens.profile

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.GradientIconButton
import com.cc.creatorcircle.utils.createFileFromUri
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserUpdateState
import java.io.File


@Composable
fun YourProfileScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Initialize UserViewModel for updates
    val userViewModel = remember { UserViewModel(context) }

    // Collect user profile from PostsViewModel
    val userProfile by postsViewModel.userProfile.collectAsState()
    val isLoading by postsViewModel.profileLoading.collectAsState()
    val error by postsViewModel.profileError.collectAsState()

    // Collect update state from UserViewModel
    val updateState by userViewModel.updateState.collectAsState()

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        postsViewModel.fetchUserProfile()
    }

    // Initialize state with profile data when available
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var instagramName by remember { mutableStateOf("") }
    var instagramLink by remember { mutableStateOf("") }
    var instagramFollowers by remember { mutableStateOf("") }

    var facebookName by remember { mutableStateOf("") }
    var facebookLink by remember { mutableStateOf("") }
    var facebookFollowers by remember { mutableStateOf("") }

    var youtubeName by remember { mutableStateOf("") }
    var youtubeLink by remember { mutableStateOf("") }
    var youtubeFollowers by remember { mutableStateOf("") }

    // Profile image states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedFile by remember { mutableStateOf<File?>(null) }
    var currentProfilePic by remember { mutableStateOf<String?>(null) }

    // Update fields when profile is loaded
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            // Parse full name into first and last name
            val nameParts = profile.full_name?.split(" ", limit = 2) ?: listOf()
            firstName = nameParts.getOrNull(0) ?: ""
            lastName = nameParts.getOrNull(1) ?: ""

            category = profile.categories?.firstOrNull() ?: ""
            bio = profile.bio ?: ""
            currentProfilePic = profile.profile_pic

            // Extract Instagram data
            try {
                val instagramData = profile.social_media_links?.get("instagram") as? List<*>
                val firstInstagram = instagramData?.firstOrNull() as? Map<*, *>
                instagramName = (firstInstagram?.get("username") as? String) ?: ""
                instagramLink = (firstInstagram?.get("link") as? String) ?: ""

                val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
                val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
                val followers = (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
                instagramFollowers = if (followers > 0) followers.toString() else ""
            } catch (e: Exception) {
                Log.e("YourProfileScreen", "Error parsing Instagram data", e)
            }

            // Extract Facebook data
            try {
                val facebookData = profile.social_media_links?.get("facebook") as? List<*>
                val firstFacebook = facebookData?.firstOrNull() as? Map<*, *>
                facebookName = (firstFacebook?.get("name") as? String) ?: ""
                facebookLink = (firstFacebook?.get("url") as? String) ?: ""

                val platformFollowers = profile.platform_followers?.get("facebook") as? List<*>
                val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
                val followers = (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
                facebookFollowers = if (followers > 0) followers.toString() else ""
            } catch (e: Exception) {
                Log.e("YourProfileScreen", "Error parsing Facebook data", e)
            }

            // Extract YouTube data
            try {
                val youtubeData = profile.social_media_links?.get("youtube") as? List<*>
                val firstYoutube = youtubeData?.firstOrNull() as? Map<*, *>
                youtubeName = (firstYoutube?.get("name") as? String) ?: ""
                youtubeLink = (firstYoutube?.get("url") as? String) ?: ""

                val platformFollowers = profile.platform_followers?.get("youtube") as? List<*>
                val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
                val followers = (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
                youtubeFollowers = if (followers > 0) followers.toString() else ""
            } catch (e: Exception) {
                Log.e("YourProfileScreen", "Error parsing YouTube data", e)
            }
        }
    }

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                userViewModel.resetUpdateState()
                // Refresh the profile data
                postsViewModel.fetchUserProfile()
                // Navigate back or dismiss
                onDismiss()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                userViewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val file = createFileFromUri(context, it)
            uploadedFile = file
            file?.let { f ->
                // Set profile pic in UserViewModel
                userViewModel.setProfilePic(f)
                Log.d("YourProfileScreen", "File created: ${f.absolutePath}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFB388FF)
                )
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error ?: "An error occurred",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Button(
                        onClick = { postsViewModel.fetchUserProfile() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB388FF)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Top Bar with Back Arrow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_left_arrow),
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onDismiss() },
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Your profile",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Profile Image
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.LightGray, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (currentProfilePic != null) {
                                AsyncImage(
                                    model = currentProfilePic,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.LightGray, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E0E0))
                                        .border(1.dp, Color.LightGray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_profile),
                                        contentDescription = "Default profile",
                                        modifier = Modifier.size(60.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Change Photo Button
                        GradientIconButton("Change Photo", R.drawable.ic_edit) {
                            imagePickerLauncher.launch("image/*")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Name Section
                        Text(
                            text = "Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "First name",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    placeholder = {
                                        Text(
                                            "Enter Your first name",
                                            color = Color(0xFFBDBDBD),
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Last name",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = { lastName = it },
                                    placeholder = {
                                        Text(
                                            "Enter Your last name",
                                            color = Color(0xFFBDBDBD),
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Category Section
                        Text(
                            text = "Category of Influencer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            placeholder = {
                                Text(
                                    "Add your category",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bio Section
                        Text(
                            text = "Bio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = {
                                if (it.length <= 50) bio = it
                            },
                            placeholder = {
                                Text(
                                    "Add your Bio",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 4
                        )

                        // Character Counter
                        Text(
                            text = "${bio.length}/50",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Social Links Section
                        Text(
                            text = "Social Links",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Instagram
                        SocialLinkRow(
                            icon = R.drawable.ic_instagram,
                            iconTint = Color(0xFFE4405F),
                            name = instagramName,
                            onNameChange = { instagramName = it },
                            link = instagramLink,
                            onLinkChange = { instagramLink = it },
                            followers = instagramFollowers,
                            onFollowersChange = { instagramFollowers = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Facebook
                        SocialLinkRow(
                            icon = R.drawable.ic_facebook,
                            iconTint = Color(0xFF1877F2),
                            name = facebookName,
                            onNameChange = { facebookName = it },
                            link = facebookLink,
                            onLinkChange = { facebookLink = it },
                            followers = facebookFollowers,
                            onFollowersChange = { facebookFollowers = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // YouTube
                        SocialLinkRow(
                            icon = R.drawable.ic_youtube,
                            iconTint = Color(0xFFFF0000),
                            name = youtubeName,
                            onNameChange = { youtubeName = it },
                            link = youtubeLink,
                            onLinkChange = { youtubeLink = it },
                            followers = youtubeFollowers,
                            onFollowersChange = { youtubeFollowers = it }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Save Button (Fixed at bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (updateState is UserUpdateState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp),
                                color = Color(0xFFB388FF)
                            )
                        } else {
                            GradientButton(
                                text = "Save",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Construct full name
                                val fullName = "$firstName $lastName".trim()

                                // Prepare categories list
                                val categories = if (category.isNotBlank()) {
                                    listOf(category)
                                } else null

                                // Call updateSocialMediaData with all social media info
                                userViewModel.updateSocialMediaData(
                                    instagramFollowers = instagramFollowers.takeIf { it.isNotBlank() },
                                    instagramLink = instagramLink.takeIf { it.isNotBlank() },
                                    youtubeFollowers = youtubeFollowers.takeIf { it.isNotBlank() },
                                    youtubeLink = youtubeLink.takeIf { it.isNotBlank() },
                                    facebookFollowers = facebookFollowers.takeIf { it.isNotBlank() },
                                    facebookLink = facebookLink.takeIf { it.isNotBlank() }
                                )

                                // Update basic user info
                                userViewModel.updateUser(
                                    fullName = fullName.takeIf { it.isNotBlank() },
                                    categories = categories,
                                    bio = bio.takeIf { it.isNotBlank() }
                                )

                                Log.d("YourProfileScreen", "Saving profile data:")
                                Log.d("YourProfileScreen", "Full Name: $fullName")
                                Log.d("YourProfileScreen", "Category: $category")
                                Log.d("YourProfileScreen", "Bio: $bio")
                                Log.d("YourProfileScreen", "Instagram: $instagramLink, Followers: $instagramFollowers")
                                Log.d("YourProfileScreen", "Facebook: $facebookLink, Followers: $facebookFollowers")
                                Log.d("YourProfileScreen", "YouTube: $youtubeLink, Followers: $youtubeFollowers")
                                uploadedFile?.let { file ->
                                    Log.d("YourProfileScreen", "Profile pic file: ${file.absolutePath}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SocialLinkRow(
    icon: Int,
    iconTint: Color,
    name: String,
    onNameChange: (String) -> Unit,
    link: String,
    onLinkChange: (String) -> Unit,
    followers: String,
    onFollowersChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Social Icon
        Image(
            painter = painterResource(id = icon),
            contentDescription = "Social Icon",
            modifier = Modifier.size(40.dp)
        )

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = "Enter name",
                    color = Color(0xFFBDBDBD),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.weight(0.28f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        // Link Field
        OutlinedTextField(
            value = link,
            onValueChange = onLinkChange,
            placeholder = {
                Text(
                    "Paste your profile link",
                    color = Color(0xFFBDBDBD),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.weight(0.42f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        // Followers Field
        OutlinedTextField(
            value = followers,
            onValueChange = onFollowersChange,
            placeholder = {
                Text(
                    "Followers count",
                    color = Color(0xFFBDBDBD),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.weight(0.30f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF8B5CF6),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
    }
}


