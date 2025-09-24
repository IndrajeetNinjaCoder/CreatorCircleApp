package com.cc.creatorcircle.ui.screens.home

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.boundsInWindow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Video player manager to control which video should play
object VideoPlayerManager {
    private var currentPlayingVideoId: String? = null
    private val videoControllers = mutableMapOf<String, VideoController>()

    data class VideoController(
        val exoPlayer: ExoPlayer,
        val onPause: () -> Unit,
        val onPlay: () -> Unit,
        var isVisible: Boolean = false,
        var wasPlayingBeforeInvisible: Boolean = false
    )

    fun registerVideoPlayer(videoId: String, controller: VideoController) {
        videoControllers[videoId] = controller
    }

    fun unregisterVideoPlayer(videoId: String) {
        videoControllers.remove(videoId)
        if (currentPlayingVideoId == videoId) {
            currentPlayingVideoId = null
        }
    }

    fun updateVisibility(videoId: String, isVisible: Boolean, autoPlay: Boolean = true) {
        val controller = videoControllers[videoId]
        if (controller != null) {
            val wasVisible = controller.isVisible
            videoControllers[videoId] = controller.copy(isVisible = isVisible)

            when {
                isVisible && !wasVisible && autoPlay -> {
                    // Video became visible - auto play it
                    playVideo(videoId)
                }
                !isVisible && wasVisible -> {
                    // Video became invisible - pause it if it was playing
                    if (currentPlayingVideoId == videoId) {
                        controller.wasPlayingBeforeInvisible = true
                        pauseVideo(videoId)
                    }
                }
            }
        }
    }

    fun playVideo(videoId: String, userInitiated: Boolean = false) {
        // Only play if the video is visible (unless user explicitly clicked)
        val controller = videoControllers[videoId]
        if (!userInitiated && controller?.isVisible != true) return

        // Pause currently playing video if different
        if (currentPlayingVideoId != videoId) {
            currentPlayingVideoId?.let { currentId ->
                videoControllers[currentId]?.onPause?.invoke()
            }
            currentPlayingVideoId = videoId
        }

        // Play the requested video
        videoControllers[videoId]?.onPlay?.invoke()
    }

    fun pauseVideo(videoId: String) {
        if (currentPlayingVideoId == videoId) {
            videoControllers[videoId]?.onPause?.invoke()
            currentPlayingVideoId = null
        }
    }

    fun pauseAllVideos() {
        videoControllers.values.forEach { it.onPause() }
        currentPlayingVideoId = null
    }

    fun isCurrentlyPlaying(videoId: String): Boolean {
        return currentPlayingVideoId == videoId
    }

    fun getVisibleVideos(): List<String> {
        return videoControllers.filter { it.value.isVisible }.keys.toList()
    }
}

