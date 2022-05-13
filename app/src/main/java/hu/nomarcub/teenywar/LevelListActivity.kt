package hu.nomarcub.teenywar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import hu.nomarcub.teenywar.adapter.LevelAdapter
import hu.nomarcub.teenywar.extensions.getLevelsFromSavedFile
import hu.nomarcub.teenywar.extensions.volume
import hu.nomarcub.teenywar.model.control.Level
import kotlinx.android.synthetic.main.activity_level_list.*
import kotlinx.android.synthetic.main.level_list.*

class LevelListActivity : AppCompatActivity(),
    LevelAdapter.LevelItemClickListener {

    private lateinit var levelAdapter: LevelAdapter
    private lateinit var backgroundMusic: BackgroundMusic
    private var multi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_list)

        multi = intent.getBooleanExtra("multi", false)
        backgroundMusic = BackgroundMusic(if (multi) R.raw.music_menu_player else R.raw.music_menu_ai, this)

        setSupportActionBar(toolbar)
        toolbar.title = title

        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
//        backgroundMusic.adjustVolume(volume)

        levelAdapter.clear()
        levelAdapter.addAll(getLevelsFromSavedFile(multi))
    }

    override fun onPause() {
        backgroundMusic.adjustVolume(0)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        backgroundMusic.adjustVolume(volume)
    }

    override fun onStop() {
//        backgroundMusic.adjustVolume(0)
        super.onStop()
    }

    private fun setupRecyclerView() {
        levelAdapter = LevelAdapter()
        levelAdapter.itemClickListener = this
        levelRecyclerView.adapter = levelAdapter
    }


    override fun onItemClick(level: Level, id: Int) {
        Level.current = level

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("multi", multi)
        intent.putExtra("id", id)

        startActivity(intent)
    }

    override fun onItemLongClick(position: Int, view: View): Boolean {
//        val popup = PopupMenu(this, view)
//        popup.inflate(R.menu.menu_level)
//        popup.setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.delete -> levelAdapter.deleteRow(position)
//            }
//            false
//        }
//        popup.show()
        return false
    }

}