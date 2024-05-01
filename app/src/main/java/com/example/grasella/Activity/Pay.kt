package com.example.grasella.Activity

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.grasella.Model.ModelDatadiri
import com.example.grasella.Model.ModelPulsa
import com.example.grasella.databinding.ActivityPayBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Pay : AppCompatActivity() {
    private lateinit var binding: ActivityPayBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var listDataDiri: ArrayList<ModelDatadiri>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listDataDiri = arrayListOf<ModelDatadiri>()
        fetchDataFromFirebase()
        ListOperator()

        binding.ivback.setOnClickListener {
            Intent(this,HomeActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        binding.ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.nominalRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = binding.root.findViewById<RadioButton>(checkedId)
            val nominal = selectedRadioButton.text.toString()
            val hargaPulsa = getHargaPulsa(nominal)
            binding.tvHargaPulsa.apply {
                text = "Rp. $hargaPulsa"
                visibility = View.VISIBLE
            }
        }

        binding.payButton.setOnClickListener {
            pay()
        }
    }

    private fun pay() {
        val selectedOperator = binding.operatorSpinner.selectedItem.toString()
        val selectedRadioButton: RadioButton? = binding.nominalRadioGroup.findViewById<RadioButton>(binding.nominalRadioGroup.checkedRadioButtonId)
        val selectedNominal: String? = selectedRadioButton?.text?.toString() // Gunakan tipe nullable untuk selectedNominal

        val nomorpulsa: String = binding.etPhoneNumber.text.toString().trim()

        // Validasi nomor pulsa
        if (nomorpulsa.isEmpty()) {
            binding.etPhoneNumber.error = "Nomor Harus Diisi"
            binding.etPhoneNumber.requestFocus()
            return
        }

        // Validasi panjang nomor telepon
        if (nomorpulsa.length <= 13) {
            binding.etPhoneNumber.error = "Nomor Hp Belum Lengkap"
            binding.etPhoneNumber.requestFocus()
            return
        }

        if (selectedOperator.isEmpty()) {
            Toast.makeText(this, "Pilih operator terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedNominal.isNullOrEmpty()) {
                // Tampilkan dialog atau pesan kesalahan karena RadioButton tidak dipilih
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Error")
                alertDialogBuilder.setMessage("Pilih nominal pulsa terlebih dahulu")
                alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialogBuilder.setCancelable(false)
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
                return
            }

        val hargaPulsa = selectedNominal?.let { getHargaPulsa(it) }

        // Mendapatkan userID pengguna saat ini
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        // Validasi userID
        if (userID.isNullOrEmpty()) {
            Toast.makeText(this, "UserID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Pulsa")

        val currentTime = Calendar.getInstance().timeInMillis
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = formatter.format(Date(currentTime))

        // Generate unique key untuk setiap transaksi
        val transactionID = reference.push().key

        // Membuat objek ModelPulsa
        val pulsa = ModelPulsa(
            userID,
            nomorpulsa,
            selectedOperator,
            selectedNominal,
            hargaPulsa,
            formattedDate
        )

        // Simpan data ke database Firebase
        reference.child(transactionID ?: "").setValue(pulsa)
            .addOnSuccessListener {
                selectedNominal?.let { it1 ->
                    hargaPulsa?.let { it2 ->
                        showConfirmationDialog(nomorpulsa, selectedOperator,
                            it1, it2
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmationDialog(nomorpulsa: String, selectedOperator: String, selectedNominal: String, hargaPulsa: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Konfirmasi Pembelian")
        alertDialogBuilder.setMessage("Apakah Anda yakin ingin membeli pulsa pada nomor $nomorpulsa dengan jumlah pulsa $hargaPulsa untuk operator $selectedOperator dengan Harga $selectedNominal?")
        alertDialogBuilder.setPositiveButton("Ya") { dialog, _ ->

            Toast.makeText(this, "Pembelian berhasil", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialogBuilder.setNegativeButton("Tidak") { dialog, _ ->

            Toast.makeText(this, "Pembelian dibatalkan", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        alertDialogBuilder.setCancelable(false)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun ListOperator() {
        // List opsi operator
        val options = arrayOf("Telkomsel", "XL", "Indosat", "Axis", "Smartfren")

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.operatorSpinner.adapter = adapter
        binding.operatorSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOption = options[position]
                    Toast.makeText(
                        applicationContext,
                        "Selected: $selectedOption",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Tidak melakukan apa pun saat tidak ada yang dipilih
                }
            }
    }

    private fun getHargaPulsa(nominal: String): String {
        return when (nominal) {
            "10.000" -> "10.000"
            "15.000" -> "15.000"
            "20.000" -> "20.000"
            "25.000" -> "25.000"
            "50.000" -> "50.000"
            "75.000" -> "75.000"
            "100.000" -> "100.000"
            else -> ""
        }
    }

    private fun fetchDataFromFirebase() {
        // Mendapatkan referensi database
        val database = FirebaseDatabase.getInstance()
        reference = database.getReference("DataDiri")

        // Mendapatkan userID pengguna saat ini
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        // Validasi userID
        if (userID.isNullOrEmpty()) {
            Toast.makeText(this, "UserID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Mendengarkan perubahan pada referensi DataDiri
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Bersihkan listDataDiri sebelum menambahkan data baru
                listDataDiri.clear()

                // Iterasi melalui setiap data pada snapshot
                for (data in dataSnapshot.children) {
                    // Ambil data dari snapshot dan tambahkan ke dalam listDataDiri
                    val datadiri = data.getValue(ModelDatadiri::class.java)
                    if (datadiri != null) {
                        listDataDiri.add(datadiri)
                    }
                }
                // Tampilkan data jika ditemukan
                displayDataForCurrentUser(userID)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(
                    applicationContext,
                    "Gagal mendapatkan data dari Firebase: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayDataForCurrentUser(userID: String) {
        // Cari data diri pengguna berdasarkan userID
        val userData = listDataDiri.find { it.userID == userID }

        // Cek apakah data ditemukan
        if (userData != null) {
            // Ambil nama dan nomor telepon dari data
            val nama = userData.nama
            binding.tvUsername.text = nama

        } else {
            // Tampilkan pesan data tidak ditemukan jika data tidak ada
            showDataNotFoundDialog()
        }
    }



    private fun showDataNotFoundDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Data Tidak Ditemukan")
        alertDialogBuilder.setMessage("Maaf, data Anda tidak ditemukan.")
        alertDialogBuilder.setCancelable(false) // Tidak bisa di-dismiss

        // Membuat tombol "OK"
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // Menutup dialog saat tombol "OK" ditekan
        }

        // Membuat dialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Fungsi untuk menampilkan alert dialog konfirmasi logout
    private fun showLogoutConfirmationDialog() {
        // Membuat instance dari AlertDialog.Builder
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            // Mengatur judul dialog
            setTitle("Konfirmasi Logout")
            // Mengatur pesan dialog
            setMessage("Apakah Anda yakin ingin logout?")
            // Mengatur tombol positif (Yes)
            setPositiveButton("Ya") { dialog, which ->
                // Logout user
                logout()
            }
            // Mengatur tombol negatif (No)
            setNegativeButton("Tidak") { dialog, which ->
                // Tidak melakukan apa-apa
                dialog.dismiss()
            }
            // Membuat dialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    // Fungsi untuk melakukan logout
    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}


