package ru.versoit.smartwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import ru.versoit.smartwatch.databinding.FragmentThirdBinding
import kotlin.system.exitProcess

class ThirdFragment : Fragment() {

    private var _binding: FragmentThirdBinding? = null
    private val binding: FragmentThirdBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        changeButtonTextByClockState(binding.pauseStop)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pauseStop.setOnClickListener {
            binding.clockView.paused = !binding.clockView.paused
            changeButtonTextByClockState(binding.pauseStop)
        }

        binding.closeAppButton.setOnClickListener {
            exitProcess(0)
        }
    }

    private fun changeButtonTextByClockState(button: Button) {
        button.text = if (binding.clockView.paused) "resume" else "pause"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}