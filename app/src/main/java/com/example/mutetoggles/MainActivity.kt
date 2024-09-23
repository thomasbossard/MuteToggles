package com.example.mutetoggles

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Lazy initialization of audioManager
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Get reference to the Button by its ID
        val myButton: Button = findViewById(R.id.myButton)
        val silentButton: Button = findViewById(R.id.silentButton)
        val vibrateButton: Button = findViewById(R.id.vibrateButton)
        val normalButton: Button = findViewById(R.id.normalButton)
        val testButton: Button = findViewById(R.id.testButton)

        // Set an OnClickListener for the button
        myButton.setOnClickListener {
            if (!notificationManager.isNotificationPolicyAccessGranted())
            {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Do Not Disturb Access needed")
                builder.setMessage("Please open the app and grant access to do not disturb. Do you want to do this now?")
                //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                builder.setPositiveButton("yes") { dialog, which ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

                builder.setNegativeButton("no") { dialog, which ->
                    dialog.dismiss()
                    Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show()
                }
                builder.show()
            }
            else{
                // Show a Toast or perform any action
                Toast.makeText(this, "Permissions granted already :)", Toast.LENGTH_SHORT).show()
            }

        }

        silentButton.setOnClickListener {
          //  audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            //audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            //notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)

            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL // Ensure the mode is Normal
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_RING,
                AudioManager.ADJUST_MUTE,
                0
            )
        }

        vibrateButton.setOnClickListener {
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        }

        normalButton.setOnClickListener {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
           audioManager.adjustStreamVolume(
         AudioManager.STREAM_RING,
               AudioManager.ADJUST_UNMUTE,
             0
           )
        }

        testButton.setOnClickListener {
            Toast.makeText(this, audioManager.getRingerMode().toString(), Toast.LENGTH_SHORT).show()
        }

    }
}