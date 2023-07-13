package com.shuklansh.templateapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.maxrave.kotlinyoutubeextractor.*
import com.maxrave.kotlinyoutubeextractor.State
import com.shuklansh.templateapp.presentation.AppViewModel
import com.shuklansh.templateapp.ui.theme.FlaskVideoPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                            .verticalScroll(rememberScrollState(), enabled = true)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {



//                        Text(text = state.toString())
                        Spacer(Modifier.height(12.dp))
                        Text(text = listoflinks.toString())
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(
                                12.dp
                            )
                        ) {
                            listoflinks.indices.forEach{

                                var ctx = LocalContext.current
                                //val scrst = rememberScrollState()

                                var urlofvidAvailable by remember { mutableStateOf(false) }
                                val exoPlayer = remember { ExoPlayer.Builder(ctx).build() }
                                var videoId by remember { mutableStateOf("") }

                                val yt = YTExtractor(con = ctx, CACHING = true, LOGGING = true)
                                var ytFiles: SparseArray<YtFile>? = null
                                var videoMeta: VideoMeta? by remember { mutableStateOf(null) }

                                LaunchedEffect(key1 = listoflinks ){


                                    videoId = YtVideoIdExtractor(listoflinks[it],ctx)
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
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        AndroidView(
                                            factory = { context ->
                                                PlayerView(context).apply {
                                                    exoPlayer.playWhenReady = true
                                                    exoPlayer.volume = 1f
                                                    exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                                                    player = exoPlayer
                                                }

                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(400.dp)
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Text(
                                            text = videoMeta!!.title.toString(),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = videoMeta!!.author.toString(),
                                            textAlign = TextAlign.Center
                                        )
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
