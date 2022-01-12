package com.example.nilopartner

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.nilopartner.databinding.FragmentDialogAddBinding

//(4)
class AddDialogFragment : DialogFragment(),DialogInterface.OnShowListener {
    private var binding: FragmentDialogAddBinding? = null

    private var positiveButton: Button? = null //boton positivo
    private var negativeButton: Button? = null

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

    override fun onShow(dialogInterface: DialogInterface?) {
        val dialog = dialog as? AlertDialog
        dialog?.let { //si dialog no es nulo...
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {  }
            negativeButton?.setOnClickListener {
                dismiss() //desaparece el cuadro de dialogo
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null //desvincula a binding
    }
}