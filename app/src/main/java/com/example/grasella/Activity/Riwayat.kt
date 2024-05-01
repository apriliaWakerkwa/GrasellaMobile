package com.example.grasella.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grasella.Adapter.PulsaAdapter
import com.example.grasella.Model.ModelDatadiri
import com.example.grasella.Model.ModelPulsa
import com.example.grasella.R
import com.example.grasella.databinding.ActivityRiwayatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Riwayat : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatBinding
    private lateinit var noteRV: RecyclerView
    private lateinit var pulsaList: ArrayList<ModelPulsa>
    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var listDataDiri: ArrayList<ModelDatadiri>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            Intent(this, HomeActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        mAuth = FirebaseAuth.getInstance()

        binding.ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        noteRV = binding.rvPulsa
        noteRV.layoutManager = LinearLayoutManager(this)
        noteRV.setHasFixedSize(true)

        pulsaList = arrayListOf<ModelPulsa>()
        listDataDiri = arrayListOf<ModelDatadiri>()
        getdataRiwayat()
        fetchDataFromFirebase()

        binding.ivBack.setOnClickListener {
            Intent(this, HomeActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    private fun getdataRiwayat() {
        noteRV.visibility = View.GONE
        binding.tvNodata.visibility = View.GONE

        val currentUserID = mAuth.currentUser?.uid // Dapatkan ID pengguna yang sedang login

        if (currentUserID != null) {
            dbRef = FirebaseDatabase.getInstance().getReference("Pulsa")

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pulsaList.clear()
                    if (snapshot.exists()) {
                        for (noteSnap in snapshot.children) {
                            val noteData = noteSnap.getValue(ModelPulsa::class.java)
                            if (noteData?.userID == currentUserID) { // Periksa apakah userId pada catatan sama dengan userId pengguna yang sedang login
                                pulsaList.add(noteData)
                            }
                        }
                        val nAdapter = PulsaAdapter(pulsaList)
                        noteRV.adapter = nAdapter

                        nAdapter.setOnItemClickListener(object : PulsaAdapter.onItemClickListener {
                            override fun onItemClick(position: Int) {
                                val intent = Intent(this@Riwayat, DetailActivity::class.java)

                                intent.putExtra("userID", pulsaList[position].userID)
                                intent.putExtra("nomorPulsa", pulsaList[position].nomorPulsa)
                                intent.putExtra("operator", pulsaList[position].operator)
                                intent.putExtra("nominal", pulsaList[position].nominal)
                                intent.putExtra("harga", pulsaList[position].harga)
                                intent.putExtra("tanggal", pulsaList[position].tanggal)
                                startActivity(intent)
                            }
                        })

                        // Tampilkan teks "No Data" jika daftar pulsa kosong
                        if (pulsaList.isEmpty()) {
                            binding.tvNodata.visibility = View.VISIBLE
                        } else {
                            binding.tvNodata.visibility = View.GONE
                        }

                        noteRV.visibility = View.VISIBLE
                    } else {
                        // Tampilkan teks "No Data" jika tidak ada data
                        binding.tvNodata.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tidak melakukan apa-apa saat pembatalan
                }
            })
        }
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

    private fun fetchDataFromFirebase() {
        // Mendapatkan referensi database
        val database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("DataDiri")

        // Mendapatkan userID pengguna saat ini
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        // Validasi userID
        if (userID.isNullOrEmpty()) {
            Toast.makeText(this, "UserID pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Mendengarkan perubahan pada referensi DataDiri
        dbRef.addValueEventListener(object : ValueEventListener {
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
}