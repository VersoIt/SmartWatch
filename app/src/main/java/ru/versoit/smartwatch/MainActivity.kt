package ru.versoit.smartwatch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.versoit.smartwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            binding.clock.layoutParams.width = 1000
            binding.clock.requestLayout()
        }
    }
}
