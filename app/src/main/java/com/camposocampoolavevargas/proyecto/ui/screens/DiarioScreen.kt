package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.diario.config.DiarioConfig
import com.camposocampoolavevargas.proyecto.diario.domain.model.EntradaDiario
import com.camposocampoolavevargas.proyecto.diario.ui.viewmodel.DiarioViewModel
import com.camposocampoolavevargas.proyecto.diario.util.DiarioUtils
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme

// Naranja de la app
private val AcentoNaranja = Color(0xFFF78166)

// ─────────────────────────────────────────────────────────────
// PANTALLA PRINCIPAL — Diario de Preocupaciones (SPEC-07)
// ─────────────────────────────────────────────────────────────

/**
 * Pantalla del módulo Diario de Preocupaciones (SPEC-07).
 * Integra campo de texto, historial expandible y Soft Delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiarioScreen(
    navController: NavController,
    viewModel: DiarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar de éxito
    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeExito()
        }
    }
    // Snackbar de error
    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Diario de Preocupaciones",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Banner si es ventana nocturna ─────────────────
            AnimatedVisibility(
                visible = DiarioUtils.esVentanaNocturna(),
                enter = fadeIn(tween(400)),
                exit  = fadeOut(tween(300))
            ) {
                BannerNocturno()
            }

            // ── Sección de entrada ────────────────────────────
            DiarioInputSection(
                onGuardar = { contenido, autoEliminar ->
                    viewModel.guardarEntrada(contenido, autoEliminar)
                }
            )

            // ── Sección historial ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis Entradas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${uiState.entradas.size} registros",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.cargando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AcentoNaranja, modifier = Modifier.size(32.dp))
                }
            } else if (uiState.entradas.isEmpty()) {
                DiarioEmptyState()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.entradas.forEach { entrada ->
                        DiarioEntradaCard(
                            entrada    = entrada,
                            onEliminar = { viewModel.eliminarEntrada(entrada.uuid) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENTE: Banner Ventana Nocturna
// ─────────────────────────────────────────────────────────────

@Composable
private fun BannerNocturno() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
            .border(1.dp, AcentoNaranja.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "🌙", fontSize = 22.sp)
            Text(
                text = DiarioConfig.VentanaNocturna.BANNER_TEXTO,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB0BEC5),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENTE: Sección de nueva entrada
// ─────────────────────────────────────────────────────────────

/**
 * Sección de formulario para crear una nueva entrada en el diario.
 * Incluye campo multilínea, contador de caracteres y checkbox de auto-eliminación.
 */
@Composable
private fun DiarioInputSection(
    onGuardar: (contenido: String, autoEliminar: Boolean) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var autoEliminar by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    val maxChars = DiarioConfig.Visualizacion.MAX_CHARS_ENTRADA
    val porcentaje = (texto.length.toFloat() / maxChars).coerceIn(0f, 1f)
    val colorContador = when {
        porcentaje > 0.9f -> Color(0xFFDA3633)
        porcentaje > 0.75f -> Color(0xFFE3B341)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "📝", fontSize = 20.sp)
                Text(
                    text = "Nueva Entrada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Campo de texto
            OutlinedTextField(
                value = texto,
                onValueChange = { if (it.length <= maxChars) texto = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = {
                    Text(
                        text = "Escribe lo que te preocupa hoy...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcentoNaranja,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = AcentoNaranja,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Contador de caracteres
            Text(
                text = "${texto.length} / $maxChars",
                style = MaterialTheme.typography.labelSmall,
                color = colorContador,
                modifier = Modifier.align(Alignment.End)
            )

            // Checkbox auto-eliminación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (autoEliminar) AcentoNaranja.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .clickable { autoEliminar = !autoEliminar }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Checkbox(
                    checked = autoEliminar,
                    onCheckedChange = { autoEliminar = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AcentoNaranja,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Auto-eliminar al despertar",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Esta entrada se borrará automáticamente a las 06:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botón guardar
            Button(
                onClick = {
                    onGuardar(texto, autoEliminar)
                    texto = ""
                    focusManager.clearFocus()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = texto.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcentoNaranja,
                    contentColor = Color.White,
                    disabledContainerColor = AcentoNaranja.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "Guardar Entrada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENTE: Tarjeta de entrada existente
// ─────────────────────────────────────────────────────────────

/**
 * Tarjeta expandible para una entrada del diario.
 * Muestra vista previa truncada y permite expandir/contraer y eliminar.
 */
@Composable
private fun DiarioEntradaCard(
    entrada: EntradaDiario,
    onEliminar: () -> Unit
) {
    var expandida by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (expandida)
            AcentoNaranja.copy(alpha = 0.04f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label = "bgDiario"
    )

    // Diálogo de confirmación de eliminación
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(DiarioConfig.Mensajes.CONFIRMAR_ELIMINAR) },
            text = {
                Text(
                    text = "Esta acción no puede deshacerse.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        onEliminar()
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFDA3633), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (expandida) 6.dp else 2.dp, RoundedCornerShape(16.dp))
            .then(
                if (entrada.autoEliminar) Modifier.border(
                    1.dp, AcentoNaranja.copy(alpha = 0.2f), RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clickable { expandida = !expandida },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DiarioUtils.formatearCompleto(entrada.fechaEntrada),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!expandida && entrada.contenido.length > DiarioConfig.Visualizacion.MAX_CHARS_PREVIEW)
                            "${entrada.contenido.take(DiarioConfig.Visualizacion.MAX_CHARS_PREVIEW)}…"
                        else
                            entrada.contenido,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expandida) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (entrada.autoEliminar) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(AcentoNaranja.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🌙", fontSize = 11.sp)
                        }
                    }
                    Icon(
                        imageVector = if (expandida) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandida) "Contraer" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Sección expandida: botón eliminar
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                exit  = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (entrada.autoEliminar) "🌙 Se borrará al despertar"
                            else "📌 Guardada permanentemente",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entrada.autoEliminar) AcentoNaranja
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = { mostrarDialogo = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFDA3633)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ESTADO VACÍO
// ─────────────────────────────────────────────────────────────

@Composable
private fun DiarioEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📓", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin entradas todavía",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Escribe lo que te preocupa antes de dormir. Puedes borrarlo automáticamente al despertar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF1E293B)
@Composable
fun DiarioScreenPreview() {
    DormiBienUTheme {
        DiarioScreen(navController = rememberNavController())
    }
}
