package com.example.grasella.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.grasella.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.tvDaftar.setOnClickListener {
            Intent(this, DaftarActivity::class.java).also{
                startActivity(it)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Intent(this, ForgetActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnLogin.setOnClickListener {

//            membuat variabel pada editText
            val email : String = binding.etEmailM.text.toString().trim()
            val pass  : String = binding.etPasswordM.text.toString().trim()

//            Kondisi mengecek EditText atau kolom email apabila kosong
            if (email.isEmpty()){
//               pesan yang akan tampil apabila kondisi terpenuhi
                binding.etEmailM.error = "Email Tidak Boleh Kosong"
//                cursor akan berfocus pada kolom email
                binding.etEmailM.requestFocus()
                return@setOnClickListener

//           Kondisi kedua apabila email tidak diisi dengan @ makan email tida valid
            }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//                Pesan yang akan muncul apabila kondisi terpenuhi
                binding.etEmailM.error = "Email Tidak Valid"
//                cursor akan berfocus pada kolom email
                binding.etEmailM.requestFocus()
                return@setOnClickListener

//           Kondisi ketiga apa bila kolom password kosong atau kurang dari 8 karakter
            }else if(pass.isEmpty() || pass.length < 8){
//                Pesan yang akan muncul apabila kondisi terpenuhi
                binding.etPasswordM.error = "Maksimal 8 karakter dan Tidak boleh kosong"
//                cursor akan berfocus pada kolom email
                binding.etPasswordM.requestFocus()
                return@setOnClickListener

            }else{
//              Kondisi ini akan terpenuhi apabila kondisi diatas tidak terpenuhi atau tidak mengalami masalah
                loginUser(email,pass)
            }

        }

    }

    //  Function untuk melakukan Login dan melakukan pengecekan pada database firebase
    private fun loginUser(email: String, pass: String) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Berhasil Masuk", Toast.LENGTH_SHORT).show()
                val userID = mAuth.currentUser?.uid
                checkDataExistence(userID)
            } else {
                val errorMessage = task.exception?.message
                if (errorMessage == "The email address is badly formatted.") {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Error")
                    alertDialogBuilder.setMessage("Format email salah. Silakan masukkan email yang valid.")
                    alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialogBuilder.setCancelable(false)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else if (errorMessage == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Error")
                    alertDialogBuilder.setMessage("Akun tidak terdaftar. Silakan daftar akun terlebih dahulu.")
                    alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialogBuilder.setCancelable(false)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Error")
                    alertDialogBuilder.setMessage("Gagal masuk: $errorMessage")
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


    // Fungsi untuk memeriksa apakah data sudah ada dalam database berdasarkan userID
    private fun checkDataExistence(userID: String?) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("DataDiri")
        reference.orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jika data ditemukan, arahkan pengguna ke MainActivity
                if (snapshot.exists()) {
                    Intent(this@LoginActivity, HomeActivity::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    // Jika data tidak ditemukan, arahkan pengguna ke halaman LengkapiData
                    Intent(this@LoginActivity, LengkapiData::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Gagal memeriksa data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}