package com.example.nilopartner

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilopartner.databinding.FragmentDialogAddBinding
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

//(4)
class AddDialogFragment : DialogFragment(),DialogInterface.OnShowListener {
    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null //boton positivo
    private var negativeButton: Button? = null

    private var product: Product? = null

    private var photoSelectedUri: Uri? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ //carga img a la imgView
        if (it.resultCode == Activity.RESULT_OK){
            photoSelectedUri = it.data?.data

            //binding?.imgProductPreview?.setImageURI(photoSelectedUri) //poner la img seleccionada de la galeria en el cuadro de dialogo
            binding?.let {
                Glide.with(this) //carga la img en el cuadro de dialogo
                    .load(photoSelectedUri) //ubicacion de la img
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //guarda el cache de la img
                    .centerCrop() //se visualiza bien
                    .into(it.imgProductPreview) //donde se va a ver
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity -> //si la actividad no es nula...
            binding = FragmentDialogAddBinding.inflate(LayoutInflater.from(context)) //esto es igual al xml de fragment_dialog_add

            binding?.let { //si binding no es null
                val builder = AlertDialog.Builder(activity)
                    .setTitle("Agregar producto")
                    .setPositiveButton("Agregar",null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)

                val dialog = builder.create() //se crear el builder
                dialog.setOnShowListener(this)

                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onShow(dialogInterface: DialogInterface?) { //dialogo de crear y editar
        initProduct()
        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let { //si dialog no es nulo...
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let {
                    enableUI(false)

                    uploadImage(product?.id){eventPost ->
                        if (eventPost.isSuccess){
                            if (product == null) { //dialogo de crear
                                val product = Product(
                                    name = it.etName.text.toString().trim(), //trim elimina espacios al inicio o final
                                    description = it.etDescription.text.toString().trim(),
                                    imgUrl = eventPost.photoUrl, //le asigna la url del evento
                                    quantity = it.etQuantity.text.toString().toInt(),
                                    price = it.etPrice.text.toString().toDouble()
                                ) //se crea variable con todos los datos que se enviaron

                                save(product,eventPost.documentId!!) //llamar la funcion para guardar los datos, crear el producto y le pone de id a la img el id del documento
                            }else{
                                product?.apply {
                                    name = it.etName.text.toString().trim()
                                    description = it.etDescription.text.toString().trim()
                                    imgUrl = eventPost.photoUrl
                                    quantity = it.etQuantity.text.toString().toInt()
                                    price = it.etPrice.text.toString().toDouble()

                                    update(this) //se llama la funcion para guardar y editar el producto
                                }
                            }
                        }
                    }
                }
            }
            negativeButton?.setOnClickListener {
                dismiss() //desaparece el cuadro de dialogo
            }
        }
    }

    private fun initProduct() { //llenar datos en dialogo
        product = (activity as? MainAux)?.getProductSelected()
        product?.let { product ->// se le añade datos al cuadro
            binding?.let {
                it.etName.setText(product.name)
                it.etDescription.setText(product.description)
                it.etQuantity.setText(product.quantity.toString())
                it.etPrice.setText(product.price.toString())

                Glide.with(this) //carga la img en el cuadro de dialogo
                    .load(product.imgUrl) //ubicacion de la img
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //guarda el cache de la img
                    .centerCrop() //se visualiza bien
                    .into(it.imgProductPreview) //donde se va a ver
            }
        }
    }

    private fun configButtons(){
        binding?.let {
            it.ibProduct.setOnClickListener { //cuando hay click en el imgButton abrira la galeria
                openGallery()
            }
        }
    }

    private fun openGallery() { //abrir galeria
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun uploadImage(productId: String?,callback: (EventPost)->Unit){ //cargar la img a firebase storage
        val eventPost = EventPost()
        eventPost.documentId = productId?: /*este es para actualizar */ FirebaseFirestore.getInstance().collection(Constants.COLL_PRODUCTS) //este para crear
            .document().id //id del documento
        val storageRef = FirebaseStorage.getInstance().reference.child(Constants.PATH_PRODUCT_IMGES)

        photoSelectedUri?.let { uri ->
            binding?.let { binding ->
                val photoRef = storageRef.child(eventPost.documentId!!) //le da el nombre del documento a la img
                photoRef.putFile(uri) //subir img
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { dowloadUri -> //cuando este lista la url...
                            Log.i("URL",dowloadUri.toString()) //pone en la terminal la url de la img
                            eventPost.isSuccess = true
                            eventPost.photoUrl = dowloadUri.toString()
                            callback(eventPost)
                        }
                    }
                    .addOnFailureListener{
                        eventPost.isSuccess = false
                        callback(eventPost)
                    }
            }
        }
    }

    private fun save(product: Product,documentId: String){ //envia los datos a la db
        val db = FirebaseFirestore.getInstance() //instancia de la db
        db.collection(Constants.COLL_PRODUCTS)
            .document(documentId)
            .set(product)
            //.add(product)//se añade el producto
            .addOnSuccessListener {
                Toast.makeText(activity,"Producto añadido.",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity,"Error al insertar.",Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                enableUI(true)
                dismiss()
            }
    }
    private fun update(product: Product){ //envia los datos a la db
        val db = FirebaseFirestore.getInstance() //instancia de la db

        product.id?.let { id ->
            db.collection(Constants.COLL_PRODUCTS)
                .document(id)
                .set(product)// se edita el producto
                .addOnSuccessListener {
                    Toast.makeText(activity,"Producto actualizado.",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity,"Error al actualizar.",Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                    dismiss()
                }
        }
    }

    private fun enableUI(enable: Boolean){ //desabilita los editText para ya no crear mas de un producto
        positiveButton?.isEnabled = enable
        negativeButton?.isEnabled = enable
        binding?.let {
            with(it){
                etName.isEnabled = enable
                etDescription.isEnabled = enable
                etQuantity.isEnabled = enable
                etPrice.isEnabled = enable
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null //desvincula a binding
    }
}