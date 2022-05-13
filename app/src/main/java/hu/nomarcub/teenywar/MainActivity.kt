package hu.nomarcub.teenywar

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import hu.nomarcub.teenywar.extensions.migrateLevels
import hu.nomarcub.teenywar.extensions.volume
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val SETTINGS_REQUEST_CODE = 100
    }

    private val backgroundMusic = BackgroundMusic(R.raw.music_menu_main, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.setDefaultValues(this, R.xml.settings, false)

        btnSettings.setOnClickListener {
            startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_REQUEST_CODE)
        }

        btnLocalLevels.setOnClickListener {
            val intent = Intent(this, LevelListActivity::class.java)
            intent.putExtra("multi", false)
            startActivity(intent)
        }

        btnMultiplayer.setOnClickListener {
            val intent = Intent(this, LevelListActivity::class.java)
            intent.putExtra("multi", true)
            startActivity(intent)
        }

        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        if (settings.getBoolean("first_opened", true)) {
            settings.edit().putBoolean("first_opened", false).apply()
            migrateLevels()
        }
    }

    override fun onStart() {
        super.onStart()
        backgroundMusic.adjustVolume(volume)
    }

    override fun onStop() {
        backgroundMusic.adjustVolume(0)
        super.onStop()
    }
}