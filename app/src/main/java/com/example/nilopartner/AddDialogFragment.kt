package com.example.nilopartner

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.nilopartner.databinding.FragmentDialogAddBinding
import com.google.firebase.firestore.FirebaseFirestore

//(4)
class AddDialogFragment : DialogFragment(),DialogInterface.OnShowListener {
    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null //boton positivo
    private var negativeButton: Button? = null

    private var product: Product? = null

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

        val dialog = dialog as? AlertDialog
        dialog?.let { //si dialog no es nulo...
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let {
                    if (product == null) { //dialogo de crear
                        val product = Product(
                            name = it.etName.text.toString().trim(), //trim elimina espacios al inicio o final
                            description = it.etDescription.text.toString().trim(),
                            quantity = it.etQuantity.text.toString().toInt(),
                            price = it.etPrice.text.toString().toDouble()
                        ) //se crea variable con todos los datos que se enviaron

                        save(product) //llamar la funcion para guardar los datos y crear el producto
                    }else{
                        product?.apply {
                            name = it.etName.text.toString().trim()
                            description = it.etDescription.text.toString().trim()
                            quantity = it.etQuantity.text.toString().toInt()
                            price = it.etPrice.text.toString().toDouble()

                            update(this) //se llama la funcion para guardar y editar el producto
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
            }
        }
    }

    private fun save(product: Product){ //envia los datos a la db
        val db = FirebaseFirestore.getInstance() //instancia de la db
        db.collection(getString(R.string.name_db_instance))
            .add(product)//se añade el producto
            .addOnSuccessListener {
                Toast.makeText(activity,"Producto añadido.",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity,"Error al insertar.",Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                dismiss()
            }
    }
    private fun update(product: Product){ //envia los datos a la db
        val db = FirebaseFirestore.getInstance() //instancia de la db

        product.id?.let { id ->
            db.collection(getString(R.string.name_db_instance))
                .document(id)
                .set(product)// se edita el producto
                .addOnSuccessListener {
                    Toast.makeText(activity,"Producto actualizado.",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity,"Error al actualizar.",Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    dismiss()
                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null //desvincula a binding
    }
}