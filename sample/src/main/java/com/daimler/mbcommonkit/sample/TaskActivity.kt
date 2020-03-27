package com.daimler.mbcommonkit.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.daimler.mbcommonkit.tasks.SimpleTask
import kotlinx.android.synthetic.main.activity_task.*
import java.util.*

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        taskStartButton.setOnClickListener { startSimpleTask() }
        taskErrorButton.setOnClickListener { startSimpleErrorTask() }
    }

    @SuppressLint("SetTextI18n")
    private fun startSimpleTask() {
        SimpleTask { generate() }
                .onBefore { stateText.text = "onBefore" }
                .onComplete { stateText.text = it }
                .onAlways { Toast.makeText(this, "onAlways", Toast.LENGTH_SHORT).show() }
                .execute()
    }

    @SuppressLint("SetTextI18n")
    private fun startSimpleErrorTask() {
        SimpleTask { throw RuntimeException("Dummy") }
                .onBefore { stateText.text = "onBefore" }
                .onError { stateText.text = it.toString() }
                .onAlways { Toast.makeText(this, "onAlways", Toast.LENGTH_SHORT).show() }
                .execute()
    }

    private fun generate(): String {
        Thread.sleep(2000)
        return RandomGenerator.getRandomString(20)
    }

    private object RandomGenerator {

        private const val ALPHANUMERIC_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"

        /**
         * Returns a random alphanumeric string (lowercase) of the specified length or an empty string
         * if [length] is <= 0.
         */
        fun getRandomString(length: Int): String =
                (0 until length)
                        .map { ALPHANUMERIC_CHARS.randomChar() }
                        .joinToString("")

        private fun String.randomChar(): Char = this[Random().nextInt(length)]
    }
}