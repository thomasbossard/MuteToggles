package com.example.mutetoggles

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.service.quicksettings.TileService
import android.widget.Toast

class MyQSTileService : TileService() {

    // Lazy initialization of audioManager
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

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
        // Check if Do Not Disturb access is granted
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val ringerMode = audioManager.ringerMode
            val newRingerMode = (ringerMode + 1) % 3  // Cycle between 0, 1, and 2

            // Change the ringer mode
            audioManager.ringerMode = newRingerMode

            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)


            updateTile()  // Update the tile UI
        } else {
            Toast.makeText(this, "Do Not Disturb Permissions are not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    // BroadcastReceiver to listen for ringer mode changes
    private val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTile() // Update the tile when the ringer mode changes
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
