package com.example.mad_todo_list_assignment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val llContainer = findViewById<LinearLayout>(R.id.llHistoryContainer)
        val tvNoHistory = findViewById<TextView>(R.id.tvNoHistory)

        val histPrefs = getSharedPreferences("step_history", Context.MODE_PRIVATE)
        val allData = histPrefs.all  // Map of date -> steps

        if (allData.isEmpty()) {
            tvNoHistory.visibility = View.VISIBLE
        } else {
            tvNoHistory.visibility = View.GONE

            // show last 7 days
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

            val calendar = Calendar.getInstance()
            for (i in 0 until 7) {
                val dateKey = dateFormat.format(calendar.time)
                val steps = histPrefs.getInt(dateKey, -1)

                // header row
                val tvDay = TextView(this)
                tvDay.text = displayFormat.format(calendar.time)
                tvDay.textSize = 13f
                tvDay.setTextColor(Color.parseColor("#616161"))
                tvDay.setPadding(0, 16, 0, 2)
                llContainer.addView(tvDay)

                // steps value row
                val tvSteps = TextView(this)
                if (steps == -1) {
                    tvSteps.text = "No data"
                    tvSteps.setTextColor(Color.parseColor("#BDBDBD"))
                } else {
                    tvSteps.text = "$steps steps"
                    tvSteps.setTextColor(Color.parseColor("#388E3C"))
                }
                tvSteps.textSize = 18f
                tvSteps.setTypeface(null, android.graphics.Typeface.BOLD)
                llContainer.addView(tvSteps)

                // divider line
                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).also { it.setMargins(0, 8, 0, 0) }
                divider.setBackgroundColor(Color.parseColor("#E0E0E0"))
                llContainer.addView(divider)

                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
