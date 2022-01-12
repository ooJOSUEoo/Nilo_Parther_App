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
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding //llamar a binding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){ //ver si existe un usuario Autenticado
            val user = FirebaseAuth.getInstance().currentUser //variable para el user Auth
            if (user != null){
                Toast.makeText(this,"Bienvenido.",Toast.LENGTH_SHORT).show()
            }
        }else{
            if (response == null) { //el usuario al pulsado hacia atras
                Toast.makeText(this,"Hasta pronto.",Toast.LENGTH_SHORT).show()
                finish() //cierra la app
            }else{
                response.error?.let { //si un error no es nulo hara...
                    if (it.errorCode == ErrorCodes.NO_NETWORK) {//no hay red
                        Toast.makeText(this,"Sin red.",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Codigo del error: ${it.errorCode}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
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
                binding.llProgress.visibility = View.GONE
            }else{
                val providers = arrayListOf(//son los proveedores
                    AuthUI.IdpConfig.EmailBuilder().build(), //email
                    AuthUI.IdpConfig.GoogleBuilder().build())  //google

                resultLauncher.launch( //hace la instancia donde se agregan a los proveedores
                    AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)//automaticamente te envia a la seccion de login si esta en true
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
                            binding.llProgress.visibility = View.VISIBLE
                        }else{
                            Toast.makeText(this,"No se pudo cerrar la sesión.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}