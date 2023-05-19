package com.android.aura

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.aura.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txText  = findViewById<TextView>(R.id.tv_info)

        val viewModel: MainViewModel by viewModels()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {state ->
                    when (state)
                    {
                        is MainViewModel.UIState.Empty ->  txText.text = getString(R.string.no_value)
                        is MainViewModel.UIState.NoBoots -> txText.text = getString(R.string.no_boots_detected)
                        is MainViewModel.UIState.SingleBoots -> txText.text = getString(R.string.single_boot, state.data.time)
                        is MainViewModel.UIState.MultipleBoots -> txText.text = getString(R.string.multiple_boots, state.data.time)
                    }
                }
            }
        }
    }
}