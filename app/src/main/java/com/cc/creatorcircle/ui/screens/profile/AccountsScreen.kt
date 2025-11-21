package com.cc.creatorcircle.ui.screens.profile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.repository.PlatformFollower
import com.cc.creatorcircle.data.repository.PlatformFollowers
import com.cc.creatorcircle.data.repository.SocialMediaLink
import com.cc.creatorcircle.data.repository.SocialMediaLinks
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserUpdateState
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType




//data class InstagramAccount(
//    val username: String,
//    val link: String,
//    val followers: Int,
//    val isPrimary: Boolean
//)

// UPDATED: InstagramAccount data class with String followers
data class InstagramAccount(
    val username: String,
    val link: String,
    val followers: String,  // Changed from Int to String
    val isPrimary: Boolean
)


//@Composable
//fun AccountsScreen(
//    navController: NavController,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    val userViewModel = remember { UserViewModel(context) }
//    val firebaseAnalytics = remember { Firebase.analytics }
//
//    // Collect user profile from UserViewModel
//    val userProfile by userViewModel.userProfile.collectAsState()
//    val isLoading by userViewModel.isLoading.collectAsState()
//    val error by userViewModel.error.collectAsState()
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // State for Instagram accounts
//    var instagramAccounts by remember { mutableStateOf<List<InstagramAccount>>(emptyList()) }
//    var showAddDialog by remember { mutableStateOf(false) }
//    var showEditDialog by remember { mutableStateOf(false) }
//    var selectedAccountIndex by remember { mutableStateOf(-1) }
//
//    // Fetch user profile when screen loads
//    LaunchedEffect(Unit) {
//        userViewModel.fetchUserProfile()
//    }
//
//    // Load Instagram accounts from profile
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            val accounts = mutableListOf<InstagramAccount>()
//
//            try {
//                // Extract Instagram data from social_media_links
//                val instagramLinks = profile.social_media_links?.get("instagram") as? List<*>
//
//                instagramLinks?.forEachIndexed { index, item ->
//                    val instagramMap = item as? Map<*, *>
//                    val username = (instagramMap?.get("username") as? String) ?: ""
//                    val link = (instagramMap?.get("link") as? String) ?: ""
//                    val isPrimary = (instagramMap?.get("is_primary") as? Boolean) ?: false
//
//                    // Get followers from platform_followers
//                    val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
//                    val accountFollowers = platformFollowers?.getOrNull(index) as? Map<*, *>
//                    val followers = (accountFollowers?.get("followers") as? Number)?.toInt() ?: 0
//
//                    if (username.isNotBlank() || link.isNotBlank()) {
//                        accounts.add(
//                            InstagramAccount(
//                                username = username,
//                                link = link,
//                                followers = followers,
//                                isPrimary = isPrimary
//                            )
//                        )
//                    }
//                }
//
//                Log.d("AccountsScreen", "Loaded ${accounts.size} Instagram accounts")
//            } catch (e: Exception) {
//                Log.e("AccountsScreen", "Error parsing Instagram data", e)
//            }
//
//            instagramAccounts = accounts
//        }
//    }
//
//    // Handle update state changes
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UserUpdateState.Success -> {
//                Toast.makeText(context, "Instagram account updated successfully!", Toast.LENGTH_SHORT).show()
//                userViewModel.resetUpdateState()
//                userViewModel.fetchUserProfile()
//            }
//            is UserUpdateState.Error -> {
//                val errorMessage = (updateState as UserUpdateState.Error).message
//                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
//                userViewModel.resetUpdateState()
//            }
//            else -> {}
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFAF8F8))
//    ) {
//        when {
//            isLoading -> {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                    color = Color(0xFFB388FF)
//                )
//            }
//
//            error != null -> {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = error ?: "An error occurred",
//                        color = Color.Red,
//                        fontSize = 16.sp
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = { userViewModel.fetchUserProfile() },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFB388FF)
//                        )
//                    ) {
//                        Text("Retry")
//                    }
//                }
//            }
//
//            else -> {
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    // Top Bar
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_left_arrow),
//                            contentDescription = "Back",
//                            modifier = Modifier
//                                .size(26.dp)
//                                .clickable { onDismiss() },
//                            tint = Color.Black
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text(
//                            text = "Personal Information",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                    }
//
//                    // Scrollable Content
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .verticalScroll(rememberScrollState())
//                            .padding(horizontal = 24.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Instagram Accounts Section Header
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.ic_instagram),
//                                    contentDescription = "Instagram",
//                                    tint = Color(0xFFE1306C),
//                                    modifier = Modifier.size(28.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Instagram Account",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black
//                                )
//                            }
//                            Text(
//                                text = "${instagramAccounts.size} linked",
//                                fontSize = 14.sp,
//                                color = Color(0xFF757575)
//                            )
//                        }
//
//                        // Display Instagram Accounts
//                        if (instagramAccounts.isEmpty()) {
//                            // Empty state
//                            Surface(
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(16.dp),
//                                color = Color.White,
//                                shadowElevation = 2.dp
//                            ) {
//                                Column(
//                                    modifier = Modifier.padding(24.dp),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.ic_instagram),
//                                        contentDescription = "No accounts",
//                                        tint = Color(0xFFE0E0E0),
//                                        modifier = Modifier.size(48.dp)
//                                    )
//                                    Spacer(modifier = Modifier.height(12.dp))
//                                    Text(
//                                        text = "No Instagram accounts linked",
//                                        fontSize = 14.sp,
//                                        color = Color(0xFF757575)
//                                    )
//                                    Spacer(modifier = Modifier.height(16.dp))
//                                    TextButton(
//                                        onClick = { showAddDialog = true }
//                                    ) {
//                                        Text(
//                                            text = "+ Add Account",
//                                            color = Color(0xFF8B5CF6),
//                                            fontWeight = FontWeight.Medium
//                                        )
//                                    }
//                                }
//                            }
//                        } else {
//                            instagramAccounts.forEachIndexed { index, account ->
//                                InstagramAccountCard(
//                                    account = account,
//                                    onEdit = {
//                                        selectedAccountIndex = index
//                                        showEditDialog = true
//                                    },
//                                    onDelete = {
//                                        instagramAccounts = instagramAccounts.filterIndexed { i, _ -> i != index }
//                                    }
//                                )
//                            }
//
//                            // Add more button
//                            TextButton(
//                                onClick = { showAddDialog = true },
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text(
//                                    text = "+ Add Another Account",
//                                    color = Color(0xFF8B5CF6),
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//
//                }
//            }
//        }
//
//        // Add/Edit Dialog
//        if (showAddDialog || showEditDialog) {
//            InstagramAccountDialog(
//                isEdit = showEditDialog,
//                initialLink = if (showEditDialog && selectedAccountIndex >= 0) {
//                    instagramAccounts[selectedAccountIndex].link
//                } else "",
//                userViewModel = userViewModel,
//                onDismiss = {
//                    showAddDialog = false
//                    showEditDialog = false
//                    selectedAccountIndex = -1
//                },
//                onConfirmSuccess = {
//                    showAddDialog = false
//                    showEditDialog = false
//                    selectedAccountIndex = -1
//                    userViewModel.fetchUserProfile()
//                }
//            )
//        }
//    }
//}



/*
@Composable
fun AccountsScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(context) }
    val firebaseAnalytics = remember { Firebase.analytics }

    // Collect user profile from UserViewModel
    val userProfile by userViewModel.userProfile.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    val updateState by userViewModel.updateState.collectAsState()

    // State for Instagram accounts
    var instagramAccounts by remember { mutableStateOf<List<InstagramAccount>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedAccountIndex by remember { mutableStateOf(-1) }

    // State for follower count editing
    var editingFollowerIndex by remember { mutableStateOf(-1) }
    var editedFollowerCounts by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
    }

    // Load Instagram accounts from profile
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val accounts = mutableListOf<InstagramAccount>()

            try {
                // Extract Instagram data from social_media_links
                val instagramLinks = profile.social_media_links?.get("instagram") as? List<*>

                instagramLinks?.forEachIndexed { index, item ->
                    val instagramMap = item as? Map<*, *>
                    val username = (instagramMap?.get("username") as? String) ?: ""
                    val link = (instagramMap?.get("link") as? String) ?: ""
                    val isPrimary = (instagramMap?.get("is_primary") as? Boolean) ?: false

                    // Get followers from platform_followers
                    val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
                    val accountFollowers = platformFollowers?.getOrNull(index) as? Map<*, *>
                    val followers = (accountFollowers?.get("followers") as? Number)?.toInt() ?: 0

                    if (username.isNotBlank() || link.isNotBlank()) {
                        accounts.add(
                            InstagramAccount(
                                username = username,
                                link = link,
                                followers = followers,
                                isPrimary = isPrimary
                            )
                        )
                    }
                }

                Log.d("AccountsScreen", "Loaded ${accounts.size} Instagram accounts")
            } catch (e: Exception) {
                Log.e("AccountsScreen", "Error parsing Instagram data", e)
            }

            instagramAccounts = accounts
        }
    }


    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Instagram account updated successfully!", Toast.LENGTH_SHORT).show()
                userViewModel.resetUpdateState()

                // Update the local followers count before fetching profile
                if (editingFollowerIndex >= 0) {
                    val newFollowerCount = editedFollowerCounts[editingFollowerIndex]?.toIntOrNull() ?: 0
                    instagramAccounts = instagramAccounts.mapIndexed { index, account ->
                        if (index == editingFollowerIndex) {
                            account.copy(followers = newFollowerCount)
                        } else {
                            account
                        }
                    }
                }

                editingFollowerIndex = -1
                editedFollowerCounts = emptyMap()
                userViewModel.fetchUserProfile()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                userViewModel.resetUpdateState()
            }
            else -> {}
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
                    Button(
                        onClick = { userViewModel.fetchUserProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB388FF)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
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
                            text = "Personal Information",
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Instagram Accounts Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    tint = Color(0xFFE1306C),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Instagram Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = "${instagramAccounts.size} linked",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        // Display Instagram Accounts
                        if (instagramAccounts.isEmpty()) {
                            // Empty state
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_instagram),
                                        contentDescription = "No accounts",
                                        tint = Color(0xFFE0E0E0),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Instagram accounts linked",
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(
                                        onClick = { showAddDialog = true }
                                    ) {
                                        Text(
                                            text = "+ Add Account",
                                            color = Color(0xFF8B5CF6),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            instagramAccounts.forEachIndexed { index, account ->
                                InstagramAccountCard(
                                    account = account,
                                    isEditingFollowers = editingFollowerIndex == index,
                                    editedFollowerCount = editedFollowerCounts[index] ?: account.followers.toString(),
                                    onFollowerCountChange = { newCount ->
                                        editedFollowerCounts = editedFollowerCounts.toMutableMap().apply {
                                            put(index, newCount)
                                        }
                                    },
                                    onStartEditFollowers = {
                                        editingFollowerIndex = index
                                        editedFollowerCounts = mapOf(index to account.followers.toString())
                                    },
                                    onSaveFollowers = {
                                        val followerCount = editedFollowerCounts[index]?.toIntOrNull() ?: account.followers
                                        userViewModel.updateUser(
                                            platformFollowers = PlatformFollowers(
                                                instagram = listOf(
                                                    PlatformFollower(
                                                        followers = followerCount.toString(),
                                                        is_primary = true
                                                    )
                                                )
                                            )
                                        )
                                    },
                                    onCancelEditFollowers = {
                                        editingFollowerIndex = -1
                                        editedFollowerCounts = emptyMap()
                                    },
                                    onEdit = {
                                        selectedAccountIndex = index
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        instagramAccounts = instagramAccounts.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            }

                            // Add more button
                            TextButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "+ Add Another Account",
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog || showEditDialog) {
            InstagramAccountDialog(
                isEdit = showEditDialog,
                initialLink = if (showEditDialog && selectedAccountIndex >= 0) {
                    instagramAccounts[selectedAccountIndex].link
                } else "",
                userViewModel = userViewModel,
                onDismiss = {
                    showAddDialog = false
                    showEditDialog = false
                    selectedAccountIndex = -1
                },
                onConfirmSuccess = {
                    showAddDialog = false
                    showEditDialog = false
                    selectedAccountIndex = -1
                    userViewModel.fetchUserProfile()
                }
            )
        }
    }
}


@Composable
fun InstagramAccountCard(
    account: InstagramAccount,
    isEditingFollowers: Boolean,
    editedFollowerCount: String,
    onFollowerCountChange: (String) -> Unit,
    onStartEditFollowers: () -> Unit,
    onSaveFollowers: () -> Unit,
    onCancelEditFollowers: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${account.username}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingFollowers) {
                        OutlinedTextField(
                            value = editedFollowerCount,
                            onValueChange = onFollowerCountChange,
                            label = { Text("Followers") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.clickable { onStartEditFollowers() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${account.followers} followers",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit followers",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }

                if (account.isPrimary) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Primary",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditingFollowers) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelEditFollowers,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSaveFollowers,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    // Delete Button
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}



 */


@Composable
fun AccountsScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(context) }
    val firebaseAnalytics = remember { Firebase.analytics }

    // Collect user profile from UserViewModel
    val userProfile by userViewModel.userProfile.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    val updateState by userViewModel.updateState.collectAsState()

    // State for Instagram accounts - CHANGED: followers is now String
    var instagramAccounts by remember { mutableStateOf<List<InstagramAccount>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedAccountIndex by remember { mutableStateOf(-1) }

    // State for follower count editing
    var editingFollowerIndex by remember { mutableStateOf(-1) }
    var editedFollowerCounts by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
    }

    // Load Instagram accounts from profile - UPDATED to handle String followers
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val accounts = mutableListOf<InstagramAccount>()

            try {
                // Extract Instagram data from social_media_links
                val instagramLinks = profile.social_media_links?.get("instagram") as? List<*>

                instagramLinks?.forEachIndexed { index, item ->
                    val instagramMap = item as? Map<*, *>
                    val username = (instagramMap?.get("username") as? String) ?: ""
                    val link = (instagramMap?.get("link") as? String) ?: ""
                    val isPrimary = (instagramMap?.get("is_primary") as? Boolean) ?: false

                    // Get followers from platform_followers - HANDLE AS STRING
                    val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
                    val accountFollowers = platformFollowers?.getOrNull(index) as? Map<*, *>

                    // Try to get followers as string first, then as number, default to "0"
                    val followers = when (val followersValue = accountFollowers?.get("followers")) {
                        is String -> followersValue
                        is Number -> followersValue.toString()
                        else -> "0"
                    }

                    if (username.isNotBlank() || link.isNotBlank()) {
                        accounts.add(
                            InstagramAccount(
                                username = username,
                                link = link,
                                followers = followers,
                                isPrimary = isPrimary
                            )
                        )
                    }
                }

                Log.d("AccountsScreen", "Loaded ${accounts.size} Instagram accounts")
            } catch (e: Exception) {
                Log.e("AccountsScreen", "Error parsing Instagram data", e)
            }

            instagramAccounts = accounts
        }
    }

    // Handle update state changes - UPDATED for String followers
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Instagram account updated successfully!", Toast.LENGTH_SHORT).show()
                userViewModel.resetUpdateState()

                // Update the local followers count before fetching profile
                if (editingFollowerIndex >= 0) {
                    val newFollowerCount = editedFollowerCounts[editingFollowerIndex] ?: "0"
                    instagramAccounts = instagramAccounts.mapIndexed { index, account ->
                        if (index == editingFollowerIndex) {
                            account.copy(followers = newFollowerCount)
                        } else {
                            account
                        }
                    }
                }

                editingFollowerIndex = -1
                editedFollowerCounts = emptyMap()
                userViewModel.fetchUserProfile()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                userViewModel.resetUpdateState()
            }
            else -> {}
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
                    Button(
                        onClick = { userViewModel.fetchUserProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB388FF)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
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
                            text = "Personal Information",
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Instagram Accounts Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Image(
                                    painter = painterResource(id = R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Instagram Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = "${instagramAccounts.size} linked",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        // Display Instagram Accounts
                        if (instagramAccounts.isEmpty()) {
                            // Empty state
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_instagram),
                                        contentDescription = "No accounts",
                                        tint = Color(0xFFE0E0E0),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Instagram accounts linked",
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(
                                        onClick = { showAddDialog = true }
                                    ) {
                                        Text(
                                            text = "+ Add Account",
                                            color = Color(0xFF8B5CF6),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            instagramAccounts.forEachIndexed { index, account ->
                                InstagramAccountCard(
                                    account = account,
                                    isEditingFollowers = editingFollowerIndex == index,
                                    editedFollowerCount = editedFollowerCounts[index] ?: account.followers,
                                    onFollowerCountChange = { newCount ->
                                        editedFollowerCounts = editedFollowerCounts.toMutableMap().apply {
                                            put(index, newCount)
                                        }
                                    },
                                    onStartEditFollowers = {
                                        editingFollowerIndex = index
                                        editedFollowerCounts = mapOf(index to account.followers)
                                    },
                                    onSaveFollowers = {
                                        val followerCount = editedFollowerCounts[index] ?: account.followers
                                        userViewModel.updateUser(
                                            platformFollowers = PlatformFollowers(
                                                instagram = listOf(
                                                    PlatformFollower(
                                                        followers = followerCount,  // Keep as String
                                                        is_primary = true
                                                    )
                                                )
                                            )
                                        )
                                    },
                                    onCancelEditFollowers = {
                                        editingFollowerIndex = -1
                                        editedFollowerCounts = emptyMap()
                                    },
                                    onEdit = {
                                        selectedAccountIndex = index
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        instagramAccounts = instagramAccounts.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            }

                            // Add more button
                            TextButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "+ Add Another Account",
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog || showEditDialog) {
            InstagramAccountDialog(
                isEdit = showEditDialog,
                initialLink = if (showEditDialog && selectedAccountIndex >= 0) {
                    instagramAccounts[selectedAccountIndex].link
                } else "",
                userViewModel = userViewModel,
                onDismiss = {
                    showAddDialog = false
                    showEditDialog = false
                    selectedAccountIndex = -1
                },
                onConfirmSuccess = {
                    showAddDialog = false
                    showEditDialog = false
                    selectedAccountIndex = -1
                    userViewModel.fetchUserProfile()
                }
            )
        }
    }
}

@Composable
fun InstagramAccountCard(
    account: InstagramAccount,
    isEditingFollowers: Boolean,
    editedFollowerCount: String,
    onFollowerCountChange: (String) -> Unit,
    onStartEditFollowers: () -> Unit,
    onSaveFollowers: () -> Unit,
    onCancelEditFollowers: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${account.username}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingFollowers) {
                        OutlinedTextField(
                            value = editedFollowerCount,
                            onValueChange = onFollowerCountChange,
                            label = { Text("Followers") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.clickable { onStartEditFollowers() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${account.followers} followers",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit followers",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }

                if (account.isPrimary) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Primary",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditingFollowers) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelEditFollowers,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSaveFollowers,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    // Delete Button
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}


//@Composable
//fun InstagramAccountCard(
//    account: InstagramAccount,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit
//) {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        color = Color.White,
//        shadowElevation = 2.dp
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = "@${account.username}",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = "${account.followers} followers",
//                        fontSize = 14.sp,
//                        color = Color(0xFF757575)
//                    )
//                }
//
//                if (account.isPrimary) {
//                    Surface(
//                        shape = RoundedCornerShape(12.dp),
//                        color = Color(0xFFE3F2FD)
//                    ) {
//                        Text(
//                            text = "Primary",
//                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color(0xFF1976D2)
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                // Edit Button
//                OutlinedButton(
//                    onClick = onEdit,
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = Color(0xFF8B5CF6)
//                    ),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_edit),
//                        contentDescription = "Edit",
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Edit")
//                }
//
//                // Delete Button
//                OutlinedButton(
//                    onClick = onDelete,
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = Color(0xFFE53935)
//                    ),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Delete,
//                        contentDescription = "Delete",
//                        tint = Color.Red,
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Delete")
//                }
//            }
//        }
//    }
//}
//





@Composable
fun InstagramAccountDialog(
    isEdit: Boolean,
    initialLink: String,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    onConfirmSuccess: () -> Unit
) {
    var link by remember { mutableStateOf(initialLink) }
    var username by remember { mutableStateOf("") }
    val updateState by userViewModel.updateState.collectAsState()

    // Extract username from initial link when dialog opens
    LaunchedEffect(initialLink) {
        if (initialLink.isNotBlank()) {
            username = extractUsernameFromLink(initialLink)
        }
    }

    // Handle successful update
    LaunchedEffect(updateState) {
        if (updateState is UserUpdateState.Success) {
            onConfirmSuccess()
        }
    }

    // Function to format Instagram link
    fun formatInstagramLink(input: String): String {
        val trimmedInput = input.trim()

        // If already a full URL, return as is
        if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")) {
            return trimmedInput
        }

        // Remove common Instagram prefixes if user included them
        val extractedUsername = trimmedInput
            .removePrefix("instagram.com/")
            .removePrefix("www.instagram.com/")
            .removePrefix("@")

        // Add the full Instagram URL prefix
        return "https://www.instagram.com/$extractedUsername"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Instagram Account" else "Add Instagram Account",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        link = formatInstagramLink(it)
                    },
                    label = { Text("Username") },
                    placeholder = { Text("e.g., builtbrody") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = updateState !is UserUpdateState.Loading
                )

                // Profile Link Field
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Profile Link") },
                    placeholder = { Text("https://instagram.com/username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = updateState !is UserUpdateState.Loading
                )

                if (updateState is UserUpdateState.Loading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFB388FF),
                            strokeWidth = 2.dp
                        )
                    }
                }

                if (updateState is UserUpdateState.Error) {
                    Text(
                        text = (updateState as UserUpdateState.Error).message,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isNotBlank()) {
                        val formattedLink = formatInstagramLink(username)
                        userViewModel.updateUser(
                            socialMediaLinks = SocialMediaLinks(
                                instagram = listOf(
                                    SocialMediaLink(
                                        link = formattedLink,
                                        username = username.trim(),
                                        is_primary = true
                                    )
                                )
                            )
                        )
                    }
                },
                enabled = username.isNotBlank() && updateState !is UserUpdateState.Loading
            ) {
                Text(
                    text = if (isEdit) "Update" else "Add",
                    color = Color(0xFF8B5CF6)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = updateState !is UserUpdateState.Loading
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF757575)
                )
            }
        },
        containerColor = Color.White
    )
}