// Composable to check if a composable is visible on screen
@Composable
fun Modifier.onVisibilityChanged(
    onVisibilityChanged: (Boolean) -> Unit
): Modifier {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp

    return this.onGloballyPositioned { coordinates ->
        val bounds = coordinates.boundsInWindow()
        val itemTop = bounds.top
        val itemBottom = bounds.bottom
        val screenHeightPx = with(density) { screenHeight.dp.toPx() }

        // Consider item visible if at least 50% of it is visible on screen
        val visibleHeight = when {
            itemTop < 0 && itemBottom > 0 -> itemBottom
            itemTop >= 0 && itemBottom <= screenHeightPx -> itemBottom - itemTop
            itemTop < screenHeightPx && itemBottom > screenHeightPx -> screenHeightPx - itemTop
            else -> 0f
        }

        val itemHeight = itemBottom - itemTop
        val visibilityPercentage = if (itemHeight > 0) visibleHeight / itemHeight else 0f

        val isVisible = visibilityPercentage >= 0.5f && itemTop < screenHeightPx && itemBottom > 0
        onVisibilityChanged(isVisible)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    isMuted: Boolean = false,
    onMuteToggle: (Boolean) -> Unit = {},
    onPlayPause: (Boolean) -> Unit = {},
    onVideoClick: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    enableGestures: Boolean = true,
    showMuteButton: Boolean = true,
    loopVideo: Boolean = true,
    videoId: String = videoUrl // Unique identifier for this video
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var buffering by remember { mutableStateOf(false) }
    var videoEnded by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(isMuted) }
    var isVisible by remember { mutableStateOf(false) }

    // Control visibility timer
    var controlsJob: Job? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()

    // Create ExoPlayer with production configurations
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        15000, // min buffer
                        50000, // max buffer
                        1500,  // buffer for playback
                        5000   // buffer for playback after rebuffer
                    )
                    .build()
            )
            .build()
            .apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(videoUrl)
                    .build()

                setMediaItem(mediaItem)
                repeatMode = if (loopVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                volume = if (muted) 0f else 1f

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> buffering = true
                            Player.STATE_READY -> {
                                buffering = false
                                duration = this@apply.duration.coerceAtLeast(0L)
                            }
                            Player.STATE_ENDED -> {
                                videoEnded = true
                                isPlaying = false
                                onPlayPause(false)
                            }
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                        onPlayPause(playing)
                    }
                })

                prepare()
            }
    }

    // Register this video player with the manager
    LaunchedEffect(videoId) {
        val controller = VideoPlayerManager.VideoController(
            exoPlayer = exoPlayer,
            onPause = {
                exoPlayer.pause()
                isPlaying = false
            },
            onPlay = {
                if (videoEnded) {
                    exoPlayer.seekTo(0)
                    videoEnded = false
                }
                exoPlayer.play()
                isPlaying = true
            },
            isVisible = isVisible
        )

        VideoPlayerManager.registerVideoPlayer(videoId, controller)
    }

    // Handle visibility changes
    LaunchedEffect(isVisible) {
        VideoPlayerManager.updateVisibility(videoId, isVisible, autoPlay)
    }

    // Position update effect
    LaunchedEffect(isPlaying) {
        while (isPlaying && !videoEnded) {
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            delay(100)
        }
    }

    // Mute control effect
    LaunchedEffect(muted) {
        exoPlayer.volume = if (muted) 0f else 1f
        onMuteToggle(muted)
    }

    // Auto-hide controls
    fun scheduleControlsHide() {
        controlsJob?.cancel()
        controlsJob = coroutineScope.launch {
            delay(3000)
            showControls = false
        }
    }

    // Show controls temporarily
    fun showControlsTemporarily() {
        showControls = true
        scheduleControlsHide()
    }

    // Function to toggle play/pause
    fun togglePlayPause() {
        if (isPlaying) {
            VideoPlayerManager.pauseVideo(videoId)
        } else {
            VideoPlayerManager.playVideo(videoId, userInitiated = true)
        }
        // Show controls briefly when toggling
        showControlsTemporarily()
    }

    // Cleanup
    DisposableEffect(videoUrl) {
        onDispose {
            controlsJob?.cancel()
            VideoPlayerManager.unregisterVideoPlayer(videoId)
            exoPlayer.release()
        }
    }

    // Lifecycle management
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    VideoPlayerManager.pauseAllVideos()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Resume auto-play for visible videos
                    if (autoPlay && isVisible && !videoEnded) {
                        VideoPlayerManager.playVideo(videoId)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onVisibilityChanged { visible ->
                isVisible = visible
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Single tap: toggle play/pause and show/hide controls
                        if (enableGestures) {
                            onVideoClick()
                            togglePlayPause()
                        }
                    },
                    onDoubleTap = {
                        // Double tap: custom action
                        if (enableGestures) {
                            onDoubleTap()
                        }
                    }
                )
            }
    ) {
        // Video Player Surface
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (buffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }

        // Play/Pause overlay (appears on video end or when paused)
        if ((videoEnded || (!isPlaying && showControls)) && !buffering) {
            IconButton(
                onClick = {
                    if (videoEnded) {
                        VideoPlayerManager.playVideo(videoId, userInitiated = true)
                        videoEnded = false
                    } else {
                        togglePlayPause()
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (videoEnded) Icons.Default.PlayArrow else
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (videoEnded) "Replay" else
                        if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Controls overlay
        AnimatedVisibility(
            visible = showControls && !buffering,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            ) {
                // Top controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (showMuteButton) {
                        IconButton(
                            onClick = { muted = !muted },
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (muted) "Unmute" else "Mute",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Bottom controls (progress bar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    // Progress bar
                    if (duration > 0) {
                        val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Time indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = formatTime(duration),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// Utility function to format time
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}




























//package com.cc.creatorcircle.ui.screens.home
//
//import android.view.GestureDetector
//import android.view.MotionEvent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.VolumeOff
//import androidx.compose.material.icons.filled.VolumeUp
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableLongStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.media3.common.MediaItem
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.DefaultLoadControl
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.AspectRatioFrameLayout
//import androidx.media3.ui.PlayerView
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//// Video player manager to control which video should play
//object VideoPlayerManager {
//    private var currentPlayingVideoId: String? = null
//    private val videoControllers = mutableMapOf<String, VideoController>()
//
//    data class VideoController(
//        val exoPlayer: ExoPlayer,
//        val onPause: () -> Unit,
//        val onPlay: () -> Unit,
//        val isVisible: Boolean = false
//    )
//
//    fun registerVideoPlayer(videoId: String, controller: VideoController) {
//        videoControllers[videoId] = controller
//    }
//
//    fun unregisterVideoPlayer(videoId: String) {
//        videoControllers.remove(videoId)
//    }
//
//    fun updateVisibility(videoId: String, isVisible: Boolean) {
//        val controller = videoControllers[videoId]
//        if (controller != null) {
//            videoControllers[videoId] = controller.copy(isVisible = isVisible)
//
//            if (isVisible) {
//                // If video becomes visible, play it (this will pause others)
//                playVideo(videoId)
//            } else if (currentPlayingVideoId == videoId) {
//                // If currently playing video becomes invisible, pause it
//                pauseVideo(videoId)
//            }
//        }
//    }
//
//    fun playVideo(videoId: String) {
//        // Only play if the video is visible
//        val controller = videoControllers[videoId]
//        if (controller?.isVisible != true) return
//
//        // Pause currently playing video if different
//        if (currentPlayingVideoId != videoId) {
//            currentPlayingVideoId?.let { currentId ->
//                videoControllers[currentId]?.onPause?.invoke()
//            }
//            currentPlayingVideoId = videoId
//        }
//
//        // Play the requested video
//        videoControllers[videoId]?.onPlay?.invoke()
//    }
//
//    fun pauseVideo(videoId: String) {
//        if (currentPlayingVideoId == videoId) {
//            videoControllers[videoId]?.onPause?.invoke()
//            currentPlayingVideoId = null
//        }
//    }
//
//    fun pauseAllVideos() {
//        videoControllers.values.forEach { it.onPause() }
//        currentPlayingVideoId = null
//    }
//
//    fun isCurrentlyPlaying(videoId: String): Boolean {
//        return currentPlayingVideoId == videoId
//    }
//}
//
//// Composable to track visibility of a composable within the screen
//@Composable
//fun rememberVisibilityState(): Pair<Boolean, (Boolean) -> Unit> {
//    var isVisible by remember { mutableStateOf(false) }
//    return Pair(isVisible) { visibility -> isVisible = visibility }
//}
//
//// Extension function to track visibility
//@Composable
//fun Modifier.onVisibilityChanged(
//    onVisibilityChanged: (Boolean) -> Unit
//): Modifier {
//    return this.pointerInput(Unit) {
//        // This is a simplified approach - in production you might want to use
//        // a more sophisticated visibility detection using view bounds
//    }
//}
//
//@androidx.annotation.OptIn(UnstableApi::class)
//@Composable
//fun VideoPlayer(
//    videoUrl: String,
//    modifier: Modifier = Modifier,
//    autoPlay: Boolean = true,
//    isMuted: Boolean = false,
//    onMuteToggle: (Boolean) -> Unit = {},
//    onPlayPause: (Boolean) -> Unit = {},
//    onVideoClick: () -> Unit = {},
//    onDoubleTap: () -> Unit = {},
//    enableGestures: Boolean = true,
//    showMuteButton: Boolean = true,
//    loopVideo: Boolean = true,
//    videoId: String = videoUrl, // Unique identifier for this video
//    isVisible: Boolean = true // New parameter to track visibility
//) {
//    val context = LocalContext.current
//    var isPlaying by remember { mutableStateOf(false) }
//    var showControls by remember { mutableStateOf(false) }
//    var currentPosition by remember { mutableLongStateOf(0L) }
//    var duration by remember { mutableLongStateOf(0L) }
//    var buffering by remember { mutableStateOf(false) }
//    var videoEnded by remember { mutableStateOf(false) }
//    var muted by remember { mutableStateOf(isMuted) }
//    var videoSize by remember { mutableStateOf(IntSize.Zero) }
//
//    // Control visibility timer
//    var controlsJob: Job? by remember { mutableStateOf(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Create ExoPlayer with production configurations
//    val exoPlayer = remember(videoUrl) {
//        ExoPlayer.Builder(context)
//            .setLoadControl(
//                DefaultLoadControl.Builder()
//                    .setBufferDurationsMs(
//                        15000, // min buffer
//                        50000, // max buffer
//                        1500,  // buffer for playback
//                        5000   // buffer for playback after rebuffer
//                    )
//                    .build()
//            )
//            .build()
//            .apply {
//                val mediaItem = MediaItem.Builder()
//                    .setUri(videoUrl)
//                    .build()
//
//                setMediaItem(mediaItem)
//                repeatMode = if (loopVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
//                volume = if (muted) 0f else 1f
//
//                addListener(object : Player.Listener {
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        when (playbackState) {
//                            Player.STATE_BUFFERING -> buffering = true
//                            Player.STATE_READY -> {
//                                buffering = false
//                                duration = this@apply.duration.coerceAtLeast(0L)
//                            }
//                            Player.STATE_ENDED -> {
//                                videoEnded = true
//                                isPlaying = false
//                                onPlayPause(false)
//                            }
//                        }
//                    }
//
//                    override fun onIsPlayingChanged(playing: Boolean) {
//                        isPlaying = playing
//                        onPlayPause(playing)
//                    }
//                })
//
//                prepare()
//            }
//    }
//
//    // Register this video player with the manager
//    LaunchedEffect(videoId) {
//        val controller = VideoPlayerManager.VideoController(
//            exoPlayer = exoPlayer,
//            onPause = {
//                exoPlayer.pause()
//                isPlaying = false
//            },
//            onPlay = {
//                if (videoEnded) {
//                    exoPlayer.seekTo(0)
//                    videoEnded = false
//                }
//                exoPlayer.play()
//                isPlaying = true
//            },
//            isVisible = isVisible
//        )
//
//        VideoPlayerManager.registerVideoPlayer(videoId, controller)
//    }
//
//    // Handle visibility changes
//    LaunchedEffect(isVisible) {
//        VideoPlayerManager.updateVisibility(videoId, isVisible)
//    }
//
//    // Position update effect
//    LaunchedEffect(isPlaying) {
//        while (isPlaying && !videoEnded) {
//            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
//            delay(100)
//        }
//    }
//
//    // Mute control effect
//    LaunchedEffect(muted) {
//        exoPlayer.volume = if (muted) 0f else 1f
//        onMuteToggle(muted)
//    }
//
//    // Auto-hide controls
//    fun scheduleControlsHide() {
//        controlsJob?.cancel()
//        controlsJob = coroutineScope.launch {
//            delay(3000)
//            showControls = false
//        }
//    }
//
//    // Show controls temporarily
//    fun showControlsTemporarily() {
//        showControls = true
//        scheduleControlsHide()
//    }
//
//    // Function to toggle play/pause
//    fun togglePlayPause() {
//        if (!isVisible) return // Don't allow play/pause if not visible
//
//        if (isPlaying) {
//            VideoPlayerManager.pauseVideo(videoId)
//        } else {
//            VideoPlayerManager.playVideo(videoId)
//        }
//        // Show controls briefly when toggling
//        showControlsTemporarily()
//    }
//
//    // Cleanup
//    DisposableEffect(videoUrl) {
//        onDispose {
//            controlsJob?.cancel()
//            VideoPlayerManager.unregisterVideoPlayer(videoId)
//            exoPlayer.release()
//        }
//    }
//
//    // Lifecycle management
//    val lifecycleOwner = LocalLifecycleOwner.current
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_PAUSE -> {
//                    VideoPlayerManager.pauseAllVideos()
//                }
//                Lifecycle.Event.ON_RESUME -> {
//                    if (autoPlay && !videoEnded) {
//                        // Only auto-resume if this was the last playing video
//                        if (VideoPlayerManager.isCurrentlyPlaying(videoId)) {
//                            VideoPlayerManager.playVideo(videoId)
//                        }
//                    }
//                }
//                else -> {}
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.Black)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onTap = {
//                        // Single tap: toggle play/pause and show/hide controls
//                        if (enableGestures) {
//                            onVideoClick()
//                            togglePlayPause()
//                        }
//                    },
//                    onDoubleTap = {
//                        // Double tap: custom action
//                        if (enableGestures) {
//                            onDoubleTap()
//                        }
//                    }
//                )
//            }
//    ) {
//        // Video Player Surface
//        AndroidView(
//            factory = { context ->
//                PlayerView(context).apply {
//                    player = exoPlayer
//                    useController = false
//                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        // Loading indicator
//        if (buffering) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(48.dp),
//                color = Color.White,
//                strokeWidth = 3.dp
//            )
//        }
//
//        // Play/Pause overlay (appears on video end or when paused)
//        if ((videoEnded || (!isPlaying && showControls)) && !buffering) {
//            IconButton(
//                onClick = {
//                    if (videoEnded) {
//                        VideoPlayerManager.playVideo(videoId)
//                        videoEnded = false
//                    } else {
//                        togglePlayPause()
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(80.dp)
//                    .background(
//                        Color.Black.copy(alpha = 0.6f),
//                        CircleShape
//                    )
//            ) {
//                Icon(
//                    imageVector = if (videoEnded) Icons.Default.PlayArrow else
//                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
//                    contentDescription = if (videoEnded) "Replay" else
//                        if (isPlaying) "Pause" else "Play",
//                    tint = Color.White,
//                    modifier = Modifier.size(40.dp)
//                )
//            }
//        }
//
//        // Controls overlay
//        AnimatedVisibility(
//            visible = showControls && !buffering,
//            enter = fadeIn(animationSpec = tween(200)),
//            exit = fadeOut(animationSpec = tween(200)),
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        Brush.verticalGradient(
//                            colors = listOf(
//                                Color.Transparent,
//                                Color.Black.copy(alpha = 0.3f)
//                            ),
//                            startY = 0f,
//                            endY = Float.POSITIVE_INFINITY
//                        )
//                    )
//            ) {
//                // Top controls
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .align(Alignment.TopEnd),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    if (showMuteButton) {
//                        IconButton(
//                            onClick = { muted = !muted },
//                            modifier = Modifier
//                                .background(
//                                    Color.Black.copy(alpha = 0.5f),
//                                    CircleShape
//                                )
//                        ) {
//                            Icon(
//                                imageVector = if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
//                                contentDescription = if (muted) "Unmute" else "Mute",
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                // Bottom controls (progress bar)
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                        .padding(horizontal = 16.dp, vertical = 24.dp)
//                ) {
//                    // Progress bar
//                    if (duration > 0) {
//                        val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
//
//                        LinearProgressIndicator(
//                            progress = { progress },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(2.dp),
//                            color = Color.White,
//                            trackColor = Color.White.copy(alpha = 0.3f)
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Time indicators
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = formatTime(currentPosition),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                            Text(
//                                text = formatTime(duration),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Utility function to format time
//private fun formatTime(timeMs: Long): String {
//    val totalSeconds = timeMs / 1000
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return String.format("%d:%02d", minutes, seconds)
//}
























































//
//package com.cc.creatorcircle.ui.screens.home
//
//import android.view.GestureDetector
//import android.view.MotionEvent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.VolumeOff
//import androidx.compose.material.icons.filled.VolumeUp
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableLongStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.media3.common.MediaItem
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.DefaultLoadControl
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.AspectRatioFrameLayout
//import androidx.media3.ui.PlayerView
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//// Video player manager to control which video should play
//object VideoPlayerManager {
//    private var currentPlayingVideoId: String? = null
//    private val videoControllers = mutableMapOf<String, VideoController>()
//
//    data class VideoController(
//        val exoPlayer: ExoPlayer,
//        val onPause: () -> Unit,
//        val onPlay: () -> Unit
//    )
//
//    fun registerVideoPlayer(videoId: String, controller: VideoController) {
//        videoControllers[videoId] = controller
//    }
//
//    fun unregisterVideoPlayer(videoId: String) {
//        videoControllers.remove(videoId)
//    }
//
//    fun playVideo(videoId: String) {
//        // Pause currently playing video if different
//        if (currentPlayingVideoId != videoId) {
//            currentPlayingVideoId?.let { currentId ->
//                videoControllers[currentId]?.onPause?.invoke()
//            }
//            currentPlayingVideoId = videoId
//        }
//
//        // Play the requested video
//        videoControllers[videoId]?.onPlay?.invoke()
//    }
//
//    fun pauseVideo(videoId: String) {
//        if (currentPlayingVideoId == videoId) {
//            videoControllers[videoId]?.onPause?.invoke()
//            currentPlayingVideoId = null
//        }
//    }
//
//    fun pauseAllVideos() {
//        videoControllers.values.forEach { it.onPause() }
//        currentPlayingVideoId = null
//    }
//
//    fun isCurrentlyPlaying(videoId: String): Boolean {
//        return currentPlayingVideoId == videoId
//    }
//}
//
//@androidx.annotation.OptIn(UnstableApi::class)
//@Composable
//fun VideoPlayer(
//    videoUrl: String,
//    modifier: Modifier = Modifier,
//    autoPlay: Boolean = true,
//    isMuted: Boolean = false,
//    onMuteToggle: (Boolean) -> Unit = {},
//    onPlayPause: (Boolean) -> Unit = {},
//    onVideoClick: () -> Unit = {},
//    onDoubleTap: () -> Unit = {},
//    enableGestures: Boolean = true,
//    showMuteButton: Boolean = true,
//    loopVideo: Boolean = true,
//    isVisible: Boolean = false, // New parameter to track visibility
//    videoId: String = videoUrl // Unique identifier for this video
//) {
//    val context = LocalContext.current
//    var isPlaying by remember { mutableStateOf(false) }
//    var showControls by remember { mutableStateOf(false) }
//    var currentPosition by remember { mutableLongStateOf(0L) }
//    var duration by remember { mutableLongStateOf(0L) }
//    var buffering by remember { mutableStateOf(false) }
//    var videoEnded by remember { mutableStateOf(false) }
//    var muted by remember { mutableStateOf(isMuted) }
//    var videoSize by remember { mutableStateOf(IntSize.Zero) }
//
//    // Control visibility timer
//    var controlsJob: Job? by remember { mutableStateOf(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Create ExoPlayer with production configurations
//    val exoPlayer = remember(videoUrl) {
//        ExoPlayer.Builder(context)
//            .setLoadControl(
//                DefaultLoadControl.Builder()
//                    .setBufferDurationsMs(
//                        15000, // min buffer
//                        50000, // max buffer
//                        1500,  // buffer for playback
//                        5000   // buffer for playback after rebuffer
//                    )
//                    .build()
//            )
//            .build()
//            .apply {
//                val mediaItem = MediaItem.Builder()
//                    .setUri(videoUrl)
//                    .build()
//
//                setMediaItem(mediaItem)
//                repeatMode = if (loopVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
//                volume = if (muted) 0f else 1f
//
//                addListener(object : Player.Listener {
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        when (playbackState) {
//                            Player.STATE_BUFFERING -> buffering = true
//                            Player.STATE_READY -> {
//                                buffering = false
//                                duration = this@apply.duration.coerceAtLeast(0L)
//                            }
//                            Player.STATE_ENDED -> {
//                                videoEnded = true
//                                isPlaying = false
//                                onPlayPause(false)
//                            }
//                        }
//                    }
//
//                    override fun onIsPlayingChanged(playing: Boolean) {
//                        isPlaying = playing
//                        onPlayPause(playing)
//                    }
//                })
//
//                prepare()
//            }
//    }
//
//    // Register this video player with the manager
//    LaunchedEffect(videoId) {
//        val controller = VideoPlayerManager.VideoController(
//            exoPlayer = exoPlayer,
//            onPause = {
//                exoPlayer.pause()
//                isPlaying = false
//            },
//            onPlay = {
//                if (videoEnded) {
//                    exoPlayer.seekTo(0)
//                    videoEnded = false
//                }
//                exoPlayer.play()
//                isPlaying = true
//            }
//        )
//
//        VideoPlayerManager.registerVideoPlayer(videoId, controller)
//    }
//
//    // Handle visibility changes
//    LaunchedEffect(isVisible) {
//        if (isVisible && autoPlay) {
//            // Video is visible, request to play this video
//            VideoPlayerManager.playVideo(videoId)
//        } else if (!isVisible) {
//            // Video is not visible, pause it
//            VideoPlayerManager.pauseVideo(videoId)
//        }
//    }
//
//    // Position update effect
//    LaunchedEffect(isPlaying) {
//        while (isPlaying && !videoEnded) {
//            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
//            delay(100)
//        }
//    }
//
//    // Mute control effect
//    LaunchedEffect(muted) {
//        exoPlayer.volume = if (muted) 0f else 1f
//        onMuteToggle(muted)
//    }
//
//    // Auto-hide controls
//    fun scheduleControlsHide() {
//        controlsJob?.cancel()
//        controlsJob = coroutineScope.launch {
//            delay(3000)
//            showControls = false
//        }
//    }
//
//    // Show controls temporarily
//    fun showControlsTemporarily() {
//        showControls = true
//        scheduleControlsHide()
//    }
//
//    // Function to toggle play/pause
//    fun togglePlayPause() {
//        if (isVisible) {
//            if (isPlaying) {
//                VideoPlayerManager.pauseVideo(videoId)
//            } else {
//                VideoPlayerManager.playVideo(videoId)
//            }
//            // Show controls briefly when toggling
//            showControlsTemporarily()
//        }
//    }
//
//    // Cleanup
//    DisposableEffect(videoUrl) {
//        onDispose {
//            controlsJob?.cancel()
//            VideoPlayerManager.unregisterVideoPlayer(videoId)
//            exoPlayer.release()
//        }
//    }
//
//    // Lifecycle management
//    val lifecycleOwner = LocalLifecycleOwner.current
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_PAUSE -> {
//                    VideoPlayerManager.pauseAllVideos()
//                }
//                Lifecycle.Event.ON_RESUME -> {
//                    if (isVisible && autoPlay && !videoEnded) {
//                        VideoPlayerManager.playVideo(videoId)
//                    }
//                }
//                else -> {}
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.Black)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onTap = {
//                        // Single tap: toggle play/pause and show/hide controls
//                        if (enableGestures && isVisible) {
//                            onVideoClick()
//                            togglePlayPause()
//                        }
//                    },
//                    onDoubleTap = {
//                        // Double tap: custom action
//                        if (enableGestures && isVisible) {
//                            onDoubleTap()
//                        }
//                    }
//                )
//            }
//    ) {
//        // Video Player Surface
//        AndroidView(
//            factory = { context ->
//                PlayerView(context).apply {
//                    player = exoPlayer
//                    useController = false
//                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        // Loading indicator
//        if (buffering) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(48.dp),
//                color = Color.White,
//                strokeWidth = 3.dp
//            )
//        }
//
//        // Play/Pause overlay (appears on video end or when paused)
//        if ((videoEnded || (!isPlaying && showControls)) && !buffering) {
//            IconButton(
//                onClick = {
//                    if (isVisible) {
//                        if (videoEnded) {
//                            VideoPlayerManager.playVideo(videoId)
//                            videoEnded = false
//                        } else {
//                            togglePlayPause()
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(80.dp)
//                    .background(
//                        Color.Black.copy(alpha = 0.6f),
//                        CircleShape
//                    )
//            ) {
//                Icon(
//                    imageVector = if (videoEnded) Icons.Default.PlayArrow else
//                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
//                    contentDescription = if (videoEnded) "Replay" else
//                        if (isPlaying) "Pause" else "Play",
//                    tint = Color.White,
//                    modifier = Modifier.size(40.dp)
//                )
//            }
//        }
//
//        // Controls overlay
//        AnimatedVisibility(
//            visible = showControls && !buffering,
//            enter = fadeIn(animationSpec = tween(200)),
//            exit = fadeOut(animationSpec = tween(200)),
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        Brush.verticalGradient(
//                            colors = listOf(
//                                Color.Transparent,
//                                Color.Black.copy(alpha = 0.3f)
//                            ),
//                            startY = 0f,
//                            endY = Float.POSITIVE_INFINITY
//                        )
//                    )
//            ) {
//                // Top controls
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .align(Alignment.TopEnd),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    if (showMuteButton) {
//                        IconButton(
//                            onClick = { muted = !muted },
//                            modifier = Modifier
//                                .background(
//                                    Color.Black.copy(alpha = 0.5f),
//                                    CircleShape
//                                )
//                        ) {
//                            Icon(
//                                imageVector = if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
//                                contentDescription = if (muted) "Unmute" else "Mute",
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                // Bottom controls (progress bar)
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                        .padding(horizontal = 16.dp, vertical = 24.dp)
//                ) {
//                    // Progress bar
//                    if (duration > 0) {
//                        val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
//
//                        LinearProgressIndicator(
//                            progress = { progress },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(2.dp),
//                            color = Color.White,
//                            trackColor = Color.White.copy(alpha = 0.3f)
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Time indicators
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = formatTime(currentPosition),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                            Text(
//                                text = formatTime(duration),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Utility function to format time
//private fun formatTime(timeMs: Long): String {
//    val totalSeconds = timeMs / 1000
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return String.format("%d:%02d", minutes, seconds)
//}























//package com.cc.creatorcircle.ui.screens.home
//
//import android.view.GestureDetector
//import android.view.MotionEvent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.VolumeOff
//import androidx.compose.material.icons.filled.VolumeUp
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableLongStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.media3.common.MediaItem
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.DefaultLoadControl
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.AspectRatioFrameLayout
//import androidx.media3.ui.PlayerView
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@androidx.annotation.OptIn(UnstableApi::class)
//@Composable
//fun VideoPlayer(
//    videoUrl: String,
//    modifier: Modifier = Modifier,
//    autoPlay: Boolean = true,
//    isMuted: Boolean = false,
//    onMuteToggle: (Boolean) -> Unit = {},
//    onPlayPause: (Boolean) -> Unit = {},
//    onVideoClick: () -> Unit = {},
//    onDoubleTap: () -> Unit = {},
//    enableGestures: Boolean = true,
//    showMuteButton: Boolean = true,
//    loopVideo: Boolean = true
//) {
//    val context = LocalContext.current
//    var isPlaying by remember { mutableStateOf(autoPlay) }
//    var showControls by remember { mutableStateOf(false) }
//    var currentPosition by remember { mutableLongStateOf(0L) }
//    var duration by remember { mutableLongStateOf(0L) }
//    var buffering by remember { mutableStateOf(false) }
//    var videoEnded by remember { mutableStateOf(false) }
//    var muted by remember { mutableStateOf(isMuted) }
//    var videoSize by remember { mutableStateOf(IntSize.Zero) }
//
//    // Control visibility timer
//    var controlsJob: Job? by remember { mutableStateOf(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Create ExoPlayer with production configurations
//    val exoPlayer = remember(videoUrl) {
//        ExoPlayer.Builder(context)
//            .setLoadControl(
//                DefaultLoadControl.Builder()
//                    .setBufferDurationsMs(
//                        15000, // min buffer
//                        50000, // max buffer
//                        1500,  // buffer for playback
//                        5000   // buffer for playback after rebuffer
//                    )
//                    .build()
//            )
//            .build()
//            .apply {
//                val mediaItem = MediaItem.Builder()
//                    .setUri(videoUrl)
//                    .build()
//
//                setMediaItem(mediaItem)
//                repeatMode = if (loopVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
//                volume = if (muted) 0f else 1f
//
//                addListener(object : Player.Listener {
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        when (playbackState) {
//                            Player.STATE_BUFFERING -> buffering = true
//                            Player.STATE_READY -> {
//                                buffering = false
//                                duration = this@apply.duration.coerceAtLeast(0L)
//                            }
//                            Player.STATE_ENDED -> {
//                                videoEnded = true
//                                isPlaying = false
//                                onPlayPause(false)
//                            }
//                        }
//                    }
//
//                    override fun onIsPlayingChanged(playing: Boolean) {
//                        isPlaying = playing
//                        onPlayPause(playing)
//                    }
//
//
//                })
//
//                if (autoPlay) {
//                    playWhenReady = true
//                }
//                prepare()
//            }
//    }
//
//    // Position update effect
//    LaunchedEffect(isPlaying) {
//        while (isPlaying && !videoEnded) {
//            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
//            delay(100)
//        }
//    }
//
//    // Mute control effect
//    LaunchedEffect(muted) {
//        exoPlayer.volume = if (muted) 0f else 1f
//        onMuteToggle(muted)
//    }
//
//    // Auto-hide controls
//    fun scheduleControlsHide() {
//        controlsJob?.cancel()
//        controlsJob = coroutineScope.launch {
//            delay(3000)
//            showControls = false
//        }
//    }
//
//    // Show controls temporarily
//    fun showControlsTemporarily() {
//        showControls = true
//        scheduleControlsHide()
//    }
//
//    // Function to toggle play/pause
//    fun togglePlayPause() {
//        if (videoEnded) {
//            // If video ended, restart from beginning
//            exoPlayer.seekTo(0)
//            exoPlayer.play()
//            videoEnded = false
//        } else if (isPlaying) {
//            // If playing, pause
//            exoPlayer.pause()
//        } else {
//            // If paused, play
//            exoPlayer.play()
//        }
//        // Show controls briefly when toggling
//        showControlsTemporarily()
//    }
//
//    // Cleanup
//    DisposableEffect(videoUrl) {
//        onDispose {
//            controlsJob?.cancel()
//            exoPlayer.release()
//        }
//    }
//
//    // Lifecycle management
//    val lifecycleOwner = LocalLifecycleOwner.current
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_PAUSE -> {
//                    exoPlayer.pause()
//                }
//                Lifecycle.Event.ON_RESUME -> {
//                    if (autoPlay && !videoEnded) {
//                        exoPlayer.play()
//                    }
//                }
//                else -> {}
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.Black)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onTap = {
//                        // Single tap: toggle play/pause and show/hide controls
//                        if (enableGestures) {
//                            onVideoClick()
//                            togglePlayPause()
//                        }
//                    },
//                    onDoubleTap = {
//                        // Double tap: custom action
//                        if (enableGestures) {
//                            onDoubleTap()
//                        }
//                    }
//                )
//            }
//    ) {
//        // Video Player Surface
//        AndroidView(
//            factory = { context ->
//                PlayerView(context).apply {
//                    player = exoPlayer
//                    useController = false
//                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        // Loading indicator
//        if (buffering) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(48.dp),
//                color = Color.White,
//                strokeWidth = 3.dp
//            )
//        }
//
//        // Play/Pause overlay (appears on video end or when paused)
//        if ((videoEnded || (!isPlaying && showControls)) && !buffering) {
//            IconButton(
//                onClick = {
//                    if (videoEnded) {
//                        exoPlayer.seekTo(0)
//                        exoPlayer.play()
//                        videoEnded = false
//                    } else {
//                        togglePlayPause()
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(80.dp)
//                    .background(
//                        Color.Black.copy(alpha = 0.6f),
//                        CircleShape
//                    )
//            ) {
//                Icon(
//                    imageVector = if (videoEnded) Icons.Default.PlayArrow else
//                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
//                    contentDescription = if (videoEnded) "Replay" else
//                        if (isPlaying) "Pause" else "Play",
//                    tint = Color.White,
//                    modifier = Modifier.size(40.dp)
//                )
//            }
//        }
//
//        // Controls overlay
//        AnimatedVisibility(
//            visible = showControls && !buffering,
//            enter = fadeIn(animationSpec = tween(200)),
//            exit = fadeOut(animationSpec = tween(200)),
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        Brush.verticalGradient(
//                            colors = listOf(
//                                Color.Transparent,
//                                Color.Black.copy(alpha = 0.3f)
//                            ),
//                            startY = 0f,
//                            endY = Float.POSITIVE_INFINITY
//                        )
//                    )
//            ) {
//                // Top controls
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .align(Alignment.TopEnd),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    if (showMuteButton) {
//                        IconButton(
//                            onClick = { muted = !muted },
//                            modifier = Modifier
//                                .background(
//                                    Color.Black.copy(alpha = 0.5f),
//                                    CircleShape
//                                )
//                        ) {
//                            Icon(
//                                imageVector = if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
//                                contentDescription = if (muted) "Unmute" else "Mute",
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                // Bottom controls (progress bar)
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                        .padding(horizontal = 16.dp, vertical = 24.dp)
//                ) {
//                    // Progress bar
//                    if (duration > 0) {
//                        val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
//
//                        LinearProgressIndicator(
//                            progress = { progress },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(2.dp),
//                            color = Color.White,
//                            trackColor = Color.White.copy(alpha = 0.3f)
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Time indicators
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = formatTime(currentPosition),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                            Text(
//                                text = formatTime(duration),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Utility function to format time
//private fun formatTime(timeMs: Long): String {
//    val totalSeconds = timeMs / 1000
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return String.format("%d:%02d", minutes, seconds)
//}










//
//
//import android.view.GestureDetector
//import android.view.MotionEvent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.VolumeOff
//import androidx.compose.material.icons.filled.VolumeUp
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableLongStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.media3.common.MediaItem
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.DefaultLoadControl
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.AspectRatioFrameLayout
//import androidx.media3.ui.PlayerView
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//// Video player manager to control which video should play
//object VideoPlayerManager {
//    private var currentPlayingVideoId: String? = null
//    private val videoControllers = mutableMapOf<String, VideoController>()
//
//    data class VideoController(
//        val exoPlayer: ExoPlayer,
//        val onPause: () -> Unit,
//        val onPlay: () -> Unit
//    )
//
//    fun registerVideoPlayer(videoId: String, controller: VideoController) {
//        videoControllers[videoId] = controller
//    }
//
//    fun unregisterVideoPlayer(videoId: String) {
//        videoControllers.remove(videoId)
//    }
//
//    fun playVideo(videoId: String) {
//        // Pause currently playing video if different
//        if (currentPlayingVideoId != videoId) {
//            currentPlayingVideoId?.let { currentId ->
//                videoControllers[currentId]?.onPause?.invoke()
//            }
//            currentPlayingVideoId = videoId
//        }
//
//        // Play the requested video
//        videoControllers[videoId]?.onPlay?.invoke()
//    }
//
//    fun pauseVideo(videoId: String) {
//        if (currentPlayingVideoId == videoId) {
//            videoControllers[videoId]?.onPause?.invoke()
//            currentPlayingVideoId = null
//        }
//    }
//
//    fun pauseAllVideos() {
//        videoControllers.values.forEach { it.onPause() }
//        currentPlayingVideoId = null
//    }
//
//    fun isCurrentlyPlaying(videoId: String): Boolean {
//        return currentPlayingVideoId == videoId
//    }
//}
//
//@androidx.annotation.OptIn(UnstableApi::class)
//@Composable
//fun VideoPlayer(
//    videoUrl: String,
//    modifier: Modifier = Modifier,
//    autoPlay: Boolean = true,
//    isMuted: Boolean = false,
//    onMuteToggle: (Boolean) -> Unit = {},
//    onPlayPause: (Boolean) -> Unit = {},
//    onVideoClick: () -> Unit = {},
//    onDoubleTap: () -> Unit = {},
//    enableGestures: Boolean = true,
//    showMuteButton: Boolean = true,
//    loopVideo: Boolean = true,
//    videoId: String = videoUrl // Unique identifier for this video
//) {
//    val context = LocalContext.current
//    var isPlaying by remember { mutableStateOf(false) }
//    var showControls by remember { mutableStateOf(false) }
//    var currentPosition by remember { mutableLongStateOf(0L) }
//    var duration by remember { mutableLongStateOf(0L) }
//    var buffering by remember { mutableStateOf(false) }
//    var videoEnded by remember { mutableStateOf(false) }
//    var muted by remember { mutableStateOf(isMuted) }
//    var videoSize by remember { mutableStateOf(IntSize.Zero) }
//
//    // Control visibility timer
//    var controlsJob: Job? by remember { mutableStateOf(null) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Create ExoPlayer with production configurations
//    val exoPlayer = remember(videoUrl) {
//        ExoPlayer.Builder(context)
//            .setLoadControl(
//                DefaultLoadControl.Builder()
//                    .setBufferDurationsMs(
//                        15000, // min buffer
//                        50000, // max buffer
//                        1500,  // buffer for playback
//                        5000   // buffer for playback after rebuffer
//                    )
//                    .build()
//            )
//            .build()
//            .apply {
//                val mediaItem = MediaItem.Builder()
//                    .setUri(videoUrl)
//                    .build()
//
//                setMediaItem(mediaItem)
//                repeatMode = if (loopVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
//                volume = if (muted) 0f else 1f
//
//                addListener(object : Player.Listener {
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        when (playbackState) {
//                            Player.STATE_BUFFERING -> buffering = true
//                            Player.STATE_READY -> {
//                                buffering = false
//                                duration = this@apply.duration.coerceAtLeast(0L)
//                            }
//                            Player.STATE_ENDED -> {
//                                videoEnded = true
//                                isPlaying = false
//                                onPlayPause(false)
//                            }
//                        }
//                    }
//
//                    override fun onIsPlayingChanged(playing: Boolean) {
//                        isPlaying = playing
//                        onPlayPause(playing)
//                    }
//                })
//
//                prepare()
//            }
//    }
//
//    // Register this video player with the manager
//    LaunchedEffect(videoId) {
//        val controller = VideoPlayerManager.VideoController(
//            exoPlayer = exoPlayer,
//            onPause = {
//                exoPlayer.pause()
//                isPlaying = false
//            },
//            onPlay = {
//                if (videoEnded) {
//                    exoPlayer.seekTo(0)
//                    videoEnded = false
//                }
//                exoPlayer.play()
//                isPlaying = true
//            }
//        )
//
//        VideoPlayerManager.registerVideoPlayer(videoId, controller)
//    }
//
//    // Position update effect
//    LaunchedEffect(isPlaying) {
//        while (isPlaying && !videoEnded) {
//            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
//            delay(100)
//        }
//    }
//
//    // Mute control effect
//    LaunchedEffect(muted) {
//        exoPlayer.volume = if (muted) 0f else 1f
//        onMuteToggle(muted)
//    }
//
//    // Auto-hide controls
//    fun scheduleControlsHide() {
//        controlsJob?.cancel()
//        controlsJob = coroutineScope.launch {
//            delay(3000)
//            showControls = false
//        }
//    }
//
//    // Show controls temporarily
//    fun showControlsTemporarily() {
//        showControls = true
//        scheduleControlsHide()
//    }
//
//    // Function to toggle play/pause
//    fun togglePlayPause() {
//        if (isPlaying) {
//            VideoPlayerManager.pauseVideo(videoId)
//        } else {
//            VideoPlayerManager.playVideo(videoId)
//        }
//        // Show controls briefly when toggling
//        showControlsTemporarily()
//    }
//
//    // Cleanup
//    DisposableEffect(videoUrl) {
//        onDispose {
//            controlsJob?.cancel()
//            VideoPlayerManager.unregisterVideoPlayer(videoId)
//            exoPlayer.release()
//        }
//    }
//
//    // Lifecycle management
//    val lifecycleOwner = LocalLifecycleOwner.current
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_PAUSE -> {
//                    VideoPlayerManager.pauseAllVideos()
//                }
//                Lifecycle.Event.ON_RESUME -> {
//                    if (autoPlay && !videoEnded) {
//                        // Only auto-resume if this was the last playing video
//                        if (VideoPlayerManager.isCurrentlyPlaying(videoId)) {
//                            VideoPlayerManager.playVideo(videoId)
//                        }
//                    }
//                }
//                else -> {}
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.Black)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onTap = {
//                        // Single tap: toggle play/pause and show/hide controls
//                        if (enableGestures) {
//                            onVideoClick()
//                            togglePlayPause()
//                        }
//                    },
//                    onDoubleTap = {
//                        // Double tap: custom action
//                        if (enableGestures) {
//                            onDoubleTap()
//                        }
//                    }
//                )
//            }
//    ) {
//        // Video Player Surface
//        AndroidView(
//            factory = { context ->
//                PlayerView(context).apply {
//                    player = exoPlayer
//                    useController = false
//                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        // Loading indicator
//        if (buffering) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(48.dp),
//                color = Color.White,
//                strokeWidth = 3.dp
//            )
//        }
//
//        // Play/Pause overlay (appears on video end or when paused)
//        if ((videoEnded || (!isPlaying && showControls)) && !buffering) {
//            IconButton(
//                onClick = {
//                    if (videoEnded) {
//                        VideoPlayerManager.playVideo(videoId)
//                        videoEnded = false
//                    } else {
//                        togglePlayPause()
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(80.dp)
//                    .background(
//                        Color.Black.copy(alpha = 0.6f),
//                        CircleShape
//                    )
//            ) {
//                Icon(
//                    imageVector = if (videoEnded) Icons.Default.PlayArrow else
//                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
//                    contentDescription = if (videoEnded) "Replay" else
//                        if (isPlaying) "Pause" else "Play",
//                    tint = Color.White,
//                    modifier = Modifier.size(40.dp)
//                )
//            }
//        }
//
//        // Controls overlay
//        AnimatedVisibility(
//            visible = showControls && !buffering,
//            enter = fadeIn(animationSpec = tween(200)),
//            exit = fadeOut(animationSpec = tween(200)),
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        Brush.verticalGradient(
//                            colors = listOf(
//                                Color.Transparent,
//                                Color.Black.copy(alpha = 0.3f)
//                            ),
//                            startY = 0f,
//                            endY = Float.POSITIVE_INFINITY
//                        )
//                    )
//            ) {
//                // Top controls
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .align(Alignment.TopEnd),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    if (showMuteButton) {
//                        IconButton(
//                            onClick = { muted = !muted },
//                            modifier = Modifier
//                                .background(
//                                    Color.Black.copy(alpha = 0.5f),
//                                    CircleShape
//                                )
//                        ) {
//                            Icon(
//                                imageVector = if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
//                                contentDescription = if (muted) "Unmute" else "Mute",
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                // Bottom controls (progress bar)
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                        .padding(horizontal = 16.dp, vertical = 24.dp)
//                ) {
//                    // Progress bar
//                    if (duration > 0) {
//                        val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
//
//                        LinearProgressIndicator(
//                            progress = { progress },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(2.dp),
//                            color = Color.White,
//                            trackColor = Color.White.copy(alpha = 0.3f)
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Time indicators
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = formatTime(currentPosition),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                            Text(
//                                text = formatTime(duration),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontFamily = FontFamily.Monospace
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Utility function to format time
//private fun formatTime(timeMs: Long): String {
//    val totalSeconds = timeMs / 1000
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return String.format("%d:%02d", minutes, seconds)
//}

