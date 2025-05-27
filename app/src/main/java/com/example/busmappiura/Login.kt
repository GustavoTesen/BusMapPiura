package com.example.busmappiura

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.rectangle)
        val passwordEditText = findViewById<EditText>(R.id.rectangle1)
        val loginBtn = findViewById<View>(R.id.rectangle_5)
        val registerBtn = findViewById<View>(R.id.registrarse)

        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val db = FirebaseFirestore.getInstance()

                            if (userId != null) {
                                val userDocRef = db.collection("usuarios").document(userId)
                                userDocRef.get().addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        // Es la primera vez, guardamos ese dato
                                        val userData = hashMapOf("primeraVez" to true)
                                        userDocRef.set(userData)
                                    }
                                    // Redirigimos después de verificar
                                    startActivity(Intent(this, Rutas::class.java))
                                    finish()
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Error al verificar el usuario", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Error al obtener ID del usuario", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }
}