package com.example.mad_todo_list_assignment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    // step count at sensor start (sensor gives total steps since reboot)
    private var sensorStepsAtStart = -1

    private lateinit var tvStepCount: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvGoalValue: TextView
    private lateinit var tvGoalProgress: TextView
    private lateinit var progressGoal: ProgressBar
    private lateinit var tvDate: TextView
    private lateinit var etGoalInput: EditText

    private val PREF_NAME = "step_prefs"
    private val KEY_STEPS_TODAY = "steps_today"
    private val KEY_STEP_GOAL = "step_goal"
    private val KEY_DATE = "saved_date"
    private val KEY_SENSOR_OFFSET = "sensor_offset"
    private val PERMISSION_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStepCount    = findViewById(R.id.tvStepCount)
        tvDistance     = findViewById(R.id.tvDistance)
        tvCalories     = findViewById(R.id.tvCalories)
        tvGoalValue    = findViewById(R.id.tvGoalValue)
        tvGoalProgress = findViewById(R.id.tvGoalProgress)
        progressGoal   = findViewById(R.id.progressGoal)
        tvDate         = findViewById(R.id.tvDate)
        etGoalInput    = findViewById(R.id.etGoalInput)

        // show today date
        val today = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        tvDate.text = today

        // check if day has changed, reset steps if new day
        checkAndResetForNewDay()

        // load and display saved data
        refreshUI()

        // request permission for step sensor on Android 10+
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_CODE
            )
        } else {
            setupSensor()
        }

        // Set Goal button
        findViewById<Button>(R.id.btnSetGoal).setOnClickListener {
            val input = etGoalInput.text.toString().trim()
            if (input.isEmpty()) {
                etGoalInput.error = "Enter a number"
                return@setOnClickListener
            }
            val goal = input.toIntOrNull()
            if (goal == null || goal <= 0) {
                etGoalInput.error = "Invalid number"
                return@setOnClickListener
            }
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(KEY_STEP_GOAL, goal).apply()
            etGoalInput.setText("")
            Toast.makeText(this, getString(R.string.goal_saved), Toast.LENGTH_SHORT).show()
            refreshUI()
        }

        // History button
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Reset button - resets today's steps to 0
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            // save sensor current value as new offset so count starts from 0
            val currentSensor = prefs.getInt("current_sensor_val", 0)
            prefs.edit()
                .putInt(KEY_STEPS_TODAY, 0)
                .putInt(KEY_SENSOR_OFFSET, currentSensor)
                .apply()
            sensorStepsAtStart = -1
            Toast.makeText(this, getString(R.string.reset_done), Toast.LENGTH_SHORT).show()
            refreshUI()
        }
    }

    private fun setupSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, getString(R.string.sensor_not_found), Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sensorManager.isInitialized && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
        refreshUI()
    }

    override fun onPause() {
        super.onPause()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val totalStepsSinceBoot = event.values[0].toInt()
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // save raw sensor value for reset use
        prefs.edit().putInt("current_sensor_val", totalStepsSinceBoot).apply()

        // first reading after opening app
        if (sensorStepsAtStart == -1) {
            val savedOffset = prefs.getInt(KEY_SENSOR_OFFSET, -1)
            if (savedOffset == -1) {
                // very first time ever
                prefs.edit().putInt(KEY_SENSOR_OFFSET, totalStepsSinceBoot).apply()
                sensorStepsAtStart = totalStepsSinceBoot
            } else {
                sensorStepsAtStart = savedOffset
            }
        }

        val stepsToday = totalStepsSinceBoot - sensorStepsAtStart
        if (stepsToday >= 0) {
            prefs.edit().putInt(KEY_STEPS_TODAY, stepsToday).apply()
            saveToHistory(stepsToday)
            refreshUI()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not needed
    }

    private fun refreshUI() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val steps = prefs.getInt(KEY_STEPS_TODAY, 0)
        val goal  = prefs.getInt(KEY_STEP_GOAL, 5000)

        tvStepCount.text = steps.toString()

        // distance: avg step length ~0.75m
        val distanceKm = (steps * 0.75) / 1000.0
        tvDistance.text = String.format("%.2f km", distanceKm)

        // calories: ~0.04 kcal per step
        val calories = (steps * 0.04).toInt()
        tvCalories.text = "$calories kcal"

        // goal
        tvGoalValue.text = "Goal: $goal steps"
        val percent = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100) else 0
        tvGoalProgress.text = "$percent% completed"
        progressGoal.progress = percent
    }

    // Save today's step count to history using today's date as key
    private fun saveToHistory(steps: Int) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val histPrefs = getSharedPreferences("step_history", Context.MODE_PRIVATE)
        histPrefs.edit().putInt(dateKey, steps).apply()
    }

    // Reset steps if it's a new day
    private fun checkAndResetForNewDay() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, "")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .putInt(KEY_STEPS_TODAY, 0)
                .putInt(KEY_SENSOR_OFFSET, -1)
                .remove("current_sensor_val")
                .apply()
            sensorStepsAtStart = -1
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSensor()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }
}
