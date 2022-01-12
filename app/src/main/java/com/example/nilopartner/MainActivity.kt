package com.example.nilopartner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()) //son los email de la Auth

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val response = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK){ //ver si existe un usuario Autenticado
                val user = FirebaseAuth.getInstance().currentUser //variable para el user Auth
                if (user != null){
                    Toast.makeText(this,"Bienvenido",Toast.LENGTH_SHORT).show()
                }
            }
        }.launch(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build())
    }
}