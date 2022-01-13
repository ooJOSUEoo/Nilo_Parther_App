package com.example.nilopartner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nilopartner.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() , OnProductListener, MainAux{

    private lateinit var binding: ActivityMainBinding //llamar a binding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var adapter: ProductAdapter //la clase para crud

    private lateinit var firestoreListener: ListenerRegistration

    private var productSelected: Product? = null

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
        configRecyclerView()
        //configFirestore()//llama a los datos solo una ves
        //configFirestoreRealtime()
        configButtons()
    }

    private fun configAuth(){
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null){ //saber si el usuario ya esta logeado
                supportActionBar?.title = auth.currentUser?.displayName
                binding.nsvProducts.visibility = View.VISIBLE
                binding.llProgress.visibility = View.GONE
                binding.efab.show()
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
        configFirestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()
    }

    private fun configRecyclerView(){ //muestra los productos
        adapter = ProductAdapter(mutableListOf(),this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity,3,
                GridLayoutManager.HORIZONTAL,false)
            adapter = this@MainActivity.adapter
        }

        /*(1 .. 20).forEach {
            val product = Product(it.toString(),"Producto $it",
                "Este producto es el $it","",it,it * 1.1)
            adapter.add(product)
        }*/

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
                            binding.nsvProducts.visibility = View.GONE
                            binding.llProgress.visibility = View.VISIBLE
                            binding.efab.hide()
                        }else{
                            Toast.makeText(this,"No se pudo cerrar la sesión.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configFirestore(){
        val db = FirebaseFirestore.getInstance() //hace referencia a la base de datos

        db.collection(Constants.COLL_PRODUCTS) //se llama a la tabla products
            .get()
            .addOnSuccessListener { snapshots -> //si fue exitoso la llamada a la db
                for (document in snapshots){ //extrae cada objeto y lo combierte a producto
                    val product = document.toObject(Product::class.java)
                    product.id = document.id //el id del producto sera el mismo que el documento
                    adapter.add(product)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error al consultar datos.",Toast.LENGTH_SHORT).show()
            }
    }

    private fun configFirestoreRealtime(){
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(Constants.COLL_PRODUCTS)

        firestoreListener = productRef.addSnapshotListener{ snapshots, error ->
            if (error != null){ //si hay un error...
                Toast.makeText(this,"Error al consultar datos.",Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (snapshot in snapshots!!.documentChanges){
                val product = snapshot.document.toObject(Product::class.java)
                product.id = snapshot.document.id //el id del producto sera el mismo que el documento
                when(snapshot.type){
                    DocumentChange.Type.ADDED -> adapter.add(product) //añade y se muestra el resultado en tiempo real
                    DocumentChange.Type.MODIFIED -> adapter.update(product) //lo mismo pero en actualizar
                    DocumentChange.Type.REMOVED -> adapter.delete(product) //""""
                }
            }
        }
    }

    private fun configButtons(){ //instanciar los botones de AddDialogFragment (hace el cuadro para crear)
        binding.efab.setOnClickListener {
            productSelected = null
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }

    override fun onClick(product: Product) {//se hace el cuadro de dialogo para editar

        productSelected = product
        AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
    }

    override fun onLongClick(product: Product) { //cuando se mantenga precionado un producto...
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(Constants.COLL_PRODUCTS)
        product.id?.let { id ->
            productRef.document(id)
                .delete() //se borrar el producto
                .addOnFailureListener { Toast.makeText(this,"Error al eliminar.",Toast.LENGTH_SHORT).show() }
        }
    }

    override fun getProductSelected(): Product? = productSelected
}