// Function to extract username from link
fun extractUsernameFromLink(url: String): String {
    return url
        .removePrefix("https://www.instagram.com/")
        .removePrefix("http://www.instagram.com/")
        .removePrefix("https://instagram.com/")
        .removePrefix("http://instagram.com/")
        .removeSuffix("/")
}




//
//@Composable
//fun InstagramAccountDialog(
//    isEdit: Boolean,
//    initialLink: String,
//    userViewModel: UserViewModel,
//    onDismiss: () -> Unit,
//    onConfirmSuccess: () -> Unit
//) {
//    var link by remember { mutableStateOf(initialLink) }
//    var username by remember { mutableStateOf("") }
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // Extract username from initial link when dialog opens
//    LaunchedEffect(initialLink) {
//        if (initialLink.isNotBlank()) {
//            username = extractUsernameFromLink(initialLink)
//        }
//    }
//
//    // Handle successful update
//    LaunchedEffect(updateState) {
//        if (updateState is UserUpdateState.Success) {
//            onConfirmSuccess()
//        }
//    }
//
//    // Function to format Instagram link
//    fun formatInstagramLink(input: String): String {
//        val trimmedInput = input.trim()
//
//        // If already a full URL, return as is
//        if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")) {
//            return trimmedInput
//        }
//
//        // Remove common Instagram prefixes if user included them
//        val extractedUsername = trimmedInput
//            .removePrefix("instagram.com/")
//            .removePrefix("www.instagram.com/")
//            .removePrefix("@")
//
//        // Add the full Instagram URL prefix
//        return "https://www.instagram.com/$extractedUsername"
//    }
//
//
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = if (isEdit) "Edit Instagram Account" else "Add Instagram Account",
//                fontWeight = FontWeight.SemiBold
//            )
//        },
//        text = {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Username Field
//                OutlinedTextField(
//                    value = username,
//                    onValueChange = {
//                        username = it
//                        link = formatInstagramLink(it)
//                    },
//                    label = { Text("Username") },
//                    placeholder = { Text("e.g., builtbrody") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color(0xFFE0E0E0),
//                        focusedBorderColor = Color(0xFF8B5CF6)
//                    ),
//                    shape = RoundedCornerShape(12.dp),
//                    enabled = updateState !is UserUpdateState.Loading
//                )
//
//                // Profile Link Field
//                OutlinedTextField(
//                    value = link,
//                    onValueChange = { link = it },
//                    label = { Text("Profile Link") },
//                    placeholder = { Text("https://instagram.com/username") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color(0xFFE0E0E0),
//                        focusedBorderColor = Color(0xFF8B5CF6)
//                    ),
//                    shape = RoundedCornerShape(12.dp),
//                    enabled = updateState !is UserUpdateState.Loading
//                )
//
//                if (updateState is UserUpdateState.Loading) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = Color(0xFFB388FF),
//                            strokeWidth = 2.dp
//                        )
//                    }
//                }
//
//                if (updateState is UserUpdateState.Error) {
//                    Text(
//                        text = (updateState as UserUpdateState.Error).message,
//                        color = Color.Red,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    if (username.isNotBlank()) {
//                        val formattedLink = formatInstagramLink(username)
//                        userViewModel.updateUser(
//                            socialMediaLinks = SocialMediaLinks(
//                                instagram = listOf(
//                                    SocialMediaLink(
//                                        link = formattedLink,
//                                        username = username.trim(),
//                                        is_primary = true
//                                    )
//                                )
//                            )
//                        )
//                    }
//                },
//                enabled = username.isNotBlank() && updateState !is UserUpdateState.Loading
//            ) {
//                Text(
//                    text = if (isEdit) "Update" else "Add",
//                    color = Color(0xFF8B5CF6)
//                )
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onDismiss,
//                enabled = updateState !is UserUpdateState.Loading
//            ) {
//                Text(
//                    text = "Cancel",
//                    color = Color(0xFF757575)
//                )
//            }
//        },
//        containerColor = Color.White
//    )
//}
//
//
//
//
//
//// Function to extract username from link
//fun extractUsernameFromLink(url: String): String {
//    return url
//        .removePrefix("https://www.instagram.com/")
//        .removePrefix("http://www.instagram.com/")
//        .removePrefix("https://instagram.com/")
//        .removePrefix("http://instagram.com/")
//        .removeSuffix("/")
//}











