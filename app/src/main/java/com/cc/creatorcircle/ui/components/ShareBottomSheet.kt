
package com.cc.creatorcircle.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.viewModel.PostsViewModel

// Updated Contact data class to match UserInfo structure
data class Contact(
    val userId: Int,
    val username: String?,
    val fullName: String?,
    val profilePic: String?,
    val bio: String?
)

data class SharePlatform(
    val name: String,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val color: Color,
    val action: () -> Unit
)

@Composable
fun ShareBottomSheetContent(
    post: Post,
    context: Context,
    onDismiss: () -> Unit,
    viewModel: PostsViewModel? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var isLoadingContacts by remember { mutableStateOf(true) }

    // Observe the accepted connections from the ViewModel
    val acceptedConnections by viewModel?.acceptedConnections?.collectAsState() ?: remember { mutableStateOf(null) }
    val profileLoading by viewModel?.profileLoading?.collectAsState() ?: remember { mutableStateOf(false) }
    val profileError by viewModel?.profileError?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    // Load user connections when the bottom sheet opens
    LaunchedEffect(viewModel) {
        if (viewModel != null) {
            Log.d("ShareBottomSheet", "Fetching user profile...")
            viewModel.fetchUserProfile()
        } else {
            // Fallback to the old method if no viewModel is provided
            try {
                val userContacts = loadUserContacts(context)
                contacts = userContacts
                isLoadingContacts = false
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load contacts", Toast.LENGTH_SHORT).show()
                isLoadingContacts = false
            }
        }
    }

    // Update contacts when acceptedConnections changes
    LaunchedEffect(acceptedConnections) {
        Log.d("ShareBottomSheet", "acceptedConnections LaunchedEffect triggered")
        Log.d("ShareBottomSheet", "acceptedConnections is null: ${acceptedConnections == null}")
        Log.d("ShareBottomSheet", "acceptedConnections value: $acceptedConnections")

        if (acceptedConnections != null) {
            Log.d("ShareBottomSheet", "Processing ${acceptedConnections!!.users.size} connections")

            val contactsList = mutableListOf<Contact>()

            acceptedConnections!!.users.forEach { user ->
                Log.d("ShareBottomSheet", "Processing user: id=${user.user_id}, username='${user.username}', fullName='${user.full_name}'")

                // Create a display name - prioritize full_name over username
                val displayName = when {
                    !user.full_name.isNullOrBlank() -> user.full_name
                    !user.username.isNullOrBlank() -> user.username
                    else -> "User ${user.user_id}"
                }

                // Create username for sharing - use username if available, otherwise full_name
                val shareUsername = when {
                    !user.username.isNullOrBlank() -> user.username
                    !user.full_name.isNullOrBlank() -> user.full_name
                    else -> "user_${user.user_id}"
                }

                val contact = Contact(
                    userId = user.user_id,
                    username = shareUsername,
                    fullName = user.full_name,
                    profilePic = user.profile_pic,
                    bio = user.bio
                )

                contactsList.add(contact)
                Log.d("ShareBottomSheet", "Added contact: ${contact.username} (${contact.fullName})")
            }

            Log.d("ShareBottomSheet", "Created ${contactsList.size} contacts total")
            contacts = contactsList
            isLoadingContacts = false

            // Log each contact for verification
            contactsList.forEachIndexed { index, contact ->
                Log.d("ShareBottomSheet", "Contact $index: ${contact.username} - ${contact.fullName}")
            }
        } else {
            Log.d("ShareBottomSheet", "acceptedConnections is null, keeping loading state")
        }
    }

    // Handle profile loading state
    LaunchedEffect(profileLoading) {
        Log.d("ShareBottomSheet", "Profile loading state changed: $profileLoading")
        if (!profileLoading && acceptedConnections == null && profileError != null) {
            Log.d("ShareBottomSheet", "Setting isLoadingContacts to false due to error")
            isLoadingContacts = false
        }
    }

    // Handle profile error
    LaunchedEffect(profileError) {
        profileError?.let { error ->
            Log.e("ShareBottomSheet", "Profile error: $error")
            Toast.makeText(context, "Failed to load contacts: $error", Toast.LENGTH_SHORT).show()
            isLoadingContacts = false
        }
    }

    val filteredContacts = remember(searchQuery, contacts) {
        Log.d("ShareBottomSheet", "Filtering contacts. Total: ${contacts.size}, Search: '$searchQuery'")

        val result = if (searchQuery.isEmpty()) {
            contacts.take(9) // Show max 9 contacts in grid
        } else {
            contacts.filter {
                it.username?.contains(searchQuery, ignoreCase = true) == true ||
                        it.fullName?.contains(searchQuery, ignoreCase = true) == true
            }.take(9)
        }

        Log.d("ShareBottomSheet", "Filtered result: ${result.size} contacts")
        result.forEach { contact ->
            Log.d("ShareBottomSheet", "Filtered contact: ${contact.username}")
        }
        result
    }

    val sharePlatforms = remember(post, context, onDismiss) {
        listOf(
            SharePlatform(
                name = "WhatsApp",
                iconRes = R.drawable.ic_whatsapp,
                color = Color(0xFF25D366)
            ) {
                shareViaWhatsApp(post, context)
                onDismiss()
            },
            SharePlatform(
                name = "Mail",
                iconRes = R.drawable.ic_gmail,
                color = Color(0xFFEA4335)
            ) {
                shareViaEmail(post, context)
                onDismiss()
            },
            SharePlatform(
                name = "Share",
                iconRes = R.drawable.ic_share2,
                color = Color(0xFFEA4335)
            ) {
                shareGeneric(post, context)
                onDismiss()
            },
            SharePlatform(
                name = "Copy Link",
                iconRes = R.drawable.ic_link,
                color = Color(0xFFEA4335)
            ) {
                copyPostLink(post, context)
                onDismiss()
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Share",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search",
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )


        when {
            // Show loading when either profileLoading is true OR isLoadingContacts is true
            profileLoading || isLoadingContacts -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading contacts...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            // Show "no connections" only when we're not loading and have no contacts
            filteredContacts.isEmpty() && !profileLoading && !isLoadingContacts -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No connections found" else "No matching contacts",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            // Show contacts grid
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 24.dp)
                ) {
                    items(filteredContacts) { contact ->
                        ContactItem(
                            contact = contact,
                            onContactClick = { selectedContact ->
                                selectedContact.username?.let { shareToContact(it, post, context) }
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }

        // Share Platform Options
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            sharePlatforms.forEach { platform ->
                SharePlatformItem(
                    platform = platform,
                    onClick = platform.action
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onContactClick: (Contact) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onContactClick(contact) }
            .padding(4.dp)
    ) {
        // Profile Image
        AsyncImage(
            model = contact.profilePic ?: R.drawable.ic_profile,
            contentDescription = contact.username,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_profile),
            error = painterResource(id = R.drawable.ic_profile)
        )

        Spacer(modifier = Modifier.height(8.dp))

        (contact.fullName ?: contact.username)?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
fun SharePlatformItem(
    platform: SharePlatform,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        if (platform.iconRes != null) {
            // Use drawable resource for WhatsApp and Gmail - no background, just the icon
            Image(
                painter = painterResource(id = platform.iconRes),
                contentDescription = platform.name,
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )
        } else if (platform.icon != null) {
            // Use Material Icon for other platforms with circular background
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(platform.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = platform.icon,
                    contentDescription = platform.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = platform.name,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

// Updated function to load user contacts focusing on accepted_connections
suspend fun loadUserContacts(context: Context): List<Contact> {
    return try {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()

        if (token.isNotEmpty()) {
            // Make API call to get user profile with connections
            val response = RetrofitInstance.api.getUserProfile("Bearer $token")

            if (response.isSuccessful) {
                response.body()?.let { userProfile ->
                    // Only show accepted_connections for sharing
                    val contacts = mutableListOf<Contact>()

                    // Add only accepted connections (limit to 9)
                    userProfile.accepted_connections.users.forEach { user ->
                        contacts.add(
                            Contact(
                                userId = user.user_id,
                                username = user.username,
                                fullName = user.full_name,
                                profilePic = user.profile_pic,
                                bio = user.bio
                            )
                        )
                    }

                    contacts
                } ?: emptyList()
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// Share functions
private fun shareViaWhatsApp(post: Post, context: Context) {
    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        shareGeneric(post, context)
    }
}

private fun shareViaEmail(post: Post, context: Context) {
    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Shared Post from CreatorCircle")
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Send email"))
    } catch (e: Exception) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}

private fun copyPostLink(post: Post, context: Context) {
    val postLink = "https://creatorcircle.app/post/${post.id}"

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Post Link", postLink)
    clipboardManager.setPrimaryClip(clipData)

    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareToContact(contactName: String, post: Post, context: Context) {
    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
    Toast.makeText(context, "Shared with $contactName", Toast.LENGTH_SHORT).show()
    // TODO: Implement actual sharing to specific contact through your app's messaging system
}

private fun shareGeneric(post: Post, context: Context) {
    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
}













//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.cc.creatorcircle.ui.components
//
//import android.content.Context
//import android.content.Intent
//import android.widget.Toast
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import android.content.ClipData
//import android.content.ClipboardManager
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.data.models.UserProfile
//
//// Updated Contact data class to match UserInfo structure
//data class Contact(
//    val userId: Int,
//    val username: String,
//    val fullName: String?,
//    val profilePic: String?,
//    val bio: String?
//)
//
//data class SharePlatform(
//    val name: String,
//    val icon: ImageVector? = null,
//    val iconRes: Int? = null,
//    val color: Color,
//    val action: () -> Unit
//)
//
//@Composable
//fun ShareBottomSheetContent(
//    post: com.cc.creatorcircle.data.models.Post,
//    context: Context,
//    onDismiss: () -> Unit,
//    viewModel: com.cc.creatorcircle.viewModel.PostsViewModel? = null
//) {
//    var searchQuery by remember { mutableStateOf("") }
//    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
//    var isLoadingContacts by remember { mutableStateOf(true) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Load user connections when the bottom sheet opens
//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            try {
//                val userContacts = loadUserContacts(context)
//                contacts = userContacts
//            } catch (e: Exception) {
//                Toast.makeText(context, "Failed to load contacts", Toast.LENGTH_SHORT).show()
//            } finally {
//                isLoadingContacts = false
//            }
//        }
//    }
//
//    val filteredContacts = remember(searchQuery, contacts) {
//        if (searchQuery.isEmpty()) {
//            contacts.take(9) // Show max 9 contacts in grid
//        } else {
//            contacts.filter {
//                it.username.contains(searchQuery, ignoreCase = true) ||
//                        it.fullName?.contains(searchQuery, ignoreCase = true) == true
//            }.take(9)
//        }
//    }
//
//    val sharePlatforms = remember(post, context, onDismiss) {
//        listOf(
//            SharePlatform(
//                name = "WhatsApp",
//                iconRes = R.drawable.ic_whatsapp,
//                color = Color(0xFF25D366)
//            ) {
//                shareViaWhatsApp(post, context)
//                onDismiss()
//            },
//            SharePlatform(
//                name = "Mail",
//                iconRes = R.drawable.ic_gmail,
//                color = Color(0xFFEA4335)
//            ) {
//                shareViaEmail(post, context)
//                onDismiss()
//            },
//            SharePlatform(
//                name = "Share",
//                iconRes = R.drawable.ic_share2,
//                color = Color(0xFFEA4335)
//            ) {
//                shareViaEmail(post, context)
//                onDismiss()
//            },
//            SharePlatform(
//                name = "Copy Link",
//                iconRes = R.drawable.ic_link,
//                color = Color(0xFFEA4335)
//            ) {
//                copyPostLink(post, context)
//                onDismiss()
//            },
//
//        )
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color.White)
//            .navigationBarsPadding()
//            .padding(16.dp)
//    ) {
//        // Handle bar
////        Box(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(bottom = 16.dp),
////            contentAlignment = Alignment.Center
////        ) {
////            Box(
////                modifier = Modifier
////                    .width(40.dp)
////                    .height(4.dp)
////                    .background(
////                        Color.Gray.copy(alpha = 0.3f),
////                        RoundedCornerShape(2.dp)
////                    )
////            )
////        }
//
//        // Title
//        Text(
//            text = "Share",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Black,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        // Search Bar
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = { searchQuery = it },
//            placeholder = {
//                Text(
//                    "Search",
//                    color = Color.Gray.copy(alpha = 0.7f)
//                )
//            },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Search,
//                    contentDescription = "Search",
//                    tint = Color.Gray.copy(alpha = 0.7f),
//                    modifier = Modifier.size(20.dp)
//                )
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 24.dp),
//            shape = RoundedCornerShape(12.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color.Gray.copy(alpha = 0.3f),
//                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
//                focusedContainerColor = Color.White,
//                unfocusedContainerColor = Color.White
//            ),
//            singleLine = true
//        )
//
//        // Contacts Grid
//        if (isLoadingContacts) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (filteredContacts.isEmpty() && !isLoadingContacts) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = if (searchQuery.isEmpty()) "No connections found" else "No matching contacts",
//                    color = Color.Gray,
//                    fontSize = 14.sp
//                )
//            }
//        } else {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(3),
//                horizontalArrangement = Arrangement.spacedBy(24.dp),
//                verticalArrangement = Arrangement.spacedBy(20.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .padding(bottom = 24.dp)
//            ) {
//                items(filteredContacts) { contact ->
//                    ContactItem(
//                        contact = contact,
//                        onContactClick = { selectedContact ->
//                            shareToContact(selectedContact.username, post, context)
//                            onDismiss()
//                        }
//                    )
//                }
//            }
//        }
//
//        // Share Platform Options
//        Row(
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp)
//        ) {
//            sharePlatforms.forEach { platform ->
//                SharePlatformItem(
//                    platform = platform,
//                    onClick = platform.action
//                )
//            }
//        }
//
//        // Page indicators
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(vertical = 16.dp),
////            horizontalArrangement = Arrangement.Center
////        ) {
////            repeat(4) { index ->
////                Box(
////                    modifier = Modifier
////                        .size(8.dp)
////                        .background(
////                            if (index == 0) Color(0xFF8B5CF6) else Color.Gray.copy(alpha = 0.3f),
////                            CircleShape
////                        )
////                )
////                if (index < 3) {
////                    Spacer(modifier = Modifier.width(8.dp))
////                }
////            }
////        }
//    }
//}
//
//@Composable
//fun ContactItem(
//    contact: Contact,
//    onContactClick: (Contact) -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .clickable { onContactClick(contact) }
//            .padding(4.dp)
//    ) {
//        // Profile Image
//        AsyncImage(
//            model = contact.profilePic ?: R.drawable.ic_profile,
//            contentDescription = contact.username,
//            modifier = Modifier
//                .size(60.dp)
//                .clip(CircleShape),
//            contentScale = ContentScale.Crop,
//            placeholder = painterResource(id = R.drawable.ic_profile),
//            error = painterResource(id = R.drawable.ic_profile)
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = contact.fullName ?: contact.username,
//            fontSize = 12.sp,
//            color = Color.Black,
//            textAlign = TextAlign.Center,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            modifier = Modifier.width(80.dp)
//        )
//    }
//}
//
//@Composable
//fun SharePlatformItem(
//    platform: SharePlatform,
//    onClick: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.clickable { onClick() }
//    ) {
//        if (platform.iconRes != null) {
//            // Use drawable resource for WhatsApp and Gmail - no background, just the icon
//            Image(
//                painter = painterResource(id = platform.iconRes),
//                contentDescription = platform.name,
//                modifier = Modifier.size(34.dp),
//                contentScale = ContentScale.Fit
//            )
//        } else if (platform.icon != null) {
//            // Use Material Icon for other platforms with circular background
//            Box(
//                modifier = Modifier
//                    .size(34.dp)
//                    .background(platform.color, CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = platform.icon,
//                    contentDescription = platform.name,
//                    tint = Color.White,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = platform.name,
//            fontSize = 12.sp,
//            color = Color.Black,
//            textAlign = TextAlign.Center
//        )
//    }
//}
//
//// Updated function to load user contacts focusing on accepted_connections
//suspend fun loadUserContacts(context: Context): List<Contact> {
//    return try {
//        val tokenManager = com.cc.creatorcircle.utils.TokenManager(context)
//        val token = tokenManager.getToken()
//
//        if (token.isNotEmpty()) {
//            // Make API call to get user profile with connections
//            val response = com.cc.creatorcircle.data.api.RetrofitInstance.api.getUserProfile("Bearer $token")
//
//            if (response.isSuccessful) {
//                response.body()?.let { userProfile ->
//                    // Only show accepted_connections for sharing
//                    val contacts = mutableListOf<Contact>()
//
//                    // Add only accepted connections (limit to 9)
//                    userProfile.accepted_connections.users.take(9).forEach { user ->
//                        contacts.add(
//                            Contact(
//                                userId = user.user_id,
//                                username = user.username,
//                                fullName = user.full_name,
//                                profilePic = user.profile_pic,
//                                bio = user.bio
//                            )
//                        )
//                    }
//
//                    contacts
//                } ?: emptyList()
//            } else {
//                emptyList()
//            }
//        } else {
//            emptyList()
//        }
//    } catch (e: Exception) {
//        emptyList()
//    }
//}
//
//// Share functions
//private fun shareViaWhatsApp(post: com.cc.creatorcircle.data.models.Post, context: Context) {
//    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_TEXT, shareText)
//        setPackage("com.whatsapp")
//    }
//
//    try {
//        context.startActivity(intent)
//    } catch (e: Exception) {
//        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
//        shareGeneric(post, context)
//    }
//}
//
//private fun shareViaEmail(post: com.cc.creatorcircle.data.models.Post, context: Context) {
//    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_TEXT, shareText)
//        putExtra(Intent.EXTRA_SUBJECT, "Shared Post from CreatorCircle")
//    }
//
//    try {
//        context.startActivity(Intent.createChooser(intent, "Send email"))
//    } catch (e: Exception) {
//        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
//    }
//}
//
//private fun copyPostLink(post: com.cc.creatorcircle.data.models.Post, context: Context) {
//    val postLink = "https://creatorcircle.app/post/${post.id}"
//
//    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//    val clipData = ClipData.newPlainText("Post Link", postLink)
//    clipboardManager.setPrimaryClip(clipData)
//
//    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
//}
//
//private fun shareToContact(contactName: String, post: com.cc.creatorcircle.data.models.Post, context: Context) {
//    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
//    Toast.makeText(context, "Shared with $contactName", Toast.LENGTH_SHORT).show()
//    // TODO: Implement actual sharing to specific contact through your app's messaging system
//}
//
//private fun shareGeneric(post: com.cc.creatorcircle.data.models.Post, context: Context) {
//    val shareText = "Check out this post by ${post.author.name ?: "Anonymous"}: ${post.content}"
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_TEXT, shareText)
//    }
//
//    context.startActivity(Intent.createChooser(intent, "Share via"))
//}