package com.example.grasella.Activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.grasella.R
import com.example.grasella.databinding.ActivityDaftarBinding
import com.google.firebase.auth.FirebaseAuth

class DaftarActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDaftarBinding
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDaftar.isEnabled = false

        binding.tvMasuk.setOnClickListener {
            Intent(this,LoginActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.etKonfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = binding.etPasswordD.text.toString()
                val confirmPassword = binding.etKonfirm.text.toString()

                if (password == confirmPassword) {
                    binding.tvPasswordStatus.apply {
                        text = "Password Sama"
                        setTextColor(Color.parseColor("#00FF00")) // Warna hijau
                        visibility = View.VISIBLE
                    }
                    binding.btnDaftar.apply {
                        isEnabled = true
                    }
                } else {
                    binding.tvPasswordStatus.apply {
                        text = "Password Tidak Sama"
                        setTextColor(Color.parseColor("#FF0000")) // Warna merah
                        visibility = View.VISIBLE
                    }
                }
            }
        })

        //Menghubungkan Firebase
        mAuth = FirebaseAuth.getInstance()

        binding.btnDaftar.setOnClickListener {
            //Pendeklarasian variabel untuk menampung  EditText Email,Password,dan etKonfirmasi password
            val email : String = binding.etEmailD.text.toString().trim()
            val password : String = binding.etPasswordD.text.toString().trim()
            val etKonfirm : String = binding.etKonfirm.text.toString().trim()

//          Kondisi mengecek kolom email apabila kosong
            if(email.isEmpty()){
//              pesan yang akan tampil apabila kondisi terpenuhi
                binding.etEmailD.error = "Email Tidak Boleh Kosong"
//              cursor akan berfocus pada kolom email
                binding.etEmailD.requestFocus()
                return@setOnClickListener

//          Kondisi kedua apabila email tidak diisi dengan @ makan email tida valid
            } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//              pesan yang akan tampil apabila kondisi terpenuhi
                binding.etEmailD.error = "Email tidak valid"
//              cursor akan berfocus pada kolom email
                binding.etEmailD.requestFocus()
                return@setOnClickListener

//           Kondisi ketiga apa bila kolom password kosong atau kurang dari 8 karakter
            } else if(password.isEmpty() || password.length < 8) {
//              pesan yang akan tampil apabila kondisi terpenuhi
                binding.etPasswordD.error = "Minimal 8 karakter dan Tidak boleh kosong"
//              cursor akan berfocus pada kolom email
                binding.etPasswordD.requestFocus()
                return@setOnClickListener

//           Kondisi keempat apa bila kolom password dan kolom etKonfirm paswword berbeda
            } else if(password != etKonfirm){
//              pesan yang akan tampil apabila kondisi terpenuhi
                binding.etKonfirm.error = "Password tidak Sama"
//              cursor akan berfocus pada kolom password
                binding.etKonfirm.requestFocus()
                return@setOnClickListener

            }else {
//              Kondisi ini akan terpenuhi apabila kondisi diatas tidak terpenuhi atau tidak mengalami masalah
                registerUser(email, password)
            }

        }

    }

    //  Function untuk menyimpan data yang diisikan pada kolom" pendaftaran diatas
    private fun registerUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Berhasil Mendaftar", Toast.LENGTH_SHORT).show()
                Intent(this, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            } else {
                val errorMessage = task.exception?.message
                if (errorMessage == "The email address is already in use by another account.") {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Error")
                    alertDialogBuilder.setMessage("Email sudah terdaftar. Silakan gunakan email lain atau masuk dengan email tersebut.")
                    alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialogBuilder.setCancelable(false)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Error")
                    alertDialogBuilder.setMessage("Gagal mendaftar: $errorMessage")
                    alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialogBuilder.setCancelable(false)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
            }
        }
    }

}