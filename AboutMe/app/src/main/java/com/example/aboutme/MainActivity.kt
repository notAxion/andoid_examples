package com.example.aboutme

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import com.example.aboutme.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.doneButton.setOnClickListener{
            addNickName()
            it.visibility = View.GONE
        }
    }

    private fun addNickName() {
//        val editView = binding.nicknameEdit
//        val nicknameText = binding.nicknameText

        binding.apply {
            nicknameText.text = nicknameEdit.text
            nicknameEdit.visibility = View.GONE
            nicknameText.visibility = View.VISIBLE
        }

        // Hide the keyboard
        // no clue how it works yet
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.doneButton.windowToken, 0)
    }
}