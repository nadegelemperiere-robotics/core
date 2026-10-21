/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Panel plugin managing alliance display navlet
   ------------------------------------------------------- */
package com.bylazar.alliance

// System includes
import android.content.Context
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask

// Qualcomm includes
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl

// Panels includes
import com.bylazar.panels.Panels
import com.bylazar.panels.plugins.BasePluginConfig
import com.bylazar.panels.plugins.Plugin
import com.bylazar.panels.server.Socket

open class AlliancePluginConfig : BasePluginConfig() {
    open var test = "test"
}

object Plugin : Plugin<AlliancePluginConfig>(AlliancePluginConfig()) {


    private var timer: Timer = Timer()

    var provider = AllianceProvider()

    private var opModeManagerRef: WeakReference<OpModeManagerImpl>? = null


    fun startSendingTime() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    provider.update()?.let { send("alliance", it) }
                } catch (e: IOException) {
                    stopTimer()
                }
            }
        }, 0, 1000)
    }

    fun stopTimer() {
        timer.cancel()
        timer.purge()
    }

    override fun onNewClient(client: Socket.ClientSocket) {
        // Colors are only sent on change : give the current one to newly connected clients
        sendClient(client, "alliance", provider.color)
    }

    override fun onMessage(client: Socket.ClientSocket, type: String, data: Any?) {
        log("Got message of type $type with data $data")
    }

    override fun onRegister(
        panelsInstance: Panels,
        context: Context
    ) {

    }

    override fun onAttachEventLoop(eventLoop: FtcEventLoop) {
    }

    override fun onOpModeManager(o: OpModeManagerImpl) {
        opModeManagerRef = WeakReference(o)
        startSendingTime()
    }

    override fun onOpModePreInit(opMode: OpMode) {
    }

    override fun onOpModePreStart(opMode: OpMode) {
    }

    override fun onOpModePostStop(opMode: OpMode) {
    }


    override fun onEnablePanels() {
        startSendingTime()
    }

    override fun onDisablePanels() {
        stopTimer()
    }
}