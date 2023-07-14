package com.shuklansh.templateapp

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.maxrave.kotlinyoutubeextractor.*
import com.maxrave.kotlinyoutubeextractor.State
import com.shuklansh.templateapp.presentation.AppViewModel
import com.shuklansh.templateapp.ui.theme.FlaskVideoPlayerTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlaskVideoPlayerTheme {
                // A surface container using the 'background' color from the theme

                val vm: AppViewModel by viewModels()
                val state by vm.listOfVideos.collectAsState()
                val listoflinks = ArrayList<String>()

                LaunchedEffect(key1 = state ) {
                    vm.updateVideoList()

                }
                for(i in state){
                    if(i.link !in listoflinks){
                        listoflinks.add(i.link)
                    }
                }



                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    Column(
                        Modifier
//                            .verticalScroll(rememberScrollState(), enabled = true)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {



//                        Text(text = state.toString())
//                        Spacer(Modifier.height(12.dp))
//                        Text(text = listoflinks.toString())
//                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxSize(),
//                            verticalArrangement = Arrangement.spacedBy(
//                                12.dp
//                            )
                        ) {
                            LazyColumn {
                                items(listoflinks) {

                                    var ctx = LocalContext.current
                                    //val scrst = rememberScrollState()
                                    var Col by remember {
                                        mutableStateOf(Color.White)
                                    }

                                    var urlofvidAvailable by remember { mutableStateOf(false) }
                                    val exoPlayer = remember {
                                        ExoPlayer.Builder(ctx).build()

                                    }
                                    var videoId by remember { mutableStateOf("") }

                                    val yt = YTExtractor(con = ctx, CACHING = true, LOGGING = true)
                                    var ytFiles: SparseArray<YtFile>? = null
                                    var videoMeta: VideoMeta? by remember { mutableStateOf(null) }

                                    LaunchedEffect(key1 = listoflinks) {


                                        videoId = YtVideoIdExtractor(it, ctx)
//                                videoId = YtVideoIdExtractor(link)
                                        Log.d("full", videoId)
                                        yt.extract(videoId)
                                        if (yt.state == State.SUCCESS) {
                                            ytFiles = yt.getYTFiles()
                                            videoMeta = yt.getVideoMeta()
                                            val ytFile = ytFiles?.get(18)
                                            val audioYtFiles =
                                                ytFiles?.getAudioOnly()?.bestQuality()
                                            val videoYtFiles =
                                                ytFiles?.getVideoOnly()?.bestQuality()
                                            val streamUrl = ytFile?.url


                                            if (videoYtFiles != null) {

//                    val mediaItem = MediaItem.fromUri(videoYtFiles.url!!) //best quality
                                                val mediaItem = if (streamUrl != null) {
                                                    MediaItem.fromUri(streamUrl)
                                                } else {
                                                    MediaItem.fromUri(videoYtFiles.url!!)
                                                } // lowest quality
                                                Log.d("besturl", videoYtFiles.url!!)
                                                Log.d("streamurl", streamUrl.toString())
                                                Log.d("streamurl", ytFiles.toString())
                                                //            val source = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

                                                exoPlayer.setMediaItem(mediaItem)
                                                exoPlayer.prepare()
                                                urlofvidAvailable = true
                                            } else {
                                                Log.d("%%%%", "null streamurl")
                                            }
                                        }

                                    }

                                    if (urlofvidAvailable == true) {

                                        Column(
                                            Modifier
                                                .fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            val displayMetrics = DisplayMetrics()
                                            windowManager.defaultDisplay.getMetrics(displayMetrics)
                                            val height = displayMetrics.heightPixels
                                            val width = displayMetrics.widthPixels


                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(800.dp)
                                            ) {
                                                DisposableEffect(
                                                    AndroidView(
                                                        factory = { context ->
//                                                PlayerView(context).apply {
//                                                    exoPlayer.playWhenReady = false
//                                                    exoPlayer.volume = 1f
//                                                    exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
//                                                    player = exoPlayer
//                                                }

                                                            StyledPlayerView(context).apply {
                                                                hideController()
                                                                exoPlayer.playWhenReady = false
                                                                useController = true
                                                                this.resizeMode =
                                                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                                                player = exoPlayer
                                                                layoutParams =
                                                                    FrameLayout.LayoutParams(
                                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                                        ViewGroup.LayoutParams.MATCH_PARENT
                                                                    )
                                                            }

                                                        })
                                                ) {
                                                    onDispose {
                                                        exoPlayer.pause()
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight()
                                                        .padding(bottom = 32.dp, start = 22.dp)
                                                ) {
                                                    Column(Modifier.align(Alignment.BottomStart)) {
                                                        Row(
                                                            Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Start
                                                        ) {
                                                            AsyncImage(
                                                                modifier = Modifier
                                                                    .border(
                                                                        BorderStroke(
                                                                            2.dp,
                                                                            Color.Red
                                                                        ),
                                                                        shape = CircleShape
                                                                    )
                                                                    .size(38.dp)

                                                                    .clip(
                                                                        CircleShape
                                                                    ),
                                                                model = "https://yt3.googleusercontent.com/tyLW5LsJGwr4ViM30OeYbuLcu_MXfpRzP8y-X9_aKfTNJeMFHmnNbPyxxhaFDA9NRgwEu9mT-g=s900-c-k-c0x00ffffff-no-rj",
                                                                contentDescription = "profileImg",
                                                                contentScale = ContentScale.Crop
                                                            )
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Column {
                                                                Text(
                                                                    text = videoMeta!!.author
                                                                        ?: "no author name",
                                                                    color = Color.White,
                                                                    textAlign = TextAlign.Start,
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.W500
                                                                )
                                                                //Spacer(modifier = Modifier.height(4.dp))
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        Text(
                                                            text = videoMeta!!.title
                                                                ?: "no title name",
                                                            color = Color(0xFFFFFFFF),
                                                            textAlign = TextAlign.Start,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.W500
                                                        )


                                                    }
                                                    Column(
                                                        Modifier.align(Alignment.BottomEnd),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {

                                                        IconButton(onClick = {
                                                            if (Col == Color.Red) {
                                                                Col = Color.White
                                                            } else Col = Color.Red
                                                        }) {
                                                            Icon(
                                                                if (Col == Color.White) Icons.Outlined.Favorite else Icons.Filled.Favorite,
                                                                "likeimg",
                                                                tint = Col,
                                                                modifier = Modifier.size(28.dp)
                                                            )

                                                        }
                                                        Text(
                                                            text = "12.2K",
                                                            color = Color(0xFFFFFFFF),
                                                            textAlign = TextAlign.Start,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.W500
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        IconButton(onClick = {
                                                        }) {
                                                            Icon(
                                                                Icons.Default.Comment,
                                                                "msg",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(28.dp)
                                                            )

                                                        }
                                                        Text(
                                                            text = "840", color = Color(0xFFFFFFFF),
                                                            textAlign = TextAlign.Start,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.W500
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        IconButton(onClick = {
                                                        }) {
                                                            Icon(
                                                                Icons.Default.Sms,
                                                                "share",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(28.dp)
                                                            )

                                                        }
                                                        Text(
                                                            text = "5.3K",
                                                            color = Color(0xFFFFFFFF),
                                                            textAlign = TextAlign.Start,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.W500
                                                        )

                                                    }


                                                }
                                            }
//                                            ,
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .height(400.dp)
//                                                .padding(12.dp)
//                                                .clip(RoundedCornerShape(8.dp))
//                                        )
//                                        Text(
//                                            text = videoMeta!!.title.toString(),
//                                            textAlign = TextAlign.Center
//                                        )
//                                        Text(
//                                            text = videoMeta!!.author.toString(),
//                                            textAlign = TextAlign.Center
//                                        )
                                        }
                                    } else {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(286.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colors.primary,
                                            )

                                        }

                                    }


                                }
                            }
                        }



                    }

                }
            }
        }
    }
}