//@Composable
//fun InstagramAccountDialog(
//    isEdit: Boolean,
//    initialLink: String,
//    userViewModel: UserViewModel,
//    onDismiss: () -> Unit,
//    onConfirmSuccess: () -> Unit
//) {
//    var link by remember { mutableStateOf(initialLink) }
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // Handle successful update
//    LaunchedEffect(updateState) {
//        if (updateState is UserUpdateState.Success) {
//            onConfirmSuccess()
//        }
//    }
//
//    // Function to format Instagram link
//    fun formatInstagramLink(input: String): String {
//        val trimmedInput = input.trim()
//
//        // If already a full URL, return as is
//        if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")) {
//            return trimmedInput
//        }
//
//        // Remove common Instagram prefixes if user included them
//        val username = trimmedInput
//            .removePrefix("instagram.com/")
//            .removePrefix("www.instagram.com/")
//            .removePrefix("@")
//
//        // Add the full Instagram URL prefix
//        return "https://www.instagram.com/$username"
//    }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = if (isEdit) "Edit Instagram Account" else "Add Instagram Account",
//                fontWeight = FontWeight.SemiBold
//            )
//        },
//        text = {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Profile Link Field - ONLY INPUT FIELD
//                OutlinedTextField(
//                    value = link,
//                    onValueChange = { link = it },
//                    label = { Text("Profile Link") },
//                    placeholder = { Text("username or https://instagram.com/username") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color(0xFFE0E0E0),
//                        focusedBorderColor = Color(0xFF8B5CF6)
//                    ),
//                    shape = RoundedCornerShape(12.dp),
//                    enabled = updateState !is UserUpdateState.Loading
//                )
//
//                if (updateState is UserUpdateState.Loading) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = Color(0xFFB388FF),
//                            strokeWidth = 2.dp
//                        )
//                    }
//                }
//
//                if (updateState is UserUpdateState.Error) {
//                    Text(
//                        text = (updateState as UserUpdateState.Error).message,
//                        color = Color.Red,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    if (link.isNotBlank()) {
//                        val formattedLink = formatInstagramLink(link)
//                        userViewModel.updateUser(
//                            socialMediaLinks = SocialMediaLinks(
//                                instagram = listOf(
//                                    SocialMediaLink(link = formattedLink, is_primary = true)
//                                )
//                            )
//                        )
//                    }
//                },
//                enabled = link.isNotBlank() && updateState !is UserUpdateState.Loading
//            ) {
//                Text(
//                    text = if (isEdit) "Update" else "Add",
//                    color = Color(0xFF8B5CF6)
//                )
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onDismiss,
//                enabled = updateState !is UserUpdateState.Loading
//            ) {
//                Text(
//                    text = "Cancel",
//                    color = Color(0xFF757575)
//                )
//            }
//        },
//        containerColor = Color.White
//    )
//}
//















