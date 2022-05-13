package hu.nomarcub.teenywar

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import hu.nomarcub.teenywar.extensions.*
import hu.nomarcub.teenywar.model.buidingblock.Agent
import hu.nomarcub.teenywar.model.buidingblock.Packet
import hu.nomarcub.teenywar.model.control.Level
import hu.nomarcub.teenywar.model.events.ReceiveEvent
import hu.nomarcub.teenywar.model.events.SendEvent
import kotlinx.android.synthetic.main.activity_game.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.DatagramSocket
import java.net.InetAddress

class GameActivity : AppCompatActivity() {

    class NetworkState(
        private val onConnected: () -> Unit,
        private val disconnect: () -> Unit,
        private val enableLocalResume: () -> Unit
    ) {

        var isServerConnected = false
            set(value) {
                field = value
                updateConnection()
            }
        var isClientConnected = false
            set(value) {
                field = value
                updateConnection()
            }
        val bothConnected: Boolean
            get() = isClientConnected && isServerConnected

        private fun updateConnection() {
            if (bothConnected) {
                onConnected()
            } else if (bothConnected) {
                levelsMatch = false
                levelsSent = false
            }
        }


        private var levelsSent = false
        var levelsMatch: Boolean = false
            set(value) {
                if (!bothConnected) return
                field = value
                if (value) enableLocalResume()
            }


        var localResume = false
            set(value) {
                if (!levelsMatch) return
                field = value
                resume()
            }
        var remoteResume = false
            set(value) {
                if (!levelsMatch) return
                field = value
                resume()
            }
        private val bothResume: Boolean
            get() = localResume && remoteResume

        private fun resume() {
            if (bothResume)
                Level.current.isPaused = false
        }
    }

    companion object {
        private const val tcpPort = 54555
        private const val udpPort = 54777
        private const val bufferSize = 131072
    }

    private lateinit var backgroundMusic: BackgroundMusic

    private var server = Server(bufferSize, bufferSize)
    private var client = Client(bufferSize, bufferSize)
    private var multi = false

    private lateinit var ownAddress: InetAddress
    private lateinit var remoteAddress: InetAddress

    private var levelID = 0

    private lateinit var remoteAgent: Agent
    private lateinit var remoteAgentOriginal: Agent
    //private lateinit var localAgent: Agent

    private var state = NetworkState(
        onConnected = this::onConnected,
        disconnect = this::disconnect,
        enableLocalResume = this::enableLocalResume
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        multi = intent.getBooleanExtra("multi", false)
        backgroundMusic = BackgroundMusic(if (multi) R.raw.music_game_player else R.raw.music_game_ai, this)
        levelID = intent.getIntExtra("id", 0)

        if (multi) {
            pauseButton.setOnClickListener {
                it.isEnabled = false
                pauseButton.text = getString(R.string.pause)
                state.localResume = true
                Thread { client.sendUDP("resume") }.start()
            }

            refreshButton.setOnClickListener {
                it.visibility = View.GONE
                connect()
            }
        } else
            pauseButton.setOnClickListener {
                if (Level.current.isPaused) {
                    pauseButton.text = getString(R.string.pause)
                    Level.current.isPaused = false
                } else {
                    pauseButton.text = getString(R.string.resume)
                    Level.current.isPaused = true
                }
            }
    }

    override fun onStart() {
        super.onStart()

        Level.current.isPaused = true
        EventBus.getDefault().register(Level.current)
        EventBus.getDefault().register(this)
        backgroundMusic.adjustVolume(volume)
        pauseButton.text = getString(R.string.resume)

        if (multi) {
            pauseButton.isEnabled = false

            initConnection()
            connect()
        }
    }

    override fun onStop() {
        backgroundMusic.adjustVolume(0)
        EventBus.getDefault().unregister(Level.current)
        EventBus.getDefault().unregister(this)
        Level.current.isPaused = true

        if (multi) {
            disconnect()
        }

        super.onStop()
    }

