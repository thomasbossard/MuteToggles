package com.example.mutetoggles

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.service.quicksettings.TileService
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MyQSTileService : TileService() {

    // Lazy initialization of audioManager
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val mutex = Mutex()

    // Called when the user adds your tile.
    override fun onTileAdded() {
        super.onTileAdded()
        updateTile() // Initial update when the tile is added
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
        registerReceiver(ringerModeReceiver, IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION))
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
        unregisterReceiver(ringerModeReceiver)
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        CoroutineScope(Dispatchers.Main).launch {
            mutex.withLock {
                // Check if Do Not Disturb access is granted
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    val ringerMode = audioManager.ringerMode
                    val newRingerMode = (ringerMode + 1) % 3  // Cycle between 0, 1, and 2



                    when (newRingerMode) {
                        0 -> {
                            audioManager.ringerMode =
                                AudioManager.RINGER_MODE_NORMAL // Ensure the mode is Normal
                            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) //WHY DOES THIS CHANGE EL FLAGO  ?? WHY GOOGLE ???? Perhaps Race condition
                            audioManager.adjustStreamVolume(
                                AudioManager.STREAM_RING,
                                AudioManager.ADJUST_MUTE,
                                0
                            )
                           // notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) //THE ORDER HERE IS CRUCIAL ??? VERIFY WITH MUTEX /COROUTINES
                            updateTile()
                        }

                        1 -> {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                            updateTile()
                        }

                        2 -> {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                            audioManager.adjustStreamVolume(
                                AudioManager.STREAM_RING,
                                AudioManager.ADJUST_UNMUTE,
                                0
                            )
                            updateTile()
                        }
                    }
                }

                // Change the ringer mode
                // audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                //audioManager.ringerMode = newRingerMode

                // notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)

                //    updateTile()  // Update the tile UI
                else {
                    Toast.makeText(
                        this@MyQSTileService,
                        "Do Not Disturb Permissions are not granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    // BroadcastReceiver to listen for ringer mode changes
    private val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            CoroutineScope(Dispatchers.Main).launch {
                mutex.withLock {
                    updateTile() // Update the tile when the ringer mode changes
                }
            }

        }

    }

    // Function to update the Quick Settings tile UI
    private fun updateTile() {
        val ringerMode = audioManager.ringerMode
        val label = when (ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> "Normal"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            else -> "Unknown"
        }

        val tile = qsTile
        tile.label = label
        tile.updateTile()  // Refresh the tile
    }
}
