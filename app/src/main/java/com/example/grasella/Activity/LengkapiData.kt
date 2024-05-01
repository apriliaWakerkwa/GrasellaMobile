package com.example.grasella.Activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.grasella.Model.ModelDatadiri
import com.example.grasella.R
import com.example.grasella.databinding.ActivityLengkapiDataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class LengkapiData : AppCompatActivity() {
    private lateinit var binding : ActivityLengkapiDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLengkapiDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etTanggalLahir.setOnClickListener {
            // Mendapatkan tanggal saat ini untuk menetapkan tanggal default pada DatePickerDialog
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            // Membuat instance DatePickerDialog
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Menetapkan tanggal yang dipilih pada EditText
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding.etTanggalLahir.setText(selectedDate)
            }, year, month, day)

            // Menampilkan dialog DatePickerDialog
            datePickerDialog.show()
        }

        binding.btnSimpan.setOnClickListener {
            tambahDataKeFirebase()
        }
    }

    private fun tambahDataKeFirebase() {
        if (validasiInput()) {
            // Mendapatkan nilai dari EditText
            val nama = binding.etNama.text.toString().trim()
            val jenisKelamin = when (binding.radioGroupGender.checkedRadioButtonId) {
                R.id.rbMale -> "Laki-laki"
                R.id.rbFemale -> "Perempuan"
                else -> ""
            }
            val tanggalLahir = binding.etTanggalLahir.text.toString().trim()
            val alamat = binding.etAlamat.text.toString().trim()
            val nomorTelepon = binding.etNomorTelepon.text.toString().trim()

            // Mendapatkan userID pengguna saat ini
            val userID = FirebaseAuth.getInstance().currentUser?.uid

            // Validasi userID
            if (userID.isNullOrEmpty()) {
                Toast.makeText(this, "UserID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            // Mendapatkan referensi database
            val database = FirebaseDatabase.getInstance()
            val reference = database.getReference("DataDiri")

            // Membuat objek data
            val dataDiri = ModelDatadiri(nama, jenisKelamin, tanggalLahir, alamat, nomorTelepon, userID)

            reference.push().setValue(dataDiri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menambahkan data: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validasiInput(): Boolean {
        val nama = binding.etNama.text.toString().trim()
        val jenisKelamin = when (binding.radioGroupGender.checkedRadioButtonId) {
            R.id.rbMale -> "Laki-laki"
            R.id.rbFemale -> "Perempuan"
            else -> ""
        }
        val tanggalLahir = binding.etTanggalLahir.text.toString().trim()
        val alamat = binding.etAlamat.text.toString().trim()
        val nomorTelepon = binding.etNomorTelepon.text.toString().trim()

        if (nama.isEmpty()) {
            binding.etNama.error = "Nama harus diisi"
            binding.etNama.requestFocus()
            return false
        }

        // Validasi panjang nomor telepon
        if (nomorTelepon.length > 13) {
            binding.etNomorTelepon.error = "Nomor harus terdiri dari 13 karakter"
            binding.etNomorTelepon.requestFocus()
            return false
        }

        if (jenisKelamin.isEmpty()) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return false
        }

        if (tanggalLahir.isEmpty()) {
            binding.etTanggalLahir.error = "Tanggal lahir harus diisi"
            binding.etTanggalLahir.requestFocus()
            return false
        }

        if (alamat.isEmpty()) {
            binding.etAlamat.error = "Alamat harus diisi"
            binding.etAlamat.requestFocus()
            return false
        }

        if (nomorTelepon.isEmpty()) {
            binding.etNomorTelepon.error = "Nomor telepon harus diisi"
            binding.etNomorTelepon.requestFocus()
            return false
        }

        return true
    }

}