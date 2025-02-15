package com.example.grasella.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.grasella.R
import com.example.grasella.databinding.ActivityForgetBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.tvBatal.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnResetF.setOnClickListener {
            val email: String = binding.etEmailF.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmailF.error = "Email Tidak Boleh Kosong"
                binding.etEmailF.requestFocus()
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmailF.error = "Email Tidak Valid"
                binding.etEmailF.requestFocus()
                return@setOnClickListener
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Cek email untuk Reset Password", Toast.LENGTH_SHORT).show()
                        Intent(this, LoginActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    } else {
                        val errorMessage = task.exception?.message
                        if (errorMessage == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                            val alertDialogBuilder = AlertDialog.Builder(this)
                            alertDialogBuilder.setTitle("Error")
                            alertDialogBuilder.setMessage("Email belum terdaftar. Silakan daftar akun terlebih dahulu.")
                            alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            alertDialogBuilder.setCancelable(false)
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        } else {
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }
}