//@Composable
//fun InstagramAccountDialog(
//    isEdit: Boolean,
//    initialLink: String,
//    userViewModel: UserViewModel,
//    onDismiss: () -> Unit,
//    onConfirmSuccess: () -> Unit
//) {
//    var link by remember { mutableStateOf(initialLink) }
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // Handle successful update
//    LaunchedEffect(updateState) {
//        if (updateState is UserUpdateState.Success) {
//            onConfirmSuccess()
//        }
//    }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = if (isEdit) "Edit Instagram Account" else "Add Instagram Account",
//                fontWeight = FontWeight.SemiBold
//            )
//        },
//        text = {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Profile Link Field - ONLY INPUT FIELD
//                OutlinedTextField(
//                    value = link,
//                    onValueChange = { link = it },
//                    label = { Text("Profile Link") },
//                    placeholder = { Text("https://instagram.com/username") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color(0xFFE0E0E0),
//                        focusedBorderColor = Color(0xFF8B5CF6)
//                    ),
//                    shape = RoundedCornerShape(12.dp),
//                    enabled = updateState !is UserUpdateState.Loading
//                )
//
//                if (updateState is UserUpdateState.Loading) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = Color(0xFFB388FF),
//                            strokeWidth = 2.dp
//                        )
//                    }
//                }
//
//                if (updateState is UserUpdateState.Error) {
//                    Text(
//                        text = (updateState as UserUpdateState.Error).message,
//                        color = Color.Red,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    if (link.isNotBlank()) {
//                        userViewModel.updateUser(
//                            socialMediaLinks = SocialMediaLinks(
//                                instagram = listOf(
//                                    SocialMediaLink(link = link, is_primary = true)
//                                )
//                            )
//                        )
//                    }
//                },
//                enabled = link.isNotBlank() && updateState !is UserUpdateState.Loading
//            ) {
//                Text(
//                    text = if (isEdit) "Update" else "Add",
//                    color = Color(0xFF8B5CF6)
//                )
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onDismiss,
//                enabled = updateState !is UserUpdateState.Loading
//            ) {
//                Text(
//                    text = "Cancel",
//                    color = Color(0xFF757575)
//                )
//            }
//        },
//        containerColor = Color.White
//    )
//}










