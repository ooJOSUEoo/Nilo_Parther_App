package com.example.nilopartner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.nilopartner.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding //llamar a binding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){ //ver si existe un usuario Autenticado
            val user = FirebaseAuth.getInstance().currentUser //variable para el user Auth
            if (user != null){
                Toast.makeText(this,"Bienvenido",Toast.LENGTH_SHORT).show()
            }
        }else{
            if (response == null) { //el usuario al pulsado hacia atras
                Toast.makeText(this,"Hasta pronto",Toast.LENGTH_SHORT).show()
                finish() //cierra la app
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater ) //inicializar binding
        setContentView(binding.root)

        configAuth()
    }

    private fun configAuth(){
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null){ //saber si el usuario ya esta logeado
                supportActionBar?.title = auth.currentUser?.displayName
                binding.tvInit.visibility = View.VISIBLE
            }else{
                val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()) //son los email de la Auth

                resultLauncher.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out -> {//cerrar la sesión
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this,"Sesión terminada.",Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){//si es exitoso la vista se quitara
                            binding.tvInit.visibility = View.GONE
                        }else{
                            Toast.makeText(this,"No se pudo cerrar la sesión.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}