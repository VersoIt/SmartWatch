package ru.versoit.smartwatch

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.versoit.smartwatch.databinding.FragmentFirstBinding
import kotlin.math.max

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding: FragmentFirstBinding
        get() {
            return _binding!!
        }

    private var firstDraw = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
        }

        binding.clockView.viewTreeObserver.addOnDrawListener {
            if (firstDraw) {
                binding.sizeChanger.progress = binding.clockView.width / 10
                firstDraw = false
            }
        }

        binding.sizeChanger.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.clockView.layoutParams.height = max(1, progress * 10)
                binding.clockView.requestLayout()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.changeCircleColor.setOnClickListener {
            binding.clockView.colorContainer =
                ContextCompat.getColor(requireContext(), R.color.circle_new_color)
        }

        binding.changeNumbersColor.setOnClickListener {
            binding.clockView.textColor = Color.GRAY
        }

        binding.changeShape.setOnClickListener {
            binding.clockView.clockShape = R.drawable.clock_new_shape
        }

        binding.changeHourHandColor.setOnClickListener {
            binding.clockView.hourHandColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        binding.changeMinuteHandColor.setOnClickListener {
            binding.clockView.minuteHandColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        binding.changeSecondHandColor.setOnClickListener {
            binding.clockView.secondHandColor = ContextCompat.getColor(requireContext(), R.color.black)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}