fun YtVideoIdExtractor(link: String, ctx : Context): String {
    val linkstring = link
    if(linkstring.length>=16){
        return if (linkstring.slice(IntRange(0, 16)) == "https://youtu.be/") {
            val andd = link.length
            val eq = link.indexOf("/shorts/")
            val idOfVid = linkstring.slice(IntRange(eq + 1, andd - 1)).replace("shorts/", "")
            Log.d("shorts2", idOfVid)
            idOfVid
        } else if (linkstring.slice(IntRange(0, 30)) == "https://www.youtube.com/shorts/") {
            val andd = link.length
            val eq = link.indexOf("/shorts/")
            val idOfVid = linkstring.slice(IntRange(eq + 1, andd - 1)).replace("shorts/", "")
            Log.d("shorts", idOfVid)
            idOfVid
        } else if (linkstring.slice(IntRange(0, 26)) == "https://youtube.com/shorts/") {
            val andd = link.length
            val eq = link.indexOf("/shorts/")
            val idOfVid = linkstring.slice(IntRange(eq + 1, andd - 1)).replace("shorts/", "")
            Log.d("shorts2", idOfVid)
            idOfVid
        } else if(linkstring.slice(IntRange(0,31)) == "https://www.youtube.com/watch?v=" ) {
//        val indxofand = link.indexOf("&")
//        val indxofeq = link.indexOf("=")
//        val idOfVid = linkstring.slice(IntRange(indxofeq+1,indxofand-1))
//        return idOfVid

            val indxofand = link.indexOf("&")
            val indxofeq = link.indexOf("=")
            val andd = link.length
            println("${indxofand} .. ${indxofeq} .. ${andd}")

            if (indxofand > 0) {
                val idOfVid = linkstring.slice(IntRange(indxofeq + 1, indxofand - 1))
                return idOfVid
            } else {
                val idOfVid = linkstring.slice(IntRange(indxofeq + 1, andd - 1))
                return idOfVid
            }

        }else{
            Toast.makeText(ctx,"Link is invalid", Toast.LENGTH_SHORT).show()
            return ""
        }
    }
    else{
        Toast.makeText(ctx,"Enter a valid video link", Toast.LENGTH_SHORT).show()
        return ""
    }
}