/*
data class InstagramAccount(
    val username: String,
    val link: String,
    val followers: Int,
    val isPrimary: Boolean
)

//@Composable
//fun AccountsScreen(
//    navController: NavController,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    val userViewModel = remember { UserViewModel(context) }
//    val firebaseAnalytics = remember { Firebase.analytics }
//
//    // Collect user profile from UserViewModel
//    val userProfile by userViewModel.userProfile.collectAsState()
//    val isLoading by userViewModel.isLoading.collectAsState()
//    val error by userViewModel.error.collectAsState()
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // State for Instagram accounts
//    var instagramAccounts by remember { mutableStateOf<List<InstagramAccount>>(emptyList()) }
//    var showAddDialog by remember { mutableStateOf(false) }
//    var showEditDialog by remember { mutableStateOf(false) }
//    var selectedAccountIndex by remember { mutableStateOf(-1) }
//
//    // Fetch user profile when screen loads
//    LaunchedEffect(Unit) {
//        userViewModel.fetchUserProfile()
//    }
//
//    // Load Instagram accounts from profile
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            val accounts = mutableListOf<InstagramAccount>()
//
//            try {
//                // Extract Instagram data from social_media_links
//                val instagramLinks = profile.social_media_links?.get("instagram") as? List<*>
//
//                instagramLinks?.forEachIndexed { index, item ->
//                    val instagramMap = item as? Map<*, *>
//                    val username = (instagramMap?.get("username") as? String) ?: ""
//                    val link = (instagramMap?.get("link") as? String) ?: ""
//                    val isPrimary = (instagramMap?.get("is_primary") as? Boolean) ?: false
//
//                    // Get followers from platform_followers
//                    val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
//                    val accountFollowers = platformFollowers?.getOrNull(index) as? Map<*, *>
//                    val followers = (accountFollowers?.get("followers") as? Number)?.toInt() ?: 0
//
//                    if (username.isNotBlank() || link.isNotBlank()) {
//                        accounts.add(
//                            InstagramAccount(
//                                username = username,
//                                link = link,
//                                followers = followers,
//                                isPrimary = isPrimary
//                            )
//                        )
//                    }
//                }
//
//                Log.d("AccountsScreen", "Loaded ${accounts.size} Instagram accounts")
//            } catch (e: Exception) {
//                Log.e("AccountsScreen", "Error parsing Instagram data", e)
//            }
//
//            instagramAccounts = accounts
//        }
//    }
//
//    // Handle update state changes
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UserUpdateState.Success -> {
//                Toast.makeText(context, "Instagram account updated successfully!", Toast.LENGTH_SHORT).show()
//                userViewModel.resetUpdateState()
//                userViewModel.fetchUserProfile()
//            }
//            is UserUpdateState.Error -> {
//                val errorMessage = (updateState as UserUpdateState.Error).message
//                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
//                userViewModel.resetUpdateState()
//            }
//            else -> {}
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFAF8F8))
//    ) {
//        when {
//            isLoading -> {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                    color = Color(0xFFB388FF)
//                )
//            }
//
//            error != null -> {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = error ?: "An error occurred",
//                        color = Color.Red,
//                        fontSize = 16.sp
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = { userViewModel.fetchUserProfile() },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFB388FF)
//                        )
//                    ) {
//                        Text("Retry")
//                    }
//                }
//            }
//
//            else -> {
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    // Top Bar
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_left_arrow),
//                            contentDescription = "Back",
//                            modifier = Modifier
//                                .size(26.dp)
//                                .clickable { onDismiss() },
//                            tint = Color.Black
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text(
//                            text = "Personal Information",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                    }
//
//                    // Scrollable Content
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .verticalScroll(rememberScrollState())
//                            .padding(horizontal = 24.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Instagram Accounts Section Header
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.ic_instagram),
//                                    contentDescription = "Instagram",
//                                    tint = Color(0xFFE1306C),
//                                    modifier = Modifier.size(28.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Instagram Account",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black
//                                )
//                            }
//                            Text(
//                                text = "${instagramAccounts.size} linked",
//                                fontSize = 14.sp,
//                                color = Color(0xFF757575)
//                            )
//                        }
//
//                        // Display Instagram Accounts
//                        if (instagramAccounts.isEmpty()) {
//                            // Empty state
//                            Surface(
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(16.dp),
//                                color = Color.White,
//                                shadowElevation = 2.dp
//                            ) {
//                                Column(
//                                    modifier = Modifier.padding(24.dp),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.ic_instagram),
//                                        contentDescription = "No accounts",
//                                        tint = Color(0xFFE0E0E0),
//                                        modifier = Modifier.size(48.dp)
//                                    )
//                                    Spacer(modifier = Modifier.height(12.dp))
//                                    Text(
//                                        text = "No Instagram accounts linked",
//                                        fontSize = 14.sp,
//                                        color = Color(0xFF757575)
//                                    )
//                                    Spacer(modifier = Modifier.height(16.dp))
//                                    TextButton(
//                                        onClick = { showAddDialog = true }
//                                    ) {
//                                        Text(
//                                            text = "+ Add Account",
//                                            color = Color(0xFF8B5CF6),
//                                            fontWeight = FontWeight.Medium
//                                        )
//                                    }
//                                }
//                            }
//                        } else {
//                            instagramAccounts.forEachIndexed { index, account ->
//                                InstagramAccountCard(
//                                    account = account,
//                                    onEdit = {
//                                        selectedAccountIndex = index
//                                        showEditDialog = true
//                                    },
//                                    onDelete = {
//                                        instagramAccounts = instagramAccounts.filterIndexed { i, _ -> i != index }
//                                    },
//                                    onSetPrimary = {
//                                        instagramAccounts = instagramAccounts.mapIndexed { i, acc ->
//                                            acc.copy(isPrimary = i == index)
//                                        }
//                                    }
//                                )
//                            }
//
//                            // Add more button
//                            TextButton(
//                                onClick = { showAddDialog = true },
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text(
//                                    text = "+ Add Another Account",
//                                    color = Color(0xFF8B5CF6),
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // Save Button
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 12.dp, vertical = 8.dp)
//                    ) {
//                        if (updateState is UserUpdateState.Loading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier
//                                    .align(Alignment.Center)
//                                    .size(40.dp),
//                                color = Color(0xFFB388FF)
//                            )
//                        } else {
//                            GradientButton(
//                                text = "Save",
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                // Log Firebase Analytics event
//                                val bundle = Bundle().apply {
//                                    putString("screen_name", "instagram_accounts")
//                                    putInt("accounts_count", instagramAccounts.size)
//                                }
//                                firebaseAnalytics.logEvent("instagram_accounts_save", bundle)
//
//                                if (instagramAccounts.isNotEmpty()) {
//                                    // Prepare Instagram links and followers data
//                                    val primaryAccount = instagramAccounts.firstOrNull { it.isPrimary }
//                                        ?: instagramAccounts.firstOrNull()
//
//                                    primaryAccount?.let { account ->
//                                        userViewModel.updateSocialMediaData(
//                                            instagramFollowers = account.followers.toString(),
//                                            instagramLink = account.link
//                                        )
//
//                                        Log.d("AccountsScreen", "Saving Instagram data:")
//                                        Log.d("AccountsScreen", "Username: ${account.username}")
//                                        Log.d("AccountsScreen", "Link: ${account.link}")
//                                        Log.d("AccountsScreen", "Followers: ${account.followers}")
//                                    }
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        "Please add at least one Instagram account",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Add/Edit Dialog
//        if (showAddDialog || showEditDialog) {
//            InstagramAccountDialog(
//                isEdit = showEditDialog,
//                initialUsername = if (showEditDialog && selectedAccountIndex >= 0) {
//                    instagramAccounts[selectedAccountIndex].username
//                } else "",
//                initialLink = if (showEditDialog && selectedAccountIndex >= 0) {
//                    instagramAccounts[selectedAccountIndex].link
//                } else "",
//                initialFollowers = if (showEditDialog && selectedAccountIndex >= 0) {
//                    instagramAccounts[selectedAccountIndex].followers
//                } else 0,
//                onDismiss = {
//                    showAddDialog = false
//                    showEditDialog = false
//                    selectedAccountIndex = -1
//                },
//                onConfirm = { username, link, followers ->
//                    if (showEditDialog && selectedAccountIndex >= 0) {
//                        // Update existing account
//                        instagramAccounts = instagramAccounts.mapIndexed { index, account ->
//                            if (index == selectedAccountIndex) {
//                                account.copy(
//                                    username = username,
//                                    link = link,
//                                    followers = followers
//                                )
//                            } else account
//                        }
//                    } else {
//                        // Add new account
//                        val isPrimary = instagramAccounts.isEmpty()
//                        instagramAccounts = instagramAccounts + InstagramAccount(
//                            username = username,
//                            link = link,
//                            followers = followers,
//                            isPrimary = isPrimary
//                        )
//                    }
//                    showAddDialog = false
//                    showEditDialog = false
//                    selectedAccountIndex = -1
//                }
//            )
//        }
//    }
//}


@Composable
fun AccountsScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(context) }
    val firebaseAnalytics = remember { Firebase.analytics }

    // Collect user profile from UserViewModel
    val userProfile by userViewModel.userProfile.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    val updateState by userViewModel.updateState.collectAsState()

    // State for Instagram accounts
    var instagramAccounts by remember { mutableStateOf<List<InstagramAccount>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedAccountIndex by remember { mutableStateOf(-1) }

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
    }

    // Load Instagram accounts from profile
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val accounts = mutableListOf<InstagramAccount>()

            try {
                // Extract Instagram data from social_media_links
                val instagramLinks = profile.social_media_links?.get("instagram") as? List<*>

                instagramLinks?.forEachIndexed { index, item ->
                    val instagramMap = item as? Map<*, *>
                    val username = (instagramMap?.get("username") as? String) ?: ""
                    val link = (instagramMap?.get("link") as? String) ?: ""
                    val isPrimary = (instagramMap?.get("is_primary") as? Boolean) ?: false

                    // Get followers from platform_followers
                    val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
                    val accountFollowers = platformFollowers?.getOrNull(index) as? Map<*, *>
                    val followers = (accountFollowers?.get("followers") as? Number)?.toInt() ?: 0

                    if (username.isNotBlank() || link.isNotBlank()) {
                        accounts.add(
                            InstagramAccount(
                                username = username,
                                link = link,
                                followers = followers,
                                isPrimary = isPrimary
                            )
                        )
                    }
                }

                Log.d("AccountsScreen", "Loaded ${accounts.size} Instagram accounts")
            } catch (e: Exception) {
                Log.e("AccountsScreen", "Error parsing Instagram data", e)
            }

            instagramAccounts = accounts
        }
    }

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Instagram account updated successfully!", Toast.LENGTH_SHORT).show()
                userViewModel.resetUpdateState()
                userViewModel.fetchUserProfile()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                userViewModel.resetUpdateState()
            }
            else -> {}
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
                    Button(
                        onClick = { userViewModel.fetchUserProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB388FF)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
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
                            text = "Personal Information",
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Instagram Accounts Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    tint = Color(0xFFE1306C),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Instagram Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = "${instagramAccounts.size} linked",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        // Display Instagram Accounts
                        if (instagramAccounts.isEmpty()) {
                            // Empty state
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_instagram),
                                        contentDescription = "No accounts",
                                        tint = Color(0xFFE0E0E0),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Instagram accounts linked",
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(
                                        onClick = { showAddDialog = true }
                                    ) {
                                        Text(
                                            text = "+ Add Account",
                                            color = Color(0xFF8B5CF6),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            instagramAccounts.forEachIndexed { index, account ->
                                InstagramAccountCard(
                                    account = account,
                                    onEdit = {
                                        selectedAccountIndex = index
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        instagramAccounts = instagramAccounts.filterIndexed { i, _ -> i != index }
                                    },
                                    onSetPrimary = {
                                        instagramAccounts = instagramAccounts.mapIndexed { i, acc ->
                                            acc.copy(isPrimary = i == index)
                                        }
                                    }
                                )
                            }

                            // Add more button
                            TextButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "+ Add Another Account",
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Save Button
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
                                // Log Firebase Analytics event
                                val bundle = Bundle().apply {
                                    putString("screen_name", "instagram_accounts")
                                    putInt("accounts_count", instagramAccounts.size)
                                }
                                firebaseAnalytics.logEvent("instagram_accounts_save", bundle)

                                if (instagramAccounts.isNotEmpty()) {
                                    // Prepare Instagram links (followers will be fetched from API)
                                    val primaryAccount = instagramAccounts.firstOrNull { it.isPrimary }
                                        ?: instagramAccounts.firstOrNull()

                                    primaryAccount?.let { account ->
                                        userViewModel.updateSocialMediaLinks(
                                            instagramLink = account.link
                                        )

                                        Log.d("AccountsScreen", "Saving Instagram link:")
                                        Log.d("AccountsScreen", "Link: ${account.link}")
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please add at least one Instagram account",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog || showEditDialog) {
            InstagramAccountDialog(
                isEdit = showEditDialog,
                initialUsername = if (showEditDialog && selectedAccountIndex >= 0) {
                    instagramAccounts[selectedAccountIndex].username
                } else "",
                initialLink = if (showEditDialog && selectedAccountIndex >= 0) {
                    instagramAccounts[selectedAccountIndex].link
                } else "",
                initialFollowers = if (showEditDialog && selectedAccountIndex >= 0) {
                    instagramAccounts[selectedAccountIndex].followers
                } else 0,
                onDismiss = {
                    showAddDialog = false
                    showEditDialog = false
                    selectedAccountIndex = -1
                },
                onConfirm = { username, link, followers ->
                    if (showEditDialog && selectedAccountIndex >= 0) {
                        // Update existing account
                        instagramAccounts = instagramAccounts.mapIndexed { index, account ->
                            if (index == selectedAccountIndex) {
                                account.copy(
                                    username = username,
                                    link = link,
                                    followers = followers
                                )
                            } else account
                        }
                    } else {
                        // Add new account
                        val isPrimary = instagramAccounts.isEmpty()
                        instagramAccounts = instagramAccounts + InstagramAccount(
                            username = username,
                            link = link,
                            followers = followers,
                            isPrimary = isPrimary
                        )
                    }
                    showAddDialog = false
                    showEditDialog = false
                    selectedAccountIndex = -1
                }
            )
        }
    }
}
@Composable
fun InstagramAccountCard(
    account: InstagramAccount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${account.username}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${account.followers} followers",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    if (account.link.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = account.link,
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1
                        )
                    }
                }

                if (account.isPrimary) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Primary",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                // Delete Button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun InstagramAccountDialog(
    isEdit: Boolean,
    initialUsername: String,
    initialLink: String,
    initialFollowers: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var username by remember { mutableStateOf(initialUsername) }
    var link by remember { mutableStateOf(initialLink) }
    var followers by remember { mutableStateOf(initialFollowers.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Instagram Account" else "Add Instagram Account",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("indrajeet.kumar12") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Link Field
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Profile Link") },
                    placeholder = { Text("https://instagram.com/username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Followers Field
                OutlinedTextField(
                    value = followers,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            followers = newValue
                        }
                    },
                    label = { Text("Followers") },
                    placeholder = { Text("183") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val followersInt = followers.toIntOrNull() ?: 0
                    if (username.isNotBlank() && link.isNotBlank()) {
                        onConfirm(username, link, followersInt)
                    }
                },
                enabled = username.isNotBlank() && link.isNotBlank()
            ) {
                Text(
                    text = if (isEdit) "Update" else "Add",
                    color = Color(0xFF8B5CF6)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF757575)
                )
            }
        },
        containerColor = Color.White
    )
}

*/