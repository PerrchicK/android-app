package com.perrchick.someapplication

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.setPadding
import com.perrchick.someapplication.utilities.PerrFuncs
import kotlin.math.max
import kotlin.math.min

// Raptor / temple run / sonic / fun run
class GameActivity : AppCompatActivity() {
    companion object {
        val TAG = GameActivity::class.java.simpleName
    }

    class Configurations {
        class Speed {
            companion object {
                const val Key = "SpeedExtraKey"
                const val SLOW: Long = 1000
                const val FAST: Long = 500
                const val CRAZY: Long = 200
            }
        }
        class Lanes {
            companion object {
                const val Key = "LanesExtraKey"
                const val MINIMUM: Int = 3
                const val MAXIMUM: Int = 8
            }
        }
    }

    private var lanesCount: Int = 3
    private var hitsCounter: Int = 0
    private var selectedSpeed: Long = Configurations.Speed.CRAZY
    private var isResumed: Boolean = false
    private val rootLayout: FrameLayout by lazy { findViewById<FrameLayout>(R.id.root_layout) }
    var lanesMapping: ArrayList<ArrayList<View>> = arrayListOf()
    var playerMapping: ArrayList<View> = arrayListOf()
    private var previousPlayerIndex: Int = -1
    private val previousPlayerPosition: View?
        get() {
            if (previousPlayerIndex < 0 || playerMapping.size == 0) {
                return null
            } else {
                return playerMapping[previousPlayerIndex]
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        selectedSpeed = intent?.extras?.getLong(Configurations.Speed.Key) ?: Configurations.Speed.FAST
        lanesCount = intent?.extras?.getInt(Configurations.Lanes.Key) ?: 3

        createLanes()
        createButtons()
        createPlayer()
    }

    override fun onResume() {
        super.onResume()

        isResumed = true
        startTicking()
    }

    override fun onPause() {
        super.onPause()

        isResumed = false
    }

    override fun onDestroy() {
        super.onDestroy()

        playerMapping.clear()
        lanesMapping.clear()
    }
    private fun createPlayer() {
        if (playerMapping.size == 0) return
        playerMapping.forEach {
            it.isInvisible = true
        }

        previousPlayerPosition?.isInvisible = true
        previousPlayerIndex = playerMapping.size / 2
        previousPlayerPosition!!.toggleVisibility()
    }

    private fun createButtons() {
        val buttonsLayout = LinearLayout(this)
        buttonsLayout.orientation = LinearLayout.HORIZONTAL

        val rightButton = Button(this)
        rightButton.background = ResourcesCompat.getDrawable(resources, R.drawable.transparent, theme)
        rightButton.setOnClickListener {
            // on right pressed
            goRight()
        }
        val leftButton = Button(this)
        leftButton.background = ResourcesCompat.getDrawable(resources, R.drawable.transparent, theme)
        leftButton.setOnClickListener {
            // on left pressed
            goLeft()
        }
        buttonsLayout.addView(leftButton, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        buttonsLayout.addView(rightButton, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))

        rootLayout.addView(buttonsLayout)
    }

    private fun goRight() {
        previousPlayerPosition?.isInvisible = true
        previousPlayerIndex++
        previousPlayerIndex = min(playerMapping.size - 1, previousPlayerIndex)
        previousPlayerPosition!!.toggleVisibility()
    }

    private fun goLeft() {
        previousPlayerPosition?.isInvisible = true
        previousPlayerIndex--
        previousPlayerIndex = max(0, previousPlayerIndex)
        previousPlayerPosition!!.toggleVisibility()
    }

    private fun startTicking() {
        val handler = Handler()
        val runnable = object : Runnable {
            var tickCounter = 0
            override fun run() {
                if (!isResumed) {
                    // hide all...
                    lanesMapping.forEach { lane ->
                        lane.forEach {
                            it.isInvisible = true
                        }
                    }
                } else {
                    tick(tickCounter++)
                    handler.postDelayed(this, selectedSpeed)
                }
            }
        }

        runnable.run()
    }

    private fun tick(tickCounter: Int) {
        val index = tickCounter % lanesMapping.size
        val rand: Int = PerrFuncs.random(0, 10)

        if (rand < 5) {
            dispatchOneDownFromTop(lane = index)
        }
    }

    private fun dispatchOneDownFromTop(index: Int = 0, lane: Int) {
        val progressHandler = Handler()
        val progressRunnable = object : Runnable {
            private var previous: View? = null
            var index = index

            override fun run() {
                if (!isResumed) return

                previous?.isInvisible = true
                previous = lanesMapping[lane][(this.index) % lanesMapping[lane].size]
                val isLast = this.index == lanesMapping[lane].size
                this.index++

                if (isLast) {
//                    AppLogger.log(TAG,"got to lane: $lane")
                    onHitTheGround(lane)
                } else {
                    previous?.toggleVisibility()

                    previous?.let {
                        it.animate()
                                ?.setInterpolator(LinearInterpolator())
                                ?.translationY(it.height.toFloat())
                                ?.setDuration(selectedSpeed)?.withEndAction {
                                    it.postDelayed({
                                        it.translationY = 0f
                                    }, selectedSpeed)
                                }
                                ?.start()
                    }
                    progressHandler.postDelayed(this, selectedSpeed)
                }
            }
        }

        progressRunnable.run()
    }

    private fun onHitTheGround(lane: Int) {
        if (previousPlayerIndex == lane) {
            onPlayerCatch()
        }
    }

    private fun onPlayerCatch() {
        previousPlayerPosition?.bump()
        hitsCounter++

        val txtScore = TextView(this)//View(this)
        txtScore.text = "$hitsCounter"
        txtScore.gravity = Gravity.CENTER
        txtScore.textSize = 50F

        txtScore.scaleX = 0.0f
        txtScore.scaleY = 0.0f
        rootLayout.addView(txtScore, FrameLayout.LayoutParams(PerrFuncs.screenWidthPixels(), PerrFuncs.screenWidthPixels(), Gravity.CENTER))
        txtScore.fadeAndShow()
    }

    private fun createLanes() {
        val allLanes = LinearLayout(this)
        allLanes.orientation = LinearLayout.HORIZONTAL
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)

        for (i in 0 until lanesCount) {
            // https://www.programiz.com/kotlin-programming/for-loop
            lanesMapping.add(arrayListOf())
            val oneLane = LinearLayout(this)
            oneLane.orientation = LinearLayout.VERTICAL

            for (j in 1..10) {
//                val block = View(this)
                val block = TextView(this)
                lanesMapping[i].add(block)
                block.text = "💰"
                block.textSize = 40f
                block.gravity = Gravity.CENTER
//                block.background = ResourcesCompat.getDrawable(resources, R.drawable.transparent_border, theme)
                block.setPadding(10)
                block.isInvisible = true
                oneLane.addView(block, lp)

                if (j == 10) {
//                    val player = View(this)
                    val player = ImageView(this)
                    // Made by: https://www.img-bak.in/
                    // Create icons here: https://jgilfelt.github.io/AndroidAssetStudio/
                    player.setImageResource(R.drawable.ic_basket)
                    playerMapping.add(player)
//                    player.setBackgroundColor(Color.CYAN)
                    player.setPadding(10)
                    player.isInvisible = true
                    oneLane.addView(player, lp)
                }
            }

            allLanes.addView(oneLane, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }

        rootLayout.addView(allLanes)
    }
}

private fun View.fadeAndShow() {
    scaleX = 0.1f
    scaleY = 0.1f
    animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(300).withEndAction {
                this.animate().alpha(0f).setDuration(500)
                        .withEndAction {
                            this.removeFromSuperView()
                        }
                        .start()
            }
            .start()

}

private fun View.removeFromSuperView() {
    if (parent == null) return
    if (parent !is ViewGroup) return
    (parent as? ViewGroup)?.removeView(this)
}

private fun View.bump() {
    this.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setInterpolator(BounceInterpolator())
            .setDuration(300).
                    withEndAction {
                        this.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
                    }
            .start()

}

private fun View.toggleVisibility() {
    this.isInvisible = !this.isInvisible
}
