package com.cc.creatorcircle.ui.screens.profile


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cc.creatorcircle.R
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.GradientIconButton


@Composable
fun YourProfileScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
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
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change Photo Button
                GradientIconButton("Change Photo", R.drawable.ic_edit) { }
//                Button(
//                    onClick = { /* Handle photo change */ },
//                    modifier = Modifier
//                        .width(280.dp)
//                        .height(56.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF8B5CF6)
//                    ),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Text(
//                        text = "Change photo",
//                        fontSize = 16.sp,
//                        color = Color.White,
//                        fontWeight = FontWeight.Medium
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_edit),
//                        contentDescription = "Edit",
//                        modifier = Modifier.size(20.dp),
//                        tint = Color.White
//                    )
//                }

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

            GradientButton(
                text = "Save",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) { }
//            Button(
//                onClick = { /* Handle save */ },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(64.dp)
//                    .padding(horizontal = 0.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF8B5CF6)
//                ),
//                shape = RoundedCornerShape(0.dp)
//            ) {
//                Text(
//                    text = "Save",
//                    fontSize = 18.sp,
//                    color = Color.White,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
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
            contentDescription = "Instagram Icon",
            modifier = Modifier.size(40.dp)
        )

        // Name Fieldd
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = name,
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