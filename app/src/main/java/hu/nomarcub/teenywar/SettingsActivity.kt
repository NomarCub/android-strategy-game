package hu.nomarcub.teenywar

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.widget.TextView
import hu.nomarcub.teenywar.extensions.localColor
import hu.nomarcub.teenywar.extensions.textOnBackgroundColor
import hu.nomarcub.teenywar.extensions.volume


class SettingsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_settings)
//
//        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
//    }

    private lateinit var colorDisplayer: TextView
    private val backgroundMusic = BackgroundMusic(R.raw.music_menu_settings, this)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        colorDisplayer = layoutInflater.inflate(R.layout.color_displayer, null) as TextView
        adjustColor()
        listView.addHeaderView(colorDisplayer)
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
        backgroundMusic.adjustVolume(volume)
    }

    override fun onStop() {
        backgroundMusic.adjustVolume(0)
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "red", "green", "blue" -> adjustColor()
            "volume" -> backgroundMusic.adjustVolume(volume)
        }
    }

    override fun isValidFragment(fragmentName: String) =
        fragmentName == "hu.nomarcub.teenywar.SettingsActivity\$SettingsFragment"

    private fun adjustColor() {
        val color = localColor
        colorDisplayer.setBackgroundColor(color)
        colorDisplayer.setTextColor(color.textOnBackgroundColor())
    }


//    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
//        loadHeadersFromResource(R.xml.settings_headers, target) }

//    class SettingsFragment : PreferenceFragment() {
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            addPreferencesFromResource(R.xml.settings)
//        }
//    }
}