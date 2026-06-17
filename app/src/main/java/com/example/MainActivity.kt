package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.PetRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Immerse edges of window safeareas
        enableEdgeToEdge()

        // Room Database Setup
        val database = AppDatabase.getDatabase(this)
        val repository = PetRepository(database)
        
        // ViewModel Setup
        val viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(repository)
        )[AppViewModel::class.java]

        setContent {
            // Render the master portal
            MainAppScreen(viewModel = viewModel)
        }
    }
}
