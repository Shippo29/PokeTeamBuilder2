package com.example.poketeambuilder // <--- CORREGIDO: Quitamos el ".ui" para que coincida con tu carpeta

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// IMPORTS (Si alguno sale rojo, bórralo y escríbelo de nuevo)
import com.example.poketeambuilder.ui.home.HomeFragment
import com.example.poketeambuilder.data.model.PokemonTeam
import com.example.poketeambuilder.data.local.BaseDeDatos

import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Error Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) guardarEquipoDesdeQR(result.contents)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment(), "HOME_FRAGMENT")
                .commit()
        }

        configurarBotones()
        verificarUsuario()
    }

    private fun configurarBotones() {
        val layout = findViewById<android.view.ViewGroup>(android.R.id.content)
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // 1. LOGIN GOOGLE
        val btnGoogle = Button(this).apply {
            text = "INGRESAR CON GOOGLE"
            backgroundTintList = getColorStateList(android.R.color.holo_red_light)
            setOnClickListener { iniciarLoginGoogle() }
        }

        // 2. GENERAR QR
        val btnQR = Button(this).apply {
            text = "GENERAR QR (TEST)"
            backgroundTintList = getColorStateList(android.R.color.holo_blue_light)
            setOnClickListener {
                generarYMostrarQR(PokemonTeam(nombreEquipo = "Team ${System.currentTimeMillis() % 100}", pokemones = "Pikachu"))
            }
        }

        // 3. ESCANEAR
        val btnScan = Button(this).apply {
            text = "ESCANEAR CÁMARA"
            backgroundTintList = getColorStateList(android.R.color.holo_green_light)
            setOnClickListener {
                val options = ScanOptions().apply { setOrientationLocked(false) }
                barcodeLauncher.launch(options)
            }
        }

        // 4. VER LISTA (CRUD) - CORREGIDO
        val btnVerLista = Button(this).apply {
            text = "VER MIS EQUIPOS (CRUD)"
            backgroundTintList = getColorStateList(android.R.color.holo_purple)
            setOnClickListener {
                mostrarListaEquipos()
            }
        }

        // AGREGAMOS TODOS AL CONTENEDOR
        contenedor.addView(btnGoogle)
        contenedor.addView(btnQR)
        contenedor.addView(btnScan)
        contenedor.addView(btnVerLista) // <--- ESTA LÍNEA FALTABA, POR ESO SALÍA GRIS

        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 150
        }
        layout.addView(contenedor, params)
    }

    // --- LOGIN ---
    private fun iniciarLoginGoogle() {
        // SI ESTO SALE ROJO, HAZ EL PASO 2 MÁS ABAJO
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) verificarUsuario()
                else Toast.makeText(this, "Fallo Auth", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verificarUsuario() {
        val user = auth.currentUser
        if (user != null) Toast.makeText(this, "Hola: ${user.displayName}", Toast.LENGTH_LONG).show()
    }

    // --- QR ---
    private fun generarYMostrarQR(equipo: PokemonTeam) {
        try {
            val json = Gson().toJson(equipo)
            val bitmap = BarcodeEncoder().encodeBitmap(json, BarcodeFormat.QR_CODE, 600, 600)
            val img = ImageView(this).apply { setImageBitmap(bitmap) }
            AlertDialog.Builder(this).setView(img).setPositiveButton("Cerrar", null).show()
        } catch (e: Exception) {}
    }

    private fun guardarEquipoDesdeQR(json: String) {
        try {
            val equipo = Gson().fromJson(json, PokemonTeam::class.java).copy(id = 0, nombreEquipo = "Importado")
            lifecycleScope.launch {
                BaseDeDatos.getDatabase(applicationContext).teamDao().insertTeam(equipo)
                Toast.makeText(this@MainActivity, "Guardado!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {}
    }

    // --- CRUD LISTA ---
    private fun mostrarListaEquipos() {
        lifecycleScope.launch {
            val lista = BaseDeDatos.getDatabase(applicationContext).teamDao().getAllTeams()
            if (lista.isEmpty()) {
                Toast.makeText(this@MainActivity, "Sin equipos", Toast.LENGTH_SHORT).show()
            } else {
                val nombres = lista.map { "${it.id} - ${it.nombreEquipo}" }.toTypedArray()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Tus Equipos")
                    .setItems(nombres) { _, which -> confirmarBorrado(lista[which]) }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
        }
    }

    private fun confirmarBorrado(equipo: PokemonTeam) {
        AlertDialog.Builder(this)
            .setTitle("Borrar")
            .setMessage("¿Eliminar ${equipo.nombreEquipo}?")
            .setPositiveButton("SÍ") { _, _ ->
                lifecycleScope.launch {
                    BaseDeDatos.getDatabase(applicationContext).teamDao().deleteTeam(equipo)
                    Toast.makeText(this@MainActivity, "Eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}