    private fun initConnection() {
        Thread {
            DatagramSocket().use { socket ->
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
                ownAddress = InetAddress.getByName(socket.localAddress.hostAddress)
            }

            server.addListener(object : Listener() {
                override fun connected(connection: Connection?) {
                    super.connected(connection)
                    state.isServerConnected = true
                }

                override fun received(connection: Connection, obj: Any) {
                    if (obj is String) {
                        when {
                            obj.startsWith("levelID") -> {
                                if (obj.split(" ")[1].toInt() == levelID) {
                                    state.levelsMatch = true
                                } else {
                                    runOnUiThread {
                                        //Snackbar.make(findViewById(android.R.id.content), getString(R.string.levels_dont_match), Snackbar.LENGTH_LONG).show()
                                        Toast.makeText(
                                            this@GameActivity,
                                            getString(R.string.levels_dont_match),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                }
                            }
                            obj.startsWith("agent") -> {
                                if (state.bothConnected) {
                                    remoteAgent = gson.fromJson(obj.drop("agent ".length), Agent::class.java)
                                    remoteAgent = Agent(Agent.Type.REMOTE, remoteAgent.name, remoteAgent.color)
                                    Level.current.changeAgent(remoteAgentOriginal, remoteAgent)
                                }
                            }
                            obj.startsWith("resume") -> {
                                state.remoteResume = true
                            }
                        }
                    }
                }

                override fun disconnected(connection: Connection?) {
                    state.isServerConnected = false
                    onDisconnected()
                    super.disconnected(connection)
                }
            })

            client.addListener(object : Listener() {
                override fun connected(connection: Connection?) {
                    super.connected(connection)
                    state.isClientConnected = true
                }

                override fun received(connection: Connection, obj: Any) {
                    if (obj is String) {
                        when {
                            obj.startsWith("packet") -> {
                                val packet = gson.fromJson(obj.drop("packet ".length), Packet::class.java)
                                Level.current.addRemotePacket(
                                    Packet(
                                        packet,
                                        Agent(Agent.Type.REMOTE, packet.owner.name, packet.owner.color)
                                    )
                                )
                            }
                        }
                    }
                }

                override fun disconnected(connection: Connection?) {
                    state.isClientConnected = false
                    onDisconnected()
                    super.disconnected(connection)
                }
            })

            server.bind(tcpPort, udpPort)
        }.start()
    }

    private var connecting: Boolean = false

    private fun connect() {
        runOnUiThread {
            loadingCircle.visibility = View.VISIBLE
        }

        connecting = true

        //TODO disconnect()

        Thread {
            server.start()
//            server.bind(tcpPort, udpPort)


//            while (this::ownAddress.isInitialized) {}
            client.start()
            var addresses: List<InetAddress>
            do {
                addresses = client.discoverHosts(udpPort, 2000).filter { !it.isLoopbackAddress && it != ownAddress }
            } while (addresses.isEmpty() && connecting)
            if (!connecting) return@Thread
            val address = addresses[0]
            remoteAddress = address

            client.connect(4000, address, tcpPort, udpPort)

            val local = (ownAddress.hostAddress > remoteAddress.hostAddress)
            Level.current.changeAgent(
                Level.current.agents.filter { it.type == Agent.Type.REMOTE }[if (local) 1 else 0],
                localAgent
            )
            remoteAgentOriginal = Level.current.agents.filter { it.type == Agent.Type.REMOTE }[0]

        }.start()
    }

    private fun disconnect() {
        connecting = false

        try {
            server.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            client.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onConnected() {
        runOnUiThread {
            loadingCircle.visibility = View.GONE
            refreshButton.visibility = View.GONE
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.connected), Snackbar.LENGTH_SHORT)
                .show()
        }

        client.sendUDP("levelID $levelID")
        client.sendUDP("agent ${gson.toJson(localAgent)}")
    }

    private fun onDisconnected() {
        runOnUiThread {
            //loadingCircle.visibility = View.VISIBLE
            refreshButton.visibility = View.VISIBLE
            //Snackbar.make(findViewById(android.R.id.content), getString(R.string.disconnected), Snackbar.LENGTH_SHORT).show()
            Toast.makeText(this, getString(R.string.disconnected), Toast.LENGTH_LONG).show()
        }
        EventBus.getDefault().unregister(Level.current)
        Level.current = getLevelsFromSavedFile(true)[levelID]
//        EventBus.getDefault().register(Level.current)
//        Level.current.isPaused=true
        finish()
    }

    private fun enableLocalResume() {
        runOnUiThread {
            pauseButton.isEnabled = true
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public fun packetSent(event: SendEvent) {
        server.connections.forEach { it.sendUDP("packet ${gson.toJson(event.packet)}") }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public fun packetReceived(event: ReceiveEvent) {
        //  if (event.wasCaptured) {
        if (Level.current.won) {
            levelWon(multi, levelID, true)
            runOnUiThread {
                Toast.makeText(this, getString(R.string.you_won), Toast.LENGTH_LONG).show()
                finish()
            }
        } else if (Level.current.lost) {
            runOnUiThread {
                levelWon(multi, levelID, false)
                Toast.makeText(this, getString(R.string.you_lost), Toast.LENGTH_LONG).show()
                finish()
            }
        }
        //}
